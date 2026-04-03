package com.example.sidequesting.telegrambot.keyboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Inline keyboard that appears directly below a message.
 *
 * <pre>{@code
 * var markup = InlineKeyboardMarkup.builder()
 *     .row(
 *         InlineKeyboardButton.callback("Yes", "confirm:yes"),
 *         InlineKeyboardButton.callback("No",  "confirm:no")
 *     )
 *     .row(
 *         InlineKeyboardButton.url("Open site", "https://example.com")
 *     )
 *     .build();
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InlineKeyboardMarkup(
    @JsonProperty("inline_keyboard") List<List<InlineKeyboardButton>> inlineKeyboard) {

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for InlineKeyboardMarkup. */
  public static final class Builder {

    private final List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    /**
     * Add a row of buttons.
     *
     * @param buttons the buttons
     * @return the builder
     */
    public Builder row(InlineKeyboardButton... buttons) {
      rows.add(Arrays.asList(buttons));
      return this;
    }

    /**
     * Builds the markup.
     *
     * @return the markup
     */
    public InlineKeyboardMarkup build() {
      return new InlineKeyboardMarkup(rows);
    }
  }

  // -----------------------------------------------------------------------

  /** A single button inside an inline keyboard row. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record InlineKeyboardButton(
      @JsonProperty("text") String text,
      @JsonProperty("callback_data") String callbackData,
      @JsonProperty("url") String url) {

    /** Button that sends a callback query when pressed. */
    public static InlineKeyboardButton callback(String text, String callbackData) {
      return new InlineKeyboardButton(text, callbackData, null);
    }

    /** Button that opens a URL. */
    public static InlineKeyboardButton url(String text, String url) {
      return new InlineKeyboardButton(text, null, url);
    }
  }
}
