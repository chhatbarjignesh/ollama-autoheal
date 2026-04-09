package com.selfhealing.agent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OllamaClientTest {

    @Test
    public void testOllamaConnection() {
        OllamaClient client = new OllamaClient();
        assertTrue(client.isAvailable(), "Ollama should be running on localhost:11434");
    }

    @Test
    public void testSimpleChat() throws Exception {
        OllamaClient client = new OllamaClient();
        String response = client.chat("Say 'Hello, I am working!' and nothing else.");

        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("Ollama Response: " + response);
    }
}