package com.selfhealing.agent.base;

import com.selfhealing.agent.SelfHealingAgent;
import com.selfhealing.agent.model.LocatorSuggestion;
import com.selfhealing.agent.model.SelfHealingResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SelfHealingBasePage {
    private static final Logger logger = LoggerFactory.getLogger(SelfHealingBasePage.class);
    protected WebDriver driver;
    private final SelfHealingAgent agent;
    private boolean selfHealingEnabled;

    public SelfHealingBasePage(WebDriver driver) {
        this.driver = driver;
        this.agent = new SelfHealingAgent();
        this.selfHealingEnabled = true; // Can be configured
    }

    /**
     * Find element with self-healing capability
     *
     * @param by Original locator
     * @param elementDescription Human-readable description for LLM
     * @return WebElement
     */
    protected WebElement findElementWithHealing(By by, String elementDescription) {
        try {
            // Try original locator first
            return driver.findElement(by);

        } catch (NoSuchElementException e) {

            if (!selfHealingEnabled) {
                logger.warn("Self-healing disabled. Original locator failed: {}", by);
                throw e;
            }

            logger.warn("Element not found with locator: {}. Attempting self-healing...", by);

            // Trigger self-healing
            SelfHealingResponse response = agent.findAlternativeLocator(
                    driver,
                    by.toString(),
                    elementDescription
            );

            if (response.isHealingSuccessful()) {
                LocatorSuggestion best = response.getBestSuggestion();

                logger.info("✓ Self-healing successful!");
                logger.info("  Original: {}", by);
                logger.info("  New: {} (Type: {}, Confidence: {}%)",
                        best.getLocator(),
                        best.getLocatorType(),
                        best.getConfidence());
                logger.info("  Reasoning: {}", best.getReasoning());

                // Log all suggestions for review
                logAllSuggestions(response);

                // TODO: Save to repository for permanent fix
                // saveToRepository(by, best);

                // Use new locator
                By newBy = agent.toBy(best);
                return driver.findElement(newBy);

            } else {
                logger.error("✗ Self-healing failed. No alternative locator found.");
                throw new NoSuchElementException(
                        "Element not found and self-healing failed for: " + by
                );
            }
        }
    }

    /**
     * Find elements with self-healing (returns list)
     */
    protected List<WebElement> findElementsWithHealing(By by, String elementDescription) {
        try {
            List<WebElement> elements = driver.findElements(by);

            if (elements.isEmpty() && selfHealingEnabled) {
                logger.warn("No elements found with locator: {}. Attempting self-healing...", by);

                SelfHealingResponse response = agent.findAlternativeLocator(
                        driver,
                        by.toString(),
                        elementDescription
                );

                if (response.isHealingSuccessful()) {
                    LocatorSuggestion best = response.getBestSuggestion();
                    logger.info("✓ Self-healing successful for multiple elements");

                    By newBy = agent.toBy(best);
                    return driver.findElements(newBy);
                }
            }

            return elements;

        } catch (Exception e) {
            logger.error("Error finding elements", e);
            throw e;
        }
    }

    /**
     * Click element with self-healing
     */
    protected void clickWithHealing(By by, String elementDescription) {
        WebElement element = findElementWithHealing(by, elementDescription);
        element.click();
        logger.debug("Clicked element: {}", elementDescription);
    }

    /**
     * Send keys with self-healing
     */
    protected void sendKeysWithHealing(By by, String elementDescription, String text) {
        WebElement element = findElementWithHealing(by, elementDescription);
        element.clear();
        element.sendKeys(text);
        logger.debug("Entered text into: {}", elementDescription);
    }

    /**
     * Get text with self-healing
     */
    protected String getTextWithHealing(By by, String elementDescription) {
        WebElement element = findElementWithHealing(by, elementDescription);
        return element.getText();
    }

    /**
     * Check if element exists with self-healing
     */
    protected boolean isElementPresentWithHealing(By by, String elementDescription) {
        try {
            findElementWithHealing(by, elementDescription);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Enable/disable self-healing
     */
    public void setSelfHealingEnabled(boolean enabled) {
        this.selfHealingEnabled = enabled;
        logger.info("Self-healing {}", enabled ? "ENABLED" : "DISABLED");
    }

    /**
     * Log all suggestions for debugging/review
     */
    private void logAllSuggestions(SelfHealingResponse response) {
        logger.info("All suggestions ({} total):", response.getSuggestions().size());

        for (int i = 0; i < response.getSuggestions().size(); i++) {
            LocatorSuggestion suggestion = response.getSuggestions().get(i);
            logger.info("  {}. {} (Type: {}, Confidence: {}%, Validated: {})",
                    i + 1,
                    suggestion.getLocator(),
                    suggestion.getLocatorType(),
                    suggestion.getConfidence(),
                    suggestion.isValidated() ? "✓" : "✗"
            );
        }
    }
}