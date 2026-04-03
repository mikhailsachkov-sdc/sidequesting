package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an incoming Telegram update. Each update contains at most one optional field with the
 * actual payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Update(
    @JsonProperty("update_id") long updateId,
    @JsonProperty("message") Message message,
    @JsonProperty("callback_query") CallbackQuery callbackQuery) {}
