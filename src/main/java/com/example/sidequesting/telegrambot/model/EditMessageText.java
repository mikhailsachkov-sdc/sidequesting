package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for the Telegram {@code editMessageText} API method.
 *
 * <p>Used to update the text (and optionally the inline keyboard) of a previously sent message –
 * the standard pattern after a callback query.
 *
 * <pre>{@code
 * telegramClient.editMessageText(
 *     EditMessageText.builder()
 *         .chatId(chatId)
 *         .messageId(originalMessageId)
 *         .text("Updated content")
 *         .replyMarkup(newKeyboard)
 *         .build()
 * );
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditMessageText(
    @JsonProperty("chat_id") Long chatId,
    @JsonProperty("message_id") Long messageId,
    @JsonProperty("inline_message_id")
        String inlineMessageId, // alternative to chat_id + message_id for inline mode
    @JsonProperty("text") String text,
    @JsonProperty("parse_mode") String parseMode,
    @JsonProperty("reply_markup") Object replyMarkup) {

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for EditMessageText. */
  public static final class Builder {

    private Long chatId;
    private Long messageId;
    private String inlineMessageId;
    private String text;
    private String parseMode;
    private Object replyMarkup;

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
     * Sets the message id.
     *
     * @param messageId the message id
     * @return the builder
     */
    public Builder messageId(long messageId) {
      this.messageId = messageId;
      return this;
    }

    /**
     * Sets the inline message id.
     *
     * @param inlineMessageId the inline message id
     * @return the builder
     */
    public Builder inlineMessageId(String inlineMessageId) {
      this.inlineMessageId = inlineMessageId;
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
     * Sets the parse mode to HTML.
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
     * Builds the EditMessageText.
     *
     * @return the EditMessageText
     */
    public EditMessageText build() {
      return new EditMessageText(chatId, messageId, inlineMessageId, text, parseMode, replyMarkup);
    }
  }
}
