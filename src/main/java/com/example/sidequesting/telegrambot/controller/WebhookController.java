package com.example.sidequesting.telegrambot.controller;

import com.example.sidequesting.telegrambot.core.UpdateDispatcher;
import com.example.sidequesting.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Webhook endpoint – active only when {@code telegram.mode=webhook}.
 *
 * <p>Telegram will POST each update to {@code /webhook/{secret}} (the path is configured in {@code
 * application.yml}). The secret path segment is a simple security measure to ensure only Telegram
 * can trigger this endpoint.
 *
 * <p>The controller must respond with HTTP 200 quickly; heavy work is done reactively downstream
 * without blocking.
 */
@RestController
@RequestMapping("${telegram.webhook.path:/webhook}")
@ConditionalOnProperty(name = "telegram.mode", havingValue = "webhook")
public class WebhookController {

  private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

  private final UpdateDispatcher dispatcher;

  /**
   * Constructs the WebhookController.
   *
   * @param dispatcher the dispatcher
   */
  public WebhookController(UpdateDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  /**
   * Receives an update via webhook.
   *
   * @param update the update
   * @return the response entity mono
   */
  @PostMapping
  public Mono<ResponseEntity<Void>> receiveUpdate(@RequestBody Update update) {
    log.debug("Webhook received update_id={}", update.updateId());

    // Dispatch asynchronously – Telegram only needs a 200 OK back.
    // subscribeOn / publishOn can be added here if CPU-bound work is involved.
    dispatcher.dispatch(update).subscribe();

    return Mono.just(ResponseEntity.noContent().build());
  }
}
