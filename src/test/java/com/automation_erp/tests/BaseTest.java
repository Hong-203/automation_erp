package com.automation_erp.tests;

import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.driver.DriverFactory;
import com.automation_erp.framework.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    @BeforeMethod
    public void setUp() {
        String executionType = ConfigReader.getExecutionType().toUpperCase();
        System.out.println("========== BAT DAU TEST CASE (Execution Type: " + executionType + ") ==========");

        if ("UI".equals(executionType)) {
            System.out.println("[BaseTest] Dang khoi tao trinh duyet: " + ConfigReader.getBrowser());
            WebDriver driver = DriverFactory.createDriverInstance();
            DriverManager.setDriver(driver);
        } else {
            System.out.println("[BaseTest] Bo qua khoi tao trinh duyet (Chay API mode).");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        String executionType = ConfigReader.getExecutionType().toUpperCase();
        if ("UI".equals(executionType)) {
            System.out.println("[BaseTest] Dang dong trinh duyet...");
            DriverManager.quitDriver();
        }
        System.out.println("========== KET THUC TEST CASE ==========\n");
    }
}
