package com.example.sidequesting.telegrambot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Catches unhandled exceptions in the WebFlux pipeline and returns a clean HTTP 500 instead of
 * leaking stack traces to Telegram (which would cause repeated retries on the same broken update).
 *
 * <p>Order(-2) places this before Spring Boot's default {@code DefaultErrorWebExceptionHandler}.
 */
@Configuration
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    log.error("Unhandled error on {}: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);

    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    byte[] bytes = "{\"error\":\"internal server error\"}".getBytes();
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}
