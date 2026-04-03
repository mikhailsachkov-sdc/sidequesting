package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a Telegram Chat. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(
    @JsonProperty("id") long id,
    @JsonProperty("type") String type, // "private", "group", "supergroup", "channel"
    @JsonProperty("title") String title,
    @JsonProperty("username") String username,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName) {}
