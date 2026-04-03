package com.example.sidequesting.telegrambot.config;

import com.example.sidequesting.telegrambot.service.TelegramClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Registers the webhook URL with Telegram on startup, and removes it on shutdown when {@code
 * telegram.mode=webhook}.
 */
@Component
@ConditionalOnProperty(name = "telegram.mode", havingValue = "webhook")
public class WebhookInitializer {

  private static final Logger log = LoggerFactory.getLogger(WebhookInitializer.class);

  @Value("${telegram.webhook.url}")
  private String webhookUrl;

  @Value("${telegram.webhook.path:/webhook}")
  private String webhookPath;

  private final TelegramClient telegramClient;

  /**
   * Constructor for WebhookInitializer.
   *
   * @param telegramClient the telegram client
   */
  public WebhookInitializer(TelegramClient telegramClient) {
    this.telegramClient = telegramClient;
  }

  /** Registers the webhook. */
  @PostConstruct
  public void registerWebhook() {
    String fullUrl = webhookUrl + webhookPath;
    log.info("Registering webhook: {}", fullUrl);
    telegramClient
        .setWebhook(fullUrl)
        .doOnSuccess(v -> log.info("Webhook registered successfully"))
        .doOnError(e -> log.error("Failed to register webhook: {}", e.getMessage()))
        .subscribe();
  }

  /** Deregisters the webhook. */
  @PreDestroy
  public void deregisterWebhook() {
    log.info("Removing webhook on shutdown");
    telegramClient
        .deleteWebhook()
        .doOnSuccess(v -> log.info("Webhook removed"))
        .doOnError(e -> log.error("Failed to remove webhook: {}", e.getMessage()))
        .block(); // block so shutdown completes before process exits
  }
}
