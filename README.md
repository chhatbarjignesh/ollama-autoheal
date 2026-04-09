# Ollama-Autoheal: Self-Healing Test Automation 🚀

`ollama-autoheal` is a Java-based self-healing test automation framework that leverages a local LLM (Ollama) to automatically repair broken Selenium locators at runtime. When a test fails to find an element, the framework analyzes the current page source and element context to suggest and validate new locators dynamically.

## 🌟 Key Features

- **LLM-Powered Recovery:** Uses a local LLM (default: `qwen2.5-coder:1.5b`) to understand element context and suggest stable locators.
- **Runtime Validation:** All suggested locators are automatically validated against the live DOM before being applied to the test.
- **Easy Integration:** Seamlessly integrates with existing Page Object Models (POM) by extending `SelfHealingBasePage`.
- **Hybrid Strategy:** Combines AI-driven suggestions with traditional Selenium validation for high reliability.
- **Context-Aware:** Sends partial page source and element descriptions to the LLM to ensure accurate healing.

## 🏗️ Architectural Overview

1.  **Failure Detection:** Intercepts `NoSuchElementException` during test execution.
2.  **Context Gathering:** Captures the current URL and the first 3000 characters of the page source.
3.  **LLM Request:** Sends a structured prompt to the local Ollama API with element metadata and page context.
4.  **Locator Generation:** The LLM suggests multiple alternative locators (ID, XPath, CSS, etc.) with confidence scores.
5.  **Validation:** The `SelfHealingAgent` attempts to find the element using each suggestion on the live page.
6.  **Self-Correction:** If a valid replacement is found, it is applied immediately, and the test continues without failing.

## 📋 Prerequisites

- **Java 17** or higher
- **Maven** 3.6+
- **Ollama** running locally on port `11434`
- **Ollama Model:** `qwen2.5-coder:1.5b` (or any other coder-specific model)

To pull the default model:
```bash
ollama pull qwen2.5-coder:1.5b
```

## 🚀 Getting Started

### 1. Installation

Clone the repository and install dependencies:
```bash
git clone https://github.com/your-username/ollama-autoheal.git
cd ollama-autoheal
mvn install
```

### 2. Usage in Page Objects

Extend `SelfHealingBasePage` and use the `*WithHealing` methods for critical elements:

```java
public class LoginPage extends SelfHealingBasePage {

    private By usernameField = By.id("username");
    private By loginButton = By.xpath("//button[@type='submit']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void login(String username, String password) {
        // This will automatically heal if the ID "username" changes!
        sendKeysWithHealing(
                usernameField, 
                "Username input field on login page", 
                username
        );
        
        clickWithHealing(
                loginButton, 
                "Submit/Login button on login page"
        );
    }
}
```

### 3. Running Tests

You can run the included integration tests to see self-healing in action:
```bash
mvn test -Dtest=SelfHealingAgentTest
```

## ⚙️ Configuration

The core settings are located in `OllamaClient.java`:

- `OLLAMA_URL`: Default is `http://localhost:11434/api/chat`
- `MODEL`: Default is `qwen2.5-coder:1.5b`

The healing logic can be toggled in your page objects:
```java
setSelfHealingEnabled(true); // Default
```

## 🛠️ Project Structure

- `com.selfhealing.agent`: Core logic for LLM communication and locator validation.
- `com.selfhealing.agent.base`: Base classes for Selenium integration.
- `com.selfhealing.agent.model`: Data models for LLM responses and suggestions.
- `com.selfhealing.pages`: Example Page Objects demonstrating the healing capabilities.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---
*Built with ❤️ for better test automation.*
