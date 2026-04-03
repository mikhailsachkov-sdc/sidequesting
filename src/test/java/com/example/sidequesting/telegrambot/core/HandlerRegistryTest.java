package com.example.sidequesting.telegrambot.core;

import static org.mockito.Mockito.*;

import com.example.sidequesting.telegrambot.annotation.BotController;
import com.example.sidequesting.telegrambot.annotation.CommandHandler;
import com.example.sidequesting.telegrambot.annotation.TextHandler;
import com.example.sidequesting.telegrambot.model.Chat;
import com.example.sidequesting.telegrambot.model.Message;
import com.example.sidequesting.telegrambot.model.User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HandlerRegistryTest {

  // ── Minimal fake @BotController ────────────────────────────────────────
  @BotController
  static class FakeController {

    boolean startCalled;
    boolean textCalled;

    @CommandHandler("/start")
    public Mono<Void> onStart(Message message) {
      startCalled = true;
      return Mono.empty();
    }

    @TextHandler
    public Mono<Void> onText(Message message) {
      textCalled = true;
      return Mono.empty();
    }
  }

  // ── Test setup ─────────────────────────────────────────────────────────

  FakeController controller;
  HandlerRegistry registry;

  @BeforeEach
  void setUp() {
    controller = new FakeController();

    ApplicationContext ctx = mock(ApplicationContext.class);
    when(ctx.getBeansWithAnnotation(BotController.class))
        .thenReturn(Map.of("fakeController", controller));

    registry = new HandlerRegistry(ctx);
    registry.scan();
  }

  // ── Tests ──────────────────────────────────────────────────────────────

  @Test
  void commandHandlerIsInvoked() {
    StepVerifier.create(registry.handleMessage(message(10L, "/start"))).verifyComplete();
    assert controller.startCalled;
    assert !controller.textCalled;
  }

  @Test
  void textHandlerFiresWhenNotCommand() {
    StepVerifier.create(registry.handleMessage(message(30L, "random text"))).verifyComplete();
    assert controller.textCalled;
    assert !controller.startCalled;
  }

  @Test
  void unknownCommandReturnsEmpty() {
    StepVerifier.create(registry.handleMessage(message(40L, "/unknown"))).verifyComplete();
    assert !controller.startCalled;
    assert !controller.textCalled; // /unknown starts with / so text handlers don't run
  }

  // ── Helpers ────────────────────────────────────────────────────────────

  private static Message message(long userId, String text) {
    var user = new User(userId, false, "Test", null, null, null);
    var chat = new Chat(userId, "private", null, null, "Test", null);
    return new Message(1L, user, chat, text, 0L);
  }
}
