package com.example.sidequesting.telegrambot.keyboard;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Instructs Telegram to remove the custom reply keyboard from the user's client. */
public record ReplyKeyboardRemove(
    @JsonProperty("remove_keyboard") boolean removeKeyboard,
    @JsonProperty("selective") boolean selective) {

  /** Remove keyboard for all users in the chat. */
  public static ReplyKeyboardRemove remove() {
    return new ReplyKeyboardRemove(true, false);
  }

  /** Remove keyboard only for the user that sent the last message. */
  public static ReplyKeyboardRemove removeSelective() {
    return new ReplyKeyboardRemove(true, true);
  }
}
