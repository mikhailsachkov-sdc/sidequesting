package com.example.sidequesting.telegrambot.service;

import com.example.sidequesting.telegrambot.core.UpdateDispatcher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Drives the Telegram long-polling loop when {@code telegram.mode=polling} (the default).
 *
 * <p>The loop:
 *
 * <ol>
 *   <li>Calls {@code getUpdates} with a 30-second long-poll timeout.
 *   <li>Dispatches each received update through {@link UpdateDispatcher}.
 *   <li>Advances the offset so processed updates are acknowledged.
 *   <li>On network error, waits 5 seconds and retries.
 * </ol>
 *
 * <p>Activate by setting {@code telegram.mode=polling} (or leaving it absent – polling is the
 * default). When running with webhooks, set {@code telegram.mode=webhook} to disable this bean
 * entirely.
 */
@Service
@ConditionalOnProperty(name = "telegram.mode", havingValue = "polling", matchIfMissing = true)
public class LongPollingService {

  private static final Logger log = LoggerFactory.getLogger(LongPollingService.class);
  private static final int POLL_TIMEOUT_SEC = 10;
  private static final Duration RETRY_DELAY = Duration.ofSeconds(5);

  private final TelegramClient telegramClient;
  private final UpdateDispatcher dispatcher;

  private Disposable subscription;

  /**
   * Constructs the LongPollingService.
   *
   * @param telegramClient the telegram client
   * @param dispatcher the update dispatcher
   */
  public LongPollingService(TelegramClient telegramClient, UpdateDispatcher dispatcher) {
    this.telegramClient = telegramClient;
    this.dispatcher = dispatcher;
  }

  /** Starts the polling loop. */
  @PostConstruct
  public void start() {
    log.info("Starting Telegram long-polling loop (timeout={}s)", POLL_TIMEOUT_SEC);

    // Mutable offset box – Flux doesn't allow raw mutable state, so we
    // use a one-element array as a simple mutable holder.
    long[] offset = {0L};

    subscription =
        Flux.defer(
                () ->
                    telegramClient
                        .getUpdates(offset[0], POLL_TIMEOUT_SEC)
                        .flatMapIterable(
                            response -> {
                              var updates = response.result();
                              if (updates != null && !updates.isEmpty()) {
                                // Advance offset past the last seen update
                                offset[0] = updates.getLast().updateId() + 1;
                              }
                              return updates != null ? updates : java.util.List.of();
                            })
                        .concatMap(dispatcher::dispatch) // sequential per-update
                )
            .repeat() // loop forever
            .onErrorContinue(
                (err, obj) -> {
                  log.error(
                      "Polling error, retrying in {}s: {}",
                      RETRY_DELAY.getSeconds(),
                      err.getMessage());
                  // Block reactor scheduler thread briefly to apply retry delay.
                  // In production you may prefer retryWhen(Retry.fixedDelay(...)).
                  try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                  } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                  }
                })
            .subscribe();
  }

  /** Stops the polling loop. */
  @PreDestroy
  public void stop() {
    log.info("Stopping Telegram long-polling loop");
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
    }
  }

  // Helper for callers that want to await one full poll cycle in tests
  Mono<Void> pollOnce(long[] offset) {
    return telegramClient
        .getUpdates(offset[0], 0)
        .flatMap(
            response -> {
              var updates = response.result();
              if (updates != null && !updates.isEmpty()) {
                offset[0] = updates.getLast().updateId() + 1;
              }
              return Flux.fromIterable(updates != null ? updates : java.util.List.of())
                  .concatMap(dispatcher::dispatch)
                  .then();
            });
  }
}
