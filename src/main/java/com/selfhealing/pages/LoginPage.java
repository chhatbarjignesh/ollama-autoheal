package com.selfhealing.pages;

import com.selfhealing.agent.base.SelfHealingBasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends SelfHealingBasePage {

    // Your original locators (keep them as-is)
    private By usernameField = By.id("username");
    private By passwordField = By.id("password");
    private By loginButton = By.xpath("//button[@type='submit']");
    private By errorMessage = By.cssSelector(".error-message");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Old way (without self-healing) - still works
     */
    public void enterUsernameOldWay(String username) {
        driver.findElement(usernameField).sendKeys(username);
    }

    /**
     * New way (with self-healing) - recommended
     */
    public void enterUsername(String username) {
        sendKeysWithHealing(
                usernameField,
                "Username input field on login page",
                username
        );
    }

    public void enterPassword(String password) {
        sendKeysWithHealing(
                passwordField,
                "Password input field on login page",
                password
        );
    }

    public void clickLogin() {
        clickWithHealing(
                loginButton,
                "Submit/Login button on login page"
        );
    }

    public String getErrorMessage() {
        return getTextWithHealing(
                errorMessage,
                "Error message displayed after failed login"
        );
    }

    public boolean isLoginButtonVisible() {
        return isElementPresentWithHealing(
                loginButton,
                "Submit/Login button"
        );
    }

    /**
     * Complete login flow
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }
}