package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a Telegram User. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
    @JsonProperty("id") long id,
    @JsonProperty("is_bot") boolean isBot,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("username") String username,
    @JsonProperty("language_code") String languageCode) {}
