package com.pcmtexas.actiontracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pcmtexas.actiontracker.dto.BulkCreateRequest.ExtractedTaskItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ClaudeService {

    @Value("${app.claude.api-key}")
    private String apiKey;

    @Value("${app.claude.model}")
    private String model;

    @Value("${app.claude.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            You are an action item extraction assistant. Given meeting notes, emails, or any text content,
            extract actionable tasks from the text. For each task, identify:
            - title: a concise task title (required)
            - description: more detail about what needs to be done (optional)
            - assignee: the full name of the person responsible (required if mentioned)
            - dueDate: due date in ISO format YYYY-MM-DD (optional, null if not specified)
            - priority: HIGH, MEDIUM, or LOW (default to MEDIUM if not clear)
            - source: a brief label for where this task came from (e.g., meeting name, email subject)
            - sourceUrl: any URL mentioned in context (optional, null if none)

            Return ONLY a valid JSON array of task objects with these exact fields.
            If a field is not applicable, use null.
            Do not include any explanation or markdown - just the raw JSON array.

            Example output format:
            [
              {
                "title": "Prepare quarterly report",
                "description": "Compile Q3 metrics and create slides for board meeting",
                "assignee": "John Smith",
                "dueDate": "2024-03-15",
                "priority": "HIGH",
                "source": "Board Meeting Notes",
                "sourceUrl": null
              }
            ]
            """;

    public List<ExtractedTaskItem> extractTasks(String notes) {
        log.info("Extracting tasks from notes using Claude model: {}", model);

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 4096);
            requestBody.put("system", SYSTEM_PROMPT);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", "Please extract action items from the following text:\n\n" + notes);
            messages.add(userMessage);
            requestBody.set("messages", messages);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/messages"))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Claude API returned status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Claude API error: HTTP " + response.statusCode() + " - " + response.body());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            JsonNode contentArray = responseJson.path("content");

            if (contentArray.isEmpty()) {
                log.warn("Claude API returned empty content array");
                return new ArrayList<>();
            }

            String extractedText = contentArray.get(0).path("text").asText();
            log.debug("Claude response text: {}", extractedText);

            // Strip potential markdown code fences
            String cleanJson = extractedText.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode tasksArray = objectMapper.readTree(cleanJson);
            List<ExtractedTaskItem> result = new ArrayList<>();

            for (JsonNode taskNode : tasksArray) {
                ExtractedTaskItem item = new ExtractedTaskItem();
                item.setTitle(getTextOrNull(taskNode, "title"));
                item.setDescription(getTextOrNull(taskNode, "description"));
                item.setAssignee(getTextOrNull(taskNode, "assignee"));
                item.setAssigneeEmail(null); // Will be resolved by the frontend or admin
                item.setDueDate(getTextOrNull(taskNode, "dueDate"));
                item.setPriority(getTextOrNull(taskNode, "priority") != null
                        ? getTextOrNull(taskNode, "priority")
                        : "MEDIUM");
                item.setSource(getTextOrNull(taskNode, "source"));
                item.setSourceUrl(getTextOrNull(taskNode, "sourceUrl"));

                if (item.getTitle() != null && !item.getTitle().isBlank()) {
                    result.add(item);
                }
            }

            log.info("Successfully extracted {} tasks from notes", result.size());
            return result;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract tasks from Claude API", e);
            throw new RuntimeException("Failed to extract tasks: " + e.getMessage(), e);
        }
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asText();
        return (text == null || text.isBlank() || "null".equalsIgnoreCase(text)) ? null : text;
    }
}
