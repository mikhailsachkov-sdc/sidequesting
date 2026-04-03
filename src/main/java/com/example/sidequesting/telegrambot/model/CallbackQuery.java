package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Sent when a user presses a button on an inline keyboard. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CallbackQuery(
    @JsonProperty("id") String id,
    @JsonProperty("from") User from,
    @JsonProperty("message") Message message,
    // the callback_data value set on the button
    @JsonProperty("data") String data) {}
