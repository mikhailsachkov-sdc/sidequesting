package com.example.sidequesting.telegrambot.core;

import com.example.sidequesting.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Receives every Telegram {@link Update} (from polling or webhook) and delegates to {@link
 * HandlerRegistry}, which resolves the right {@code @BotController} method.
 */
@Service
public class UpdateDispatcher {

  private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

  private final HandlerRegistry registry;

  /**
   * Constructs the UpdateDispatcher.
   *
   * @param registry the registry
   */
  public UpdateDispatcher(HandlerRegistry registry) {
    this.registry = registry;
  }

  /**
   * Dispatches the update.
   *
   * @param update the update
   * @return the Mono result
   */
  public Mono<Void> dispatch(Update update) {
    Mono<Void> handler;

    if (update.message() != null) {
      log.debug("Dispatching message update_id={}", update.updateId());
      handler = registry.handleMessage(update.message());

    } else if (update.callbackQuery() != null) {
      log.debug("Dispatching callbackQuery update_id={}", update.updateId());
      handler = registry.handleCallback(update.callbackQuery());

    } else {
      log.warn("Unknown update type update_id={}", update.updateId());
      handler = Mono.empty();
    }

    return handler.onErrorResume(
        err -> {
          log.error("Error processing update_id={}: {}", update.updateId(), err.getMessage(), err);
          return Mono.empty();
        });
  }
}
