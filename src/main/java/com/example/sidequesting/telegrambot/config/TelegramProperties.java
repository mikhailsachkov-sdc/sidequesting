package com.example.sidequesting.telegrambot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Binds all {@code telegram.*} properties from {@code application.yml}.
 *
 * <p>Register in your main class or a {@code @Configuration} class:
 *
 * <pre>{@code
 * @EnableConfigurationProperties(TelegramProperties.class)
 * }</pre>
 *
 * <p>or simply annotate this class with {@code @Component} (done here).
 */
@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
    String token, @DefaultValue("polling") String mode, @DefaultValue Webhook webhook) {

  public boolean isWebhookMode() {
    return "webhook".equalsIgnoreCase(mode);
  }

  public boolean isPollingMode() {
    return !isWebhookMode();
  }

  /** Webhook properties. */
  public record Webhook(
      @DefaultValue("https://yourdomain.com") String url, @DefaultValue("/webhook") String path) {

    /**
     * Gets the full URL.
     *
     * @return the full URL
     */
    public String fullUrl() {
      return url + path;
    }
  }
}
