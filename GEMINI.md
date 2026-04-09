# GEMINI.md - ollama-autoheal

## Project Overview
`ollama-autoheal` is a Java-based self-healing test automation framework that uses a local LLM (Ollama) to automatically repair broken Selenium locators at runtime. When a test fails to find an element, the `SelfHealingAgent` analyzes the current page source and element context to suggest and validate new locators.

## Tech Stack
- **Language:** Java 17
- **Build Tool:** Maven
- **Core Library:** Selenium 4.16.1
- **LLM Integration:** Ollama (default model: `qwen2.5-coder:1.5b`)
- **JSON Processing:** Gson
- **HTTP Client:** OkHttp
- **Testing:** JUnit 5 & TestNG

## Key Components
- `SelfHealingAgent.java`: The core engine that orchestrates the self-healing process.
- `OllamaClient.java`: Handles communication with the local Ollama API (default: `http://localhost:11434`).
- `SelfHealingBasePage.java`: Base class for Page Objects that provides self-healing capabilities.
- `LocatorSuggestion.java`: Model representing a potential fix with confidence scores.

## Architectural Patterns
- **Page Object Model (POM):** Standard UI automation pattern.
- **Self-Healing Wrapper:** Uses `SelfHealingBasePage` to intercept `NoSuchElementException` and trigger the healing agent.
- **LLM-Based Recovery:** Offloads locator discovery to an LLM instead of using static heuristics.
- **Runtime Validation:** All LLM-suggested locators are validated against the live DOM before being applied.

## Development Standards
- **Java Version:** Use Java 17+ features.
- **Error Handling:** Always wrap self-healing logic in try-catch blocks to ensure that even if healing fails, the original exception is preserved or logged correctly.
- **LLM Prompts:** Prompts are located in `SelfHealingAgent#buildPrompt`. Any changes to locator strategy should be updated there.
- **Page Source Optimization:** Currently, the first 3000 characters of page source are sent to the LLM. Monitor token limits if using larger models.
- **Testing:** Add unit tests for `OllamaClient` and integration tests for `SelfHealingAgent` when modifying core logic.

## Environment Requirements
- **Ollama:** Must be running locally on port 11434.
- **Model:** `qwen2.5-coder:1.5b` should be pulled (`ollama pull qwen2.5-coder:1.5b`).
