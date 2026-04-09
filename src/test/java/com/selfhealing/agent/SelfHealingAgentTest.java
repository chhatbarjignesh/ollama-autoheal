package com.selfhealing.agent;

import com.selfhealing.agent.model.SelfHealingResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class SelfHealingAgentTest {

    private WebDriver driver;
    private SelfHealingAgent agent;

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--headless"); // Run headless for faster testing

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        agent = new SelfHealingAgent();
    }

    @Test
    public void testSelfHealingWithRealPage() {
        System.out.println("\n=== Testing Self-Healing Agent ===\n");

        // Navigate to test page
        driver.get("https://practicetestautomation.com/practice-test-login/");

        System.out.println("Page loaded: " + driver.getTitle());
        System.out.println("Current URL: " + driver.getCurrentUrl());

        // Simulate a broken locator
        String brokenLocator = "By.id: usernameWRONG";
        String elementDescription = "Username input field - should accept text input for login";

        System.out.println("\nAttempting self-healing for locator: " + brokenLocator);
        System.out.println("Element description: " + elementDescription);
        System.out.println("\nThis may take 10-30 seconds on first run...\n");

        long startTime = System.currentTimeMillis();

        // Trigger self-healing
        SelfHealingResponse response = agent.findAlternativeLocator(
                driver,
                brokenLocator,
                elementDescription
        );

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n=== Results ===");
        System.out.println("Time taken: " + duration + " ms");
        System.out.println("Healing successful: " + response.isHealingSuccessful());

        if (response.isHealingSuccessful()) {
            System.out.println("\n✓ Best Suggestion:");
            System.out.println("  Locator: " + response.getBestSuggestion().getLocator());
            System.out.println("  Type: " + response.getBestSuggestion().getLocatorType());
            System.out.println("  Confidence: " + response.getBestSuggestion().getConfidence() + "%");
            System.out.println("  Reasoning: " + response.getBestSuggestion().getReasoning());
            System.out.println("  Validated: " + response.getBestSuggestion().isValidated());
            System.out.println("  Elements found: " + response.getBestSuggestion().getElementCount());
        } else {
            System.out.println("\n✗ No valid locator found");
        }

        System.out.println("\nAll Suggestions (" + response.getSuggestions().size() + " total):");
        response.getSuggestions().forEach(suggestion -> {
            System.out.println("  - " + suggestion.getLocator() +
                    " (" + suggestion.getLocatorType() + ") " +
                    "Confidence: " + suggestion.getConfidence() + "% " +
                    "Validated: " + (suggestion.isValidated() ? "✓" : "✗"));
        });

        System.out.println("\n=== Test Complete ===\n");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}