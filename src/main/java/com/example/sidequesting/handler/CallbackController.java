package com.example.sidequesting.handler;

import com.example.sidequesting.service.UserSessionStore;
import com.example.sidequesting.service.UserSessionStore.BotState;
import com.example.sidequesting.telegrambot.annotation.BotController;
import com.example.sidequesting.telegrambot.annotation.CallbackHandler;
import com.example.sidequesting.telegrambot.model.CallbackQuery;
import com.example.sidequesting.telegrambot.model.EditMessageText;
import com.example.sidequesting.telegrambot.model.SendMessage;
import com.example.sidequesting.telegrambot.service.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/** Handles callback queries from inline keyboards. */
@BotController
public class CallbackController {

  private static final Logger log = LoggerFactory.getLogger(CallbackController.class);

  private final TelegramClient telegramClient;
  private final UserSessionStore sessionStore;

  /**
   * Constructs the CallbackController.
   *
   * @param telegramClient the telegram client
   * @param sessionStore the session store
   */
  public CallbackController(TelegramClient telegramClient, UserSessionStore sessionStore) {
    this.telegramClient = telegramClient;
    this.sessionStore = sessionStore;
  }

  /**
   * Handles confirm callbacks.
   *
   * @param cq the callback query
   * @param value the value
   * @return the Mono result
   */
  @CallbackHandler("confirm")
  public Mono<Void> onConfirm(CallbackQuery cq, String value) {
    String reply = "yes".equals(value) ? "✅ Confirmed!" : "❌ Cancelled.";

    return telegramClient
        .answerCallbackQuery(cq.id(), reply)
        .then(
            telegramClient.editMessageText(
                EditMessageText.builder()
                    .chatId(cq.message().chat().id())
                    .messageId(cq.message().messageId())
                    .text(reply)
                    .build()));
  }

  /**
   * Handles language selection callbacks.
   *
   * @param cq the callback query
   * @param language the selected language
   * @return the Mono result
   */
  @CallbackHandler("lang")
  public Mono<Void> onLanguageSelected(CallbackQuery cq, String language) {
    long userId = cq.from().id();
    var session = sessionStore.getOrCreate(userId);

    var newSession = session.withData("language", language);
    boolean onboarded = "true".equals(session.getData("onboarded"));

    Mono<Void> answerMono =
        telegramClient.answerCallbackQuery(cq.id(), "Language set to " + language);
    Mono<Void> editMono =
        telegramClient.editMessageText(
            EditMessageText.builder()
                .chatId(cq.message().chat().id())
                .messageId(cq.message().messageId())
                .text("Language set to: " + language)
                .build());

    if (onboarded) {
      sessionStore.put(userId, newSession);
      return answerMono.then(editMono);
    } else {
      sessionStore.put(userId, newSession.withState(BotState.AWAITING_DIFFICULTY));
      Mono<Void> nextStepMono =
          telegramClient.sendMessage(
              SendMessage.builder()
                  .chatId(cq.message().chat().id())
                  .text("Next, what difficulty level do you prefer?")
                  .build());
      return answerMono.then(editMono).then(nextStepMono);
    }
  }

  /**
   * Handles unknown callbacks.
   *
   * @param cq the callback query
   * @return the Mono result
   */
  @CallbackHandler("*")
  public Mono<Void> onUnknownCallback(CallbackQuery cq) {
    log.warn("Unhandled callback data: {}", cq.data());
    return telegramClient.answerCallbackQuery(cq.id(), "Unknown action");
  }
}
