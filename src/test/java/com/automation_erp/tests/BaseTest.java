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
        System.out.println("========== BẮT ĐẦU TEST CASE (Execution Type: " + executionType + ") ==========");

        if ("UI".equals(executionType)) {
            System.out.println("[BaseTest] Đang khởi tạo trình duyệt: " + ConfigReader.getBrowser());
            WebDriver driver = DriverFactory.createDriverInstance();
            DriverManager.setDriver(driver);
        } else {
            System.out.println("[BaseTest] Bỏ qua khởi tạo trình duyệt (Chạy API mode).");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        String executionType = ConfigReader.getExecutionType().toUpperCase();
        if ("UI".equals(executionType)) {
            System.out.println("[BaseTest] Đang đóng trình duyệt...");
            DriverManager.quitDriver();
        }
        System.out.println("========== KẾT THÚC TEST CASE ==========\n");
    }
}
