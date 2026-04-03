package com.example.sidequesting.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/** Service for generating sidequests using Spring AI. */
@Service
public class SidequestPromptService {

  private final ChatClient chatClient;
  private final Resource templateResource;

  /**
   * Constructs the service with a chat client builder and a template resource.
   *
   * @param chatClientBuilder the builder for the chat client
   * @param templateResource the resource containing the sidequest prompt template
   */
  public SidequestPromptService(
      ChatClient.Builder chatClientBuilder,
      @Value("classpath:prompts/sidequest-prompt.txt") Resource templateResource) {
    this.chatClient = chatClientBuilder.build();
    this.templateResource = templateResource;
  }

  /**
   * Generates a sidequest based on the provided parameters.
   *
   * @param location where the sidequest takes place
   * @param difficulty how hard the sidequest should be
   * @param language the language to use for generation
   * @return the generated sidequest text
   */
  public String generateSidequest(String location, String difficulty, String language) {
    validate(location, "location");
    validate(difficulty, "difficulty");
    validate(language, "language");

    return chatClient
        .prompt()
        .user(
            u ->
                u.text(templateResource)
                    .param("location", location.trim())
                    .param("difficulty", difficulty.trim())
                    .param("language", language.trim()))
        .call()
        .content();
  }

  private static void validate(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
  }
}
