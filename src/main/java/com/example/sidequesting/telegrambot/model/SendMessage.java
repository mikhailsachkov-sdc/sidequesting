package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for the Telegram sendMessage API method.
 *
 * <p>Build instances via {@link SendMessage#builder()}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMessage(
    @JsonProperty("chat_id") long chatId,
    @JsonProperty("text") String text,
    // "HTML" | "Markdown" | "MarkdownV2"
    @JsonProperty("parse_mode") String parseMode,
    // InlineKeyboardMarkup or ReplyKeyboardMarkup
    @JsonProperty("reply_markup") Object replyMarkup) {

  // -----------------------------------------------------------------------
  // Fluent builder
  // -----------------------------------------------------------------------

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for SendMessage. */
  public static final class Builder {

    private long chatId;
    private String text;
    private String parseMode;
    private Object replyMarkup;

    private Builder() {}

    /**
     * Sets the chat id.
     *
     * @param chatId the chat id
     * @return the builder
     */
    public Builder chatId(long chatId) {
      this.chatId = chatId;
      return this;
    }

    /**
     * Sets the text.
     *
     * @param text the text
     * @return the builder
     */
    public Builder text(String text) {
      this.text = text;
      return this;
    }

    /**
     * Convenience shortcut – sets parse mode to "HTML".
     *
     * @return the builder
     */
    public Builder html() {
      this.parseMode = "HTML";
      return this;
    }

    /**
     * Sets the parse mode.
     *
     * @param parseMode the parse mode
     * @return the builder
     */
    public Builder parseMode(String parseMode) {
      this.parseMode = parseMode;
      return this;
    }

    /**
     * Sets the reply markup.
     *
     * @param replyMarkup the reply markup
     * @return the builder
     */
    public Builder replyMarkup(Object replyMarkup) {
      this.replyMarkup = replyMarkup;
      return this;
    }

    /**
     * Builds the SendMessage.
     *
     * @return the SendMessage
     */
    public SendMessage build() {
      return new SendMessage(chatId, text, parseMode, replyMarkup);
    }
  }
}
