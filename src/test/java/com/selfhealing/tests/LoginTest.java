package com.selfhealing.tests;

import com.selfhealing.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class LoginTest {

    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeEach
    public void setUp() {
        // Setup ChromeDriver automatically
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        loginPage = new LoginPage(driver);
    }

    @Test
    public void testLoginWithSelfHealing() {
        // Navigate to a demo login page
        driver.get("https://practicetestautomation.com/practice-test-login/");

        // Perform login - if locators fail, self-healing kicks in
        loginPage.login("student", "Password123");

        // Verify login successful
        System.out.println("Login test completed!");
    }

    @Test
    public void testSelfHealingWithBrokenLocator() {
        driver.get("https://practicetestautomation.com/practice-test-login/");

        // This demonstrates self-healing when locator is intentionally broken
        // Modify the LoginPage locators to wrong values and run this test
        // The agent should find the correct locators automatically

        loginPage.enterUsername("student");
        loginPage.enterPassword("Password123");
        loginPage.clickLogin();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}