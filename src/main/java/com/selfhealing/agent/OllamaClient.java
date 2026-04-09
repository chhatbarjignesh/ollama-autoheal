package com.selfhealing.agent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OllamaClient {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen2.5-coder:1.5b";

    private final OkHttpClient client;
    private final Gson gson;

    public OllamaClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Send a prompt to Ollama and get response
     * @param prompt The prompt to send
     * @return The LLM response as string
     */
    public String chat(String prompt) throws IOException {
        logger.info("=== Starting Ollama Chat Request ===");
        logger.info("Prompt length: {} characters", prompt.length());

        // Build message
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        // Build messages array
        JsonArray messagesArray = new JsonArray();
        messagesArray.add(message);

        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("stream", false);

        String jsonBody = requestBody.toString();
        logger.info("Request body: {}", jsonBody.substring(0, Math.min(500, jsonBody.length())) + "...");

        // Create HTTP request
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(body)
                .build();

        logger.info("Sending request to: {}", OLLAMA_URL);
        logger.info("Waiting for response...");

        long startTime = System.currentTimeMillis();

        try (Response response = client.newCall(request).execute()) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Response received in {} ms", duration);
            logger.info("Response code: {}", response.code());

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                logger.error("Error response: {}", errorBody);
                throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            logger.info("Response body length: {} characters", responseBody.length());
            logger.debug("Full response: {}", responseBody);

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Extract message content
            String content = jsonResponse
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            logger.info("Extracted content length: {} characters", content.length());
            logger.info("=== Ollama Chat Request Complete ===");

            return content;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Request failed after {} ms", duration);
            logger.error("Error details: ", e);
            throw e;
        }
    }
    /**
     * Test connection to Ollama
     * @return true if Ollama is reachable
     */
    public boolean isAvailable() {
        try {
            Request request = new Request.Builder()
                    .url("http://localhost:11434/api/tags")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            logger.error("Ollama is not available", e);
            return false;
        }
    }
}