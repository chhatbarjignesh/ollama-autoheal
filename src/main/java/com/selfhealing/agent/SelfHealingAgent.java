package com.selfhealing.agent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.selfhealing.agent.model.LocatorSuggestion;
import com.selfhealing.agent.model.SelfHealingResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SelfHealingAgent {
    private static final Logger logger = LoggerFactory.getLogger(SelfHealingAgent.class);
    private final OllamaClient ollamaClient;
    private final Gson gson;

    public SelfHealingAgent() {
        this.ollamaClient = new OllamaClient();
        this.gson = new Gson();
    }

    /**
     * Find alternative locators when the original locator fails
     *
     * @param driver Selenium WebDriver instance
     * @param failedLocator The locator that failed (e.g., "By.id: username")
     * @param elementDescription Human-readable description of the element
     * @return SelfHealingResponse with suggestions and best match
     */
    public SelfHealingResponse findAlternativeLocator(
            WebDriver driver,
            String failedLocator,
            String elementDescription) {

        logger.info("Starting self-healing for failed locator: {}", failedLocator);
        logger.info("Element description: {}", elementDescription);

        SelfHealingResponse response = new SelfHealingResponse();
        response.setOriginalLocator(failedLocator);

        try {
            // Get page source for context
            String pageSource = driver.getPageSource();
            String pageUrl = driver.getCurrentUrl();

            // Limit page source to avoid token limits (first 3000 chars)
            String limitedPageSource = pageSource.length() > 3000
                    ? pageSource.substring(0, 3000) + "..."
                    : pageSource;

            // Build prompt for LLM
            String prompt = buildPrompt(failedLocator, elementDescription, pageUrl, limitedPageSource);

            // Get suggestions from LLM
            String llmResponse = ollamaClient.chat(prompt);
            logger.debug("LLM Response: {}", llmResponse);

            // Parse LLM response
            List<LocatorSuggestion> suggestions = parseLLMResponse(llmResponse);

            // Validate suggestions against actual page
            List<LocatorSuggestion> validatedSuggestions = validateSuggestions(driver, suggestions);

            response.setSuggestions(validatedSuggestions);

            // Find best suggestion (validated + highest confidence)
            LocatorSuggestion best = findBestSuggestion(validatedSuggestions);

            if (best != null) {
                response.setBestSuggestion(best);
                response.setHealingSuccessful(true);
                logger.info("Self-healing successful! Best locator: {} (confidence: {}%)",
                        best.getLocator(), best.getConfidence());
            } else {
                response.setHealingSuccessful(false);
                logger.warn("Self-healing failed - no valid locator found");
            }

        } catch (Exception e) {
            logger.error("Error during self-healing", e);
            response.setHealingSuccessful(false);
        }

        return response;
    }

    /**
     * Build the prompt for the LLM
     */
    private String buildPrompt(String failedLocator, String elementDescription,
                               String pageUrl, String pageSource) {
        return String.format("""
            You are a Selenium test automation expert specializing in creating robust element locators.
            
            CONTEXT:
            - Page URL: %s
            - Failed Locator: %s
            - Element Description: %s
            
            PAGE HTML (partial):
            %s
            
            TASK:
            Suggest 3 alternative locators to find the element described above.
            Prioritize stable attributes like: id, name, data-testid, aria-label
            Avoid fragile locators like: nth-child, absolute XPath with many levels
            
            RESPOND ONLY WITH VALID JSON (no markdown, no code blocks):
            {
                "locators": [
                    {
                        "locator": "username",
                        "locatorType": "id",
                        "confidence": 95,
                        "reasoning": "Uses stable ID attribute"
                    },
                    {
                        "locator": "//input[@name='username']",
                        "locatorType": "xpath",
                        "confidence": 85,
                        "reasoning": "Uses name attribute as fallback"
                    }
                ]
            }
            
            Valid locatorType values: id, xpath, css, name, className, tagName, linkText, partialLinkText
            Confidence: 0-100 (higher = more stable/reliable)
            """, pageUrl, failedLocator, elementDescription, pageSource);
    }

    /**
     * Parse LLM JSON response into LocatorSuggestion objects
     */
    private List<LocatorSuggestion> parseLLMResponse(String llmResponse) {
        List<LocatorSuggestion> suggestions = new ArrayList<>();

        try {
            // Clean response - remove markdown code blocks if present
            String cleaned = llmResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            JsonObject json = gson.fromJson(cleaned, JsonObject.class);
            JsonArray locators = json.getAsJsonArray("locators");

            for (int i = 0; i < locators.size(); i++) {
                JsonObject loc = locators.get(i).getAsJsonObject();

                LocatorSuggestion suggestion = new LocatorSuggestion(
                        loc.get("locator").getAsString(),
                        loc.get("locatorType").getAsString(),
                        loc.get("confidence").getAsInt(),
                        loc.get("reasoning").getAsString()
                );

                suggestions.add(suggestion);
            }

        } catch (Exception e) {
            logger.error("Failed to parse LLM response", e);
        }

        return suggestions;
    }

    /**
     * Validate suggestions by trying them on the actual page
     */
    private List<LocatorSuggestion> validateSuggestions(WebDriver driver,
                                                        List<LocatorSuggestion> suggestions) {
        for (LocatorSuggestion suggestion : suggestions) {
            try {
                By by = convertToBy(suggestion.getLocatorType(), suggestion.getLocator());
                List<WebElement> elements = driver.findElements(by);

                if (!elements.isEmpty()) {
                    suggestion.setValidated(true);
                    suggestion.setElementCount(elements.size());

                    // Adjust confidence based on uniqueness
                    if (elements.size() == 1) {
                        suggestion.setConfidence(
                                Math.min(100, suggestion.getConfidence() + 10)
                        );
                    } else {
                        suggestion.setConfidence(
                                Math.max(0, suggestion.getConfidence() - 15)
                        );
                    }
                } else {
                    suggestion.setValidated(false);
                }

            } catch (NoSuchElementException | IllegalArgumentException e) {
                suggestion.setValidated(false);
                logger.debug("Locator validation failed: {}", suggestion.getLocator());
            }
        }

        return suggestions;
    }

    /**
     * Convert locator string to Selenium By object
     */
    private By convertToBy(String locatorType, String locator) {
        return switch (locatorType.toLowerCase()) {
            case "id" -> By.id(locator);
            case "xpath" -> By.xpath(locator);
            case "css", "cssselector" -> By.cssSelector(locator);
            case "name" -> By.name(locator);
            case "classname" -> By.className(locator);
            case "tagname" -> By.tagName(locator);
            case "linktext" -> By.linkText(locator);
            case "partiallinktext" -> By.partialLinkText(locator);
            default -> throw new IllegalArgumentException("Unknown locator type: " + locatorType);
        };
    }

    /**
     * Find the best suggestion (validated + highest confidence)
     */
    private LocatorSuggestion findBestSuggestion(List<LocatorSuggestion> suggestions) {
        return suggestions.stream()
                .filter(LocatorSuggestion::isValidated)
                .max(Comparator.comparingInt(LocatorSuggestion::getConfidence))
                .orElse(null);
    }

    /**
     * Convert LocatorSuggestion to Selenium By object
     */
    public By toBy(LocatorSuggestion suggestion) {
        return convertToBy(suggestion.getLocatorType(), suggestion.getLocator());
    }
}