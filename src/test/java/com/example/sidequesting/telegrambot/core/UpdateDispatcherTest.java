package com.example.sidequesting.telegrambot.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.sidequesting.telegrambot.model.CallbackQuery;
import com.example.sidequesting.telegrambot.model.Chat;
import com.example.sidequesting.telegrambot.model.Message;
import com.example.sidequesting.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UpdateDispatcherTest {

  @Mock private HandlerRegistry registry;

  private UpdateDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    dispatcher = new UpdateDispatcher(registry);
  }

  @Test
  void dispatchesMessageUpdate() {
    var message = new Message(1L, null, chat(100L), "/start", 0L);
    var update = new Update(42L, message, null);

    when(registry.handleMessage(message)).thenReturn(Mono.empty());

    StepVerifier.create(dispatcher.dispatch(update)).verifyComplete();

    verify(registry).handleMessage(message);
    verifyNoMoreInteractions(registry);
  }

  @Test
  void dispatchesCallbackQueryUpdate() {
    var cq =
        new CallbackQuery(
            "cq1", null, new Message(2L, null, chat(100L), null, 0L), "action:confirm");
    var update = new Update(43L, null, cq);

    when(registry.handleCallback(cq)).thenReturn(Mono.empty());

    StepVerifier.create(dispatcher.dispatch(update)).verifyComplete();

    verify(registry).handleCallback(cq);
    verifyNoMoreInteractions(registry);
  }

  @Test
  void swallowsHandlerErrors() {
    var message = new Message(3L, null, chat(200L), "boom", 0L);
    var update = new Update(44L, message, null);

    when(registry.handleMessage(any())).thenReturn(Mono.error(new RuntimeException("oops")));

    // Dispatcher must complete without propagating the error
    StepVerifier.create(dispatcher.dispatch(update)).verifyComplete();
  }

  @Test
  void handlesUnknownUpdateGracefully() {
    var update = new Update(45L, null, null); // no message, no callbackQuery

    StepVerifier.create(dispatcher.dispatch(update)).verifyComplete();

    verifyNoInteractions(registry);
  }

  private static Chat chat(long id) {
    return new Chat(id, "private", null, null, "Test", null);
  }
}
