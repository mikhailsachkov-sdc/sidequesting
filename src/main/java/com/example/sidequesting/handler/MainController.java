package com.example.sidequesting.handler;

import com.example.sidequesting.service.SidequestPromptService;
import com.example.sidequesting.service.UserSessionStore;
import com.example.sidequesting.service.UserSessionStore.BotState;
import com.example.sidequesting.telegrambot.annotation.BotController;
import com.example.sidequesting.telegrambot.annotation.CommandHandler;
import com.example.sidequesting.telegrambot.annotation.TextHandler;
import com.example.sidequesting.telegrambot.keyboard.InlineKeyboardMarkup;
import com.example.sidequesting.telegrambot.keyboard.InlineKeyboardMarkup.InlineKeyboardButton;
import com.example.sidequesting.telegrambot.model.Message;
import com.example.sidequesting.telegrambot.model.SendMessage;
import com.example.sidequesting.telegrambot.service.TelegramClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Controller for the main onboarding flow and handling of core commands. */
@BotController
public class MainController {

  private final TelegramClient telegramClient;
  private final UserSessionStore sessionStore;
  private final SidequestPromptService sidequestPromptService;

  /**
   * Constructs the MainController.
   *
   * @param telegramClient the telegram client used to send messages
   * @param sessionStore the store for maintaining user session states
   * @param sidequestPromptService the service for generating sidequests
   */
  public MainController(
      TelegramClient telegramClient,
      UserSessionStore sessionStore,
      SidequestPromptService sidequestPromptService) {
    this.telegramClient = telegramClient;
    this.sessionStore = sessionStore;
    this.sidequestPromptService = sidequestPromptService;
  }

  /**
   * Handles the /start command. Initiates or restarts the onboarding flow.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/start")
  public Mono<Void> onStart(Message message) {
    long userId = message.from().id();
    var session = sessionStore.getOrCreate(userId);
    // Restart onboarding by clearing onboarded flag
    sessionStore.put(userId, session.withData("onboarded", "false").withState(BotState.IDLE));

    return askForLanguage(
        message.chat().id(),
        "Welcome to Sidequesting! Let's get you onboarded.\n"
            + "First, what is your preferred language?");
  }

  /**
   * Handles the /language command. Asks the user to choose their preferred language.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/language")
  public Mono<Void> onLanguage(Message message) {
    return askForLanguage(message.chat().id(), "Please choose your preferred language:");
  }

  /**
   * Sends an inline keyboard asking the user to select a language.
   *
   * @param chatId the ID of the chat to send the message to
   * @param text the text of the message accompanying the keyboard
   * @return a Mono signaling completion of the send operation
   */
  private Mono<Void> askForLanguage(long chatId, String text) {
    var markup =
        InlineKeyboardMarkup.builder()
            .row(
                InlineKeyboardButton.callback("English 🇬🇧", "lang:English"),
                InlineKeyboardButton.callback("Lithuanian 🇱🇹", "lang:Lithuanian"))
            .build();

    return telegramClient.sendMessage(
        SendMessage.builder().chatId(chatId).text(text).replyMarkup(markup).build());
  }

  /**
   * Handles the /difficulty command. Prompts the user for their preferred difficulty.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/difficulty")
  public Mono<Void> onDifficulty(Message message) {
    long userId = message.from().id();
    sessionStore.put(
        userId, sessionStore.getOrCreate(userId).withState(BotState.AWAITING_DIFFICULTY));
    return telegramClient.sendMessage(
        message.chat().id(), "Please enter your preferred difficulty level:");
  }

  /**
   * Handles the /location command. Prompts the user for their preferred location.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/location")
  public Mono<Void> onLocation(Message message) {
    long userId = message.from().id();
    sessionStore.put(
        userId, sessionStore.getOrCreate(userId).withState(BotState.AWAITING_LOCATION));
    return telegramClient.sendMessage(message.chat().id(), "Please enter your preferred location:");
  }

  /**
   * Handles the /range command. Prompts the user for their preferred range.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/range")
  public Mono<Void> onRange(Message message) {
    long userId = message.from().id();
    sessionStore.put(userId, sessionStore.getOrCreate(userId).withState(BotState.AWAITING_RANGE));
    return telegramClient.sendMessage(message.chat().id(), "Please enter your preferred range:");
  }

  /**
   * Handles the /sidequest command. Generates a sidequest if onboarding is complete.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/sidequest")
  public Mono<Void> onSidequest(Message message) {
    long userId = message.from().id();
    var session = sessionStore.getOrCreate(userId);

    if (!"true".equals(session.getData("onboarded"))) {
      return telegramClient.sendMessage(
          message.chat().id(), "Please complete onboarding first using /start.");
    }

    String language = session.getData("language");
    String location = session.getData("location");
    String difficulty = session.getData("difficulty");

    return telegramClient
        .sendMessage(message.chat().id(), "⏳ Generating your sidequest...")
        .then(
            Mono.fromCallable(
                    () -> sidequestPromptService.generateSidequest(location, difficulty, language))
                .subscribeOn(Schedulers.boundedElastic()))
        .flatMap(sidequest -> telegramClient.sendMessage(message.chat().id(), sidequest));
  }

  /**
   * Handles the /help command. Displays available commands based on onboarding status.
   *
   * @param message the incoming message from the user
   * @return a Mono signaling completion of the handling process
   */
  @CommandHandler("/help")
  public Mono<Void> onHelp(Message message) {
    long userId = message.from().id();
    var session = sessionStore.getOrCreate(userId);
    if ("true".equals(session.getData("onboarded"))) {
      return telegramClient.sendMessage(
          message.chat().id(),
          "Available commands:\n"
              + "/start - Restart onboarding\n"
              + "/language - Change language\n"
              + "/difficulty - Change difficulty\n"
              + "/location - Change location\n"
              + "/range - Change range\n"
              + "/sidequest - Generate a sidequest\n"
              + "/help - Show this help");
    } else {
      return telegramClient.sendMessage(
          message.chat().id(), "You haven't completed onboarding yet. Please start with /start.");
    }
  }

