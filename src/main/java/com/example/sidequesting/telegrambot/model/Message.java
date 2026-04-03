package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a Telegram Message. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
    @JsonProperty("message_id") long messageId,
    @JsonProperty("from") User from,
    @JsonProperty("chat") Chat chat,
    @JsonProperty("text") String text,
    @JsonProperty("date") long date) {}
