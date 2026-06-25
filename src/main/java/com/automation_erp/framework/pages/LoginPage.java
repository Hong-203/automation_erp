package com.automation_erp.framework.pages;

import com.automation_erp.framework.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("btn-login");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToLoginPage() {
        driver.get(ConfigReader.getBaseUrl() + "/login");
    }

    public void login(String username, String password) {
        writeText(usernameField, username);
        writeText(passwordField, password);
        click(loginButton);
    }
}
