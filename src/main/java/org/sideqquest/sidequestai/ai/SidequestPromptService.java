package org.sideqquest.sidequestai.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SidequestPromptService {

    private static final String TEMPLATE_PATH = "prompts/sidequest-prompt.txt";

    private final AnthropicHaikuClient anthropicHaikuClient;

    public SidequestPromptService(AnthropicHaikuClient anthropicHaikuClient) {
        this.anthropicHaikuClient = anthropicHaikuClient;
    }

    public String generateSidequest(
            String location,
            String difficulty,
            String language
    ) {
        validate(location, "location");
        validate(difficulty, "difficulty");
        validate(language, "language");

        String template = loadTemplate();
        String prompt = fillTemplate(template, Map.of(
                "location", location.trim(),
                "difficulty", difficulty.trim(),
                "language", language.trim()
        ));

        return anthropicHaikuClient.ask(prompt, 900);
    }

    private static void validate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private String loadTemplate() {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load prompt template: " + TEMPLATE_PATH, e);
        }
    }

    private String fillTemplate(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