  /**
   * General text handler for processing user input based on current state.
   *
   * @param message the incoming text message from the user
   * @return a Mono signaling completion of the handling process
   */
  @TextHandler
  public Mono<Void> onText(Message message) {
    long userId = message.from().id();
    var session = sessionStore.getOrCreate(userId);

    return switch (session.state()) {
      case AWAITING_DIFFICULTY -> handleDifficultyInput(message, userId, session);
      case AWAITING_LOCATION -> handleLocationInput(message, userId, session);
      case AWAITING_RANGE -> handleRangeInput(message, userId, session);
      default -> handleUnknownInput(message);
    };
  }

  /**
   * Handles user input for difficulty and updates session state.
   *
   * @param message the user message
   * @param userId the user ID
   * @param session the current user session
   * @return a Mono representing the handling operation
   */
  private Mono<Void> handleDifficultyInput(
      Message message, long userId, UserSessionStore.UserSession session) {
    var newSession = session.withData("difficulty", message.text());
    if ("true".equals(session.getData("onboarded"))) {
      sessionStore.put(userId, newSession.withState(BotState.IDLE));
      return telegramClient.sendMessage(message.chat().id(), "Difficulty updated!");
    } else {
      sessionStore.put(userId, newSession.withState(BotState.AWAITING_LOCATION));
      return telegramClient.sendMessage(
          message.chat().id(), "Difficulty saved! Next, what location do you prefer?");
    }
  }

  /**
   * Handles user input for location and updates session state.
   *
   * @param message the user message
   * @param userId the user ID
   * @param session the current user session
   * @return a Mono representing the handling operation
   */
  private Mono<Void> handleLocationInput(
      Message message, long userId, UserSessionStore.UserSession session) {
    var newSession = session.withData("location", message.text());
    if ("true".equals(session.getData("onboarded"))) {
      sessionStore.put(userId, newSession.withState(BotState.IDLE));
      return telegramClient.sendMessage(message.chat().id(), "Location updated!");
    } else {
      sessionStore.put(userId, newSession.withState(BotState.AWAITING_RANGE));
      return telegramClient.sendMessage(
          message.chat().id(), "Location saved! Finally, what range do you prefer?");
    }
  }

  /**
   * Handles user input for range and updates session state to complete onboarding.
   *
   * @param message the user message
   * @param userId the user ID
   * @param session the current user session
   * @return a Mono representing the handling operation
   */
  private Mono<Void> handleRangeInput(
      Message message, long userId, UserSessionStore.UserSession session) {
    boolean wasOnboarded = "true".equals(session.getData("onboarded"));
    var newSession =
        session
            .withData("range", message.text())
            .withData("onboarded", "true")
            .withState(BotState.IDLE);
    sessionStore.put(userId, newSession);

    if (wasOnboarded) {
      return telegramClient.sendMessage(message.chat().id(), "Range updated!");
    } else {
      return telegramClient.sendMessage(
          message.chat().id(),
          "Range saved! Onboarding is complete! 🎉\nYou can now use /sidequest "
              + "to generate quests or /help to see available commands.");
    }
  }

  /**
   * Handles unexpected input when not awaiting anything.
   *
   * @param message the user message
   * @return a Mono representing the handling operation
   */
  private Mono<Void> handleUnknownInput(Message message) {
    return telegramClient.sendMessage(
        message.chat().id(),
        "I don't understand that. Please use /help or complete your onboarding.");
  }
}
