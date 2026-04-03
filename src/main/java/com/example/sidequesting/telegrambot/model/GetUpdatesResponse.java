package com.example.sidequesting.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Wrapper returned by the Telegram getUpdates endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GetUpdatesResponse(
    @JsonProperty("ok") boolean ok, @JsonProperty("result") List<Update> result) {}
