package com.example.sidequesting.telegrambot.keyboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Custom reply keyboard that replaces the default keyboard.
 *
 * <pre>{@code
 * var markup = ReplyKeyboardMarkup.builder()
 *     .row("Option A", "Option B")
 *     .row("Cancel")
 *     .resizeKeyboard(true)
 *     .oneTimeKeyboard(true)
 *     .build();
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReplyKeyboardMarkup(
    @JsonProperty("keyboard") List<List<KeyboardButton>> keyboard,
    @JsonProperty("resize_keyboard") Boolean resizeKeyboard,
    @JsonProperty("one_time_keyboard") Boolean oneTimeKeyboard,
    @JsonProperty("input_field_placeholder") String inputFieldPlaceholder) {

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for ReplyKeyboardMarkup. */
  public static final class Builder {

    private final List<List<KeyboardButton>> rows = new ArrayList<>();
    private Boolean resizeKeyboard;
    private Boolean oneTimeKeyboard;
    private String placeholder;

    /**
     * Add a row from plain text labels.
     *
     * @param labels the labels
     * @return the builder
     */
    public Builder row(String... labels) {
      List<KeyboardButton> row = Arrays.stream(labels).map(KeyboardButton::text).toList();
      rows.add(row);
      return this;
    }

    /**
     * Add a row from pre-built KeyboardButton objects.
     *
     * @param buttons the buttons
     * @return the builder
     */
    public Builder row(KeyboardButton... buttons) {
      rows.add(Arrays.asList(buttons));
      return this;
    }

    /**
     * Set resize keyboard.
     *
     * @param resize the resize
     * @return the builder
     */
    public Builder resizeKeyboard(boolean resize) {
      this.resizeKeyboard = resize;
      return this;
    }

    /**
     * Set one time keyboard.
     *
     * @param oneTime the one time
     * @return the builder
     */
    public Builder oneTimeKeyboard(boolean oneTime) {
      this.oneTimeKeyboard = oneTime;
      return this;
    }

    /**
     * Set placeholder.
     *
     * @param placeholder the placeholder
     * @return the builder
     */
    public Builder placeholder(String placeholder) {
      this.placeholder = placeholder;
      return this;
    }

    /**
     * Builds the markup.
     *
     * @return the markup
     */
    public ReplyKeyboardMarkup build() {
      return new ReplyKeyboardMarkup(rows, resizeKeyboard, oneTimeKeyboard, placeholder);
    }
  }

  // -----------------------------------------------------------------------

  /** Represents a KeyboardButton. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record KeyboardButton(
      @JsonProperty("text") String text,
      @JsonProperty("request_contact") Boolean requestContact,
      @JsonProperty("request_location") Boolean requestLocation) {

    /**
     * Creates a text button.
     *
     * @param text the text
     * @return the button
     */
    public static KeyboardButton text(String text) {
      return new KeyboardButton(text, null, null);
    }

    /**
     * Creates a request contact button.
     *
     * @param text the text
     * @return the button
     */
    public static KeyboardButton requestContact(String text) {
      return new KeyboardButton(text, true, null);
    }

    /**
     * Creates a request location button.
     *
     * @param text the text
     * @return the button
     */
    public static KeyboardButton requestLocation(String text) {
      return new KeyboardButton(text, null, true);
    }
  }
}
