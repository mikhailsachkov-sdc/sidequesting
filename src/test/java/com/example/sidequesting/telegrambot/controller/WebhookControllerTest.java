package com.example.sidequesting.telegrambot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sidequesting.telegrambot.core.UpdateDispatcher;
import com.example.sidequesting.telegrambot.model.Chat;
import com.example.sidequesting.telegrambot.model.Message;
import com.example.sidequesting.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(WebhookController.class)
@TestPropertySource(
    properties = {
      "telegram.mode=webhook",
      "telegram.token=test-token",
      "telegram.webhook.url=https://example.com",
      "telegram.webhook.path=/webhook/test"
    })
class WebhookControllerTest {

  @Autowired WebTestClient webTestClient;
  @MockitoBean UpdateDispatcher dispatcher;

  @Test
  void returnsOkAndDispatchesUpdate() {
    var update =
        new Update(
            1L,
            new Message(
                1L, null, new Chat(100L, "private", null, null, "Test", null), "/start", 0L),
            null);

    when(dispatcher.dispatch(any())).thenReturn(Mono.empty());

    webTestClient
        .post()
        .uri("/webhook/test")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(update)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(dispatcher).dispatch(any(Update.class));
  }
}
