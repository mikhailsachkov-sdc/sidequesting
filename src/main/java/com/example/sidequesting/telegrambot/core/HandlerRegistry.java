package com.example.sidequesting.telegrambot.core;

import com.example.sidequesting.telegrambot.annotation.BotController;
import com.example.sidequesting.telegrambot.annotation.CallbackHandler;
import com.example.sidequesting.telegrambot.annotation.CommandHandler;
import com.example.sidequesting.telegrambot.annotation.TextHandler;
import com.example.sidequesting.telegrambot.model.CallbackQuery;
import com.example.sidequesting.telegrambot.model.Message;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Scans all Spring beans annotated with {@link BotController} and registers their methods as
 * handlers.
 *
 * <p>At startup it builds three lookup structures:
 *
 * <ul>
 *   <li><b>commandHandlers</b> – {@code "/command" → HandlerMethod}
 *   <li><b>callbackHandlers</b> – {@code "namespace" → HandlerMethod}
 *   <li><b>textHandlers</b> – list of catch-all text handlers
 * </ul>
 *
 * <p>At dispatch time, {@link #handleMessage} and {@link #handleCallback} resolve the best matching
 * handler and invoke it via reflection, performing argument injection based on the method's
 * parameter types.
 */
@Component
public class HandlerRegistry {
  private static final Logger log = LoggerFactory.getLogger(HandlerRegistry.class);

  private final ApplicationContext ctx;

  private final Map<String, HandlerMethod> commandHandlers =
      new HashMap<>(); // command string → handler
  private final Map<String, HandlerMethod> callbackHandlers =
      new HashMap<>(); // callback namespace → handler
  private final List<HandlerMethod> textHandlers = new ArrayList<>(); // text handlers

  /**
   * Constructs the HandlerRegistry.
   *
   * @param ctx the application context
   */
  public HandlerRegistry(ApplicationContext ctx) {
    this.ctx = ctx;
  }

  // -----------------------------------------------------------------------
  // Startup scan
  // -----------------------------------------------------------------------

  /** Scans for controllers. */
  @PostConstruct
  public void scan() {
    Map<String, Object> controllers = ctx.getBeansWithAnnotation(BotController.class);
    log.info("Scanning {} @BotController bean(s): {}", controllers.size(), controllers.keySet());

    for (Object bean : controllers.values()) {
      for (Method method : bean.getClass().getDeclaredMethods()) {
        method.setAccessible(true);
        registerMethod(bean, method);
      }
    }

    log.info(
        "Registered {} command(s), {} callback namespace(s), {} text handler(s)",
        commandHandlers.size(),
        callbackHandlers.size(),
        textHandlers.size());
  }

  private void registerMethod(Object bean, Method method) {
    CommandHandler cmd = method.getAnnotation(CommandHandler.class);
    if (cmd != null) {
      HandlerMethod hm = new HandlerMethod(bean, method);
      for (String command : cmd.value()) {
        commandHandlers.put(command.toLowerCase(), hm);
        log.debug(
            "  @CommandHandler {} → {}.{}",
            command,
            bean.getClass().getSimpleName(),
            method.getName());
      }
    }

    CallbackHandler cb = method.getAnnotation(CallbackHandler.class);
    if (cb != null) {
      HandlerMethod hm = new HandlerMethod(bean, method);
      callbackHandlers.put(cb.value(), hm);
      log.debug(
          "  @CallbackHandler \"{}\" → {}.{}",
          cb.value(),
          bean.getClass().getSimpleName(),
          method.getName());
    }

    TextHandler txt = method.getAnnotation(TextHandler.class);
    if (txt != null) {
      textHandlers.add(new HandlerMethod(bean, method));
      log.debug("  @TextHandler → {}.{}", bean.getClass().getSimpleName(), method.getName());
    }
  }

  // -----------------------------------------------------------------------
  // Dispatch – messages
  // -----------------------------------------------------------------------

  /**
   * Handles an incoming message.
   *
   * @param message the message
   * @return the Mono result
   */
  public Mono<Void> handleMessage(Message message) {
    if (message.text() == null) {
      return Mono.empty();
    }

    String text = message.text().trim();

    // 1. Try command handlers first
    if (text.startsWith("/")) {
      String command = text.split("\\s+")[0].toLowerCase(); // strip @botname suffix too
      HandlerMethod hm = commandHandlers.get(command);
      if (hm != null) {
        return invoke(hm, message, null, null);
      }
      return Mono.empty();
    }

    // 2. Try text handlers
    for (HandlerMethod hm : textHandlers) {
      return invoke(hm, message, null, null);
    }

    return Mono.empty();
  }

  // -----------------------------------------------------------------------
  // Dispatch – callback queries
  // -----------------------------------------------------------------------

  /**
   * Handles an incoming callback query.
   *
   * @param cq the callback query
   * @return the Mono result
   */
  public Mono<Void> handleCallback(CallbackQuery cq) {
    String data = cq.data() != null ? cq.data() : "";
    String[] parts = data.split(":", 2);
    String namespace = parts[0];
    String value = parts.length > 1 ? parts[1] : "";

    HandlerMethod hm =
        callbackHandlers.getOrDefault(namespace, callbackHandlers.get("*")); // wildcard fallback

    if (hm == null) {
      log.warn("No @CallbackHandler for namespace \"{}\"", namespace);
      return Mono.empty();
    }

    return invoke(hm, null, cq, value);
  }

  // -----------------------------------------------------------------------
  // Reflection invocation with argument injection
  // -----------------------------------------------------------------------

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Mono<Void> invoke(
      HandlerMethod hm, Message message, CallbackQuery cq, String callbackValue) {
    try {
      Object[] args = resolveArgs(hm.method(), message, cq, callbackValue);
      Object result = hm.method().invoke(hm.bean(), args);

      if (result instanceof Mono mono) {
        return mono;
      }
      return Mono.empty();

    } catch (Exception e) {
      return Mono.error(
          new RuntimeException("Handler invocation failed: " + hm.method().getName(), e));
    }
  }

  private Object[] resolveArgs(
      Method method, Message message, CallbackQuery cq, String callbackValue) {
    Class<?>[] types = method.getParameterTypes();
    Object[] args = new Object[types.length];

    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];

      if (type.isAssignableFrom(Message.class) && message != null) {
        args[i] = message;
      } else if (type.isAssignableFrom(CallbackQuery.class) && cq != null) {
        args[i] = cq;
      } else if (type == String.class) {
        args[i] = callbackValue;
      } else if (type == long.class || type == Long.class) {
        args[i] =
            message != null ? message.chat().id() : cq != null ? cq.message().chat().id() : 0L;
      } else {
        args[i] = null;
      }
    }

    return args;
  }

  // -----------------------------------------------------------------------

  /** Internal record bundling a bean and its method. */
  record HandlerMethod(Object bean, Method method) {}
}
