package com.example.sidequesting.telegrambot.service;

import com.example.sidequesting.telegrambot.model.EditMessageText;
import com.example.sidequesting.telegrambot.model.GetUpdatesResponse;
import com.example.sidequesting.telegrambot.model.SendMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Thin reactive wrapper around the Telegram Bot API.
 *
 * <p>All methods return cold {@link Mono} publishers – subscribe (or chain) to actually trigger
 * HTTP calls.
 */
@Service
public class TelegramClient {

  private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);

  private final WebClient webClient;

  /**
   * Constructor for TelegramClient.
   *
   * @param telegramWebClient the web client
   */
  public TelegramClient(@Qualifier("telegram") WebClient telegramWebClient) {
    this.webClient = telegramWebClient;
  }

  /** Fetch me object. */
  public Mono<String> getMe() {
    return webClient
        .get()
        .uri(u -> u.path("/getMe").build())
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(err -> log.error("getMe failed: {}", err.getMessage()));
  }

  // -----------------------------------------------------------------------
  // Sending
  // -----------------------------------------------------------------------

  /** Send a text message (with optional keyboard / parse mode). */
  public Mono<Void> sendMessage(SendMessage request) {
    return webClient
        .post()
        .uri("/sendMessage")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("sendMessage failed: {}", err.getMessage()));
  }

  /** Convenience overload – plain text, no markup. */
  public Mono<Void> sendMessage(long chatId, String text) {
    return sendMessage(SendMessage.builder().chatId(chatId).text(text).build());
  }

  // -----------------------------------------------------------------------
  // Editing
  // -----------------------------------------------------------------------

  /**
   * Edit the text (and optionally the inline keyboard) of an existing message. Commonly used after
   * handling a callback query to update the message in place.
   */
  public Mono<Void> editMessageText(EditMessageText request) {
    return webClient
        .post()
        .uri("/editMessageText")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("editMessageText failed: {}", err.getMessage()));
  }

  // -----------------------------------------------------------------------
  // Answering callback queries
  // -----------------------------------------------------------------------

  /**
   * Acknowledge a callback query (required to clear the loading spinner on the button). Optionally
   * show a toast message to the user.
   *
   * @param callbackQueryId the ID from {@code CallbackQuery.id()}
   * @param text optional toast text (null = silent acknowledge)
   */
  public Mono<Void> answerCallbackQuery(String callbackQueryId, String text) {
    record Body(
        @com.fasterxml.jackson.annotation.JsonProperty("callback_query_id") String id,
        @com.fasterxml.jackson.annotation.JsonProperty("text") String txt) {}

    return webClient
        .post()
        .uri("/answerCallbackQuery")
        .bodyValue(new Body(callbackQueryId, text))
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("answerCallbackQuery failed: {}", err.getMessage()));
  }

  // -----------------------------------------------------------------------
  // Long polling
  // -----------------------------------------------------------------------

  /**
   * Fetch pending updates.
   *
   * @param offset pass the highest seen {@code update_id + 1} to acknowledge previous updates; pass
   *     {@code 0} for the first call
   * @param timeout long-polling timeout in seconds (0 = short poll)
   */
  public Mono<GetUpdatesResponse> getUpdates(long offset, int timeout) {
    return webClient
        .get()
        .uri(
            u ->
                u.path("/getUpdates")
                    .queryParam("offset", offset)
                    .queryParam("timeout", timeout)
                    .queryParam("allowed_updates", "message,callback_query")
                    .build())
        .retrieve()
        .bodyToMono(GetUpdatesResponse.class)
        .doOnError(err -> log.error("getUpdates failed", err));
  }

  // -----------------------------------------------------------------------
  // Webhook management
  // -----------------------------------------------------------------------

  /**
   * Register a webhook URL with Telegram.
   *
   * @param url fully-qualified HTTPS URL that Telegram will POST updates to
   */
  public Mono<Void> setWebhook(String url) {
    record Body(@JsonProperty("url") String u) {}

    return webClient
        .post()
        .uri("/setWebhook")
        .bodyValue(new Body(url))
        .retrieve()
        .bodyToMono(Void.class);
  }

  /** Remove any previously registered webhook (switches bot back to long-polling mode). */
  public Mono<Void> deleteWebhook() {
    return webClient.post().uri("/deleteWebhook").retrieve().bodyToMono(Void.class);
  }
}
