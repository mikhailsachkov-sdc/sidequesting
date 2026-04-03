package com.example.sidequesting.telegrambot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method to one or more Telegram bot commands (e.g. {@code /start}).
 *
 * <h2>Supported method signatures</h2>
 *
 * <ul>
 *   <li>{@code Mono<Void> handler(Message message)}
 *   <li>{@code Mono<Void> handler(long chatId)}
 *   <li>{@code Mono<Void> handler()}
 * </ul>
 *
 * <pre>{@code
 * @CommandHandler({"/start", "/hello"})
 * public Mono<Void> onStart(Message message) {
 *     return telegramClient.sendMessage(message.chat().id(), "Hello!");
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandHandler {

  /** One or more commands this method handles. Each value must start with {@code /}. */
  String[] value();
}
