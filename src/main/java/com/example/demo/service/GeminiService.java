package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
@Service
public class GeminiService {
     private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String apiKey = System.getenv("GEMINI_API_KEY");

    private final String apiUrl =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public String generateResponse(String userMessage) {

        if (apiKey == null || apiKey.isBlank()) {
            return "Gemini API key is not configured.";
        }

        try {

            String prompt = """
                    You are EduAI, a friendly educational AI assistant for students.

                    Answer the student's question in a simple and easy-to-understand way.

                    Rules:
                    - Explain concepts clearly.
                    - Use simple language.
                    - Give examples when useful.
                    - Use bullet points when appropriate.
                    - If the question is about programming, explain it clearly and give a simple example.
                    - Be encouraging and educational.

                    Student's question:
                    """ + userMessage;

            // Create Gemini request body
            String requestBody = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": %s
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(
                    objectMapper.writeValueAsString(prompt)
            );

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send request to Gemini
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            // Check response
            if (response.statusCode() != 200) {

                System.out.println("Gemini API Error:");
                System.out.println(response.body());

                return "Sorry, I couldn't get a response from Gemini.";
            }

            // Convert JSON response
            JsonNode jsonResponse =
                    objectMapper.readTree(response.body());

            // Extract AI response
            JsonNode textNode =
                    jsonResponse
                            .path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text");

            if (textNode.isMissingNode()) {
                return "Sorry, I couldn't understand the Gemini response.";
            }

            return textNode.asText();

        } catch (Exception e) {

            e.printStackTrace();

            return "Sorry, something went wrong while connecting to Gemini.";
        }
    }
}
