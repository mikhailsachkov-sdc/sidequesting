package com.example.sidequesting.telegrambot.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Creates the shared {@link WebClient} that targets the Telegram Bot API, plus enables {@link
 * TelegramProperties} binding.
 */
@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
public class BotConfig {
  private static final Logger log = LoggerFactory.getLogger(BotConfig.class);
  private static final String TELEGRAM_API_BASE = "https://api.telegram.org";

  /**
   * Creates the telegram web client.
   *
   * @param builder the builder
   * @param props the properties
   * @return the web client
   */
  @Bean
  @Qualifier("telegram")
  public WebClient telegramWebClient(WebClient.Builder builder, TelegramProperties props) {
    // Reactor Netty client with generous read timeout for long-polling
    HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(30));

    return builder
        .baseUrl(TELEGRAM_API_BASE + "/bot" + props.token())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(logRequest())
        .filter(logResponse())
        .build();
  }

  private static ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        req -> {
          log.debug("→ {} {}", req.method(), req.url());
          return Mono.just(req);
        });
  }

  private static ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(
        res -> {
          if (res.statusCode().isError()) {
            log.warn("← HTTP {}", res.statusCode().value());
          }
          return Mono.just(res);
        });
  }
}
