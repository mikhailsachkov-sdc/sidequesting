package com.example.sidequesting.telegrambot.annotation;

import com.example.sidequesting.telegrambot.core.HandlerRegistry;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Marks a class as a Telegram bot handler component.
 *
 * <p>Classes annotated with {@code @BotController} are picked up by {@link HandlerRegistry} at
 * startup. They must be Spring beans (this annotation is meta-annotated with {@link Component}, so
 * component-scanning picks them up automatically).
 *
 * <pre>{@code
 * @BotController
 * public class StartHandler {
 *
 *     @CommandHandler("/start")
 *     public Mono<Void> onStart(Message message) { ... }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BotController {}
