package com.selfhealing.agent;

public class OllamaSimpleTest {

    public static void main(String[] args) {
        System.out.println("=== Testing Ollama Connection ===");

        OllamaClient client = new OllamaClient();

        // Test 1: Is available?
        System.out.println("\n1. Checking if Ollama is available...");
        boolean available = client.isAvailable();
        System.out.println("   Result: " + (available ? "✓ Available" : "✗ Not available"));

        if (!available) {
            System.out.println("   Ollama is not running. Start it with: ollama serve");
            return;
        }

        // Test 2: Simple chat
        System.out.println("\n2. Testing simple chat...");
        try {
            System.out.println("   Sending prompt: 'Say hello'");
            String response = client.chat("Say hello");
            System.out.println("   ✓ Success!");
            System.out.println("   Response: " + response);

        } catch (Exception e) {
            System.out.println("   ✗ Failed!");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 3: JSON response
        System.out.println("\n3. Testing JSON response...");
        try {
            String prompt = "Respond with ONLY this JSON, nothing else: {\"status\": \"working\"}";
            System.out.println("   Sending prompt...");
            String response = client.chat(prompt);
            System.out.println("   ✓ Success!");
            System.out.println("   Response: " + response);

        } catch (Exception e) {
            System.out.println("   ✗ Failed!");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Test Complete ===");
    }
}