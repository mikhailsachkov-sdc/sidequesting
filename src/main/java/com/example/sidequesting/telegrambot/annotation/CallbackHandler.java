package com.example.sidequesting.telegrambot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method to callback queries whose {@code data} field starts with the given namespace
 * prefix (the part before {@code :}).
 *
 * <h2>Supported method signatures</h2>
 *
 * <ul>
 *   <li>{@code Mono<Void> handler(CallbackQuery cq)}
 *   <li>{@code Mono<Void> handler(CallbackQuery cq, String value)} — {@code value} is the part
 *       <em>after</em> the first {@code :}
 *   <li>{@code Mono<Void> handler()}
 * </ul>
 *
 * <pre>{@code
 * // Handles callback data like "confirm:yes", "confirm:no"
 * @CallbackHandler("confirm")
 * public Mono<Void> onConfirm(CallbackQuery cq, String value) {
 *     boolean yes = "yes".equals(value);
 *     ...
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CallbackHandler {

  /**
   * The namespace prefix to match (without the trailing {@code :}). Use {@code "*"} to match all
   * callback queries not handled elsewhere.
   */
  String value();
}
