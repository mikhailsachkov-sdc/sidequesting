package com.example.sidequesting.telegrambot.config;

import com.example.sidequesting.telegrambot.service.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/** Initializer for the bot. */
@Component
public class BotInitializer implements InitializingBean {
  private static final Logger log = LoggerFactory.getLogger(BotInitializer.class);
  private final TelegramClient telegramClient;

  /**
   * Constructor for BotInitializer.
   *
   * @param telegramClient the telegram client
   */
  public BotInitializer(TelegramClient telegramClient) {
    this.telegramClient = telegramClient;
  }

  @Override
  public void afterPropertiesSet() {
    log.info("Verifying Telegram API token...");
    try {
      telegramClient.getMe().block();
      log.info("Telegram API is reachable and token is valid.");
    } catch (Exception e) {
      log.error(
          "Failed to reach Telegram API. Token might be invalid or network is unreachable.", e);
      throw new IllegalStateException("Failed to verify Telegram API token", e);
    }
  }
}
