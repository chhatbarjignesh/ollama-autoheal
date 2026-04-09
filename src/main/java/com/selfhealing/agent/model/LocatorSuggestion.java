package com.selfhealing.agent.model;

public class LocatorSuggestion {
    private String locator;
    private String locatorType; // "id", "xpath", "css", "name", "className", "tagName"
    private int confidence; // 0-100
    private String reasoning;
    private boolean validated;
    private int elementCount;

    public LocatorSuggestion() {
    }

    public LocatorSuggestion(String locator, String locatorType, int confidence, String reasoning) {
        this.locator = locator;
        this.locatorType = locatorType;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.validated = false;
        this.elementCount = 0;
    }

    // Getters and Setters
    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public void setLocatorType(String locatorType) {
        this.locatorType = locatorType;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public int getElementCount() {
        return elementCount;
    }

    public void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }

    @Override
    public String toString() {
        return "LocatorSuggestion{" +
                "locator='" + locator + '\'' +
                ", locatorType='" + locatorType + '\'' +
                ", confidence=" + confidence +
                ", reasoning='" + reasoning + '\'' +
                ", validated=" + validated +
                ", elementCount=" + elementCount +
                '}';
    }
}