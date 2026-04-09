package com.selfhealing.agent.model;

import java.util.ArrayList;
import java.util.List;

public class SelfHealingResponse {
    private List<LocatorSuggestion> suggestions;
    private LocatorSuggestion bestSuggestion;
    private String originalLocator;
    private boolean healingSuccessful;

    public SelfHealingResponse() {
        this.suggestions = new ArrayList<>();
        this.healingSuccessful = false;
    }

    public void addSuggestion(LocatorSuggestion suggestion) {
        this.suggestions.add(suggestion);
    }

    public List<LocatorSuggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<LocatorSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public LocatorSuggestion getBestSuggestion() {
        return bestSuggestion;
    }

    public void setBestSuggestion(LocatorSuggestion bestSuggestion) {
        this.bestSuggestion = bestSuggestion;
    }

    public String getOriginalLocator() {
        return originalLocator;
    }

    public void setOriginalLocator(String originalLocator) {
        this.originalLocator = originalLocator;
    }

    public boolean isHealingSuccessful() {
        return healingSuccessful;
    }

    public void setHealingSuccessful(boolean healingSuccessful) {
        this.healingSuccessful = healingSuccessful;
    }

    @Override
    public String toString() {
        return "SelfHealingResponse{" +
                "originalLocator='" + originalLocator + '\'' +
                ", healingSuccessful=" + healingSuccessful +
                ", bestSuggestion=" + bestSuggestion +
                ", totalSuggestions=" + suggestions.size() +
                '}';
    }
}