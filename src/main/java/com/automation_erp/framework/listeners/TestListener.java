package com.automation_erp.framework.listeners;

import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.driver.DriverManager;
import com.automation_erp.framework.pages.BasePage;
import com.automation_erp.framework.reporters.ExtentReportManager;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.ByteArrayInputStream;
import io.qameta.allure.Allure;

/**
 * TestNG Listener kết nối kết quả test với ExtentReports.
 * Tự động chụp ảnh màn hình khi test UI thất bại và đính kèm vào báo cáo.
 *
 * Đăng ký listener trong testng.xml:
 *   <listeners>
 *     <listener class-name="com.automation_erp.framework.listeners.TestListener"/>
 *   </listeners>
 *
 * Hoặc dùng annotation trên test class:
 *   @Listeners(TestListener.class)
 */
public class TestListener implements ITestListener {

    // =====================================================================
    // Suite lifecycle
    // =====================================================================

    @Override
    public void onStart(ITestContext context) {
        System.out.println("\n========== BẮT ĐẦU TEST SUITE: " + context.getName() + " ==========");
        // Khởi tạo ExtentReports singleton
        ExtentReportManager.getInstance();
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("\n========== KẾT THÚC TEST SUITE: " + context.getName() + " ==========");
        System.out.printf("   ✅ Passed : %d%n", context.getPassedTests().size());
        System.out.printf("   ❌ Failed : %d%n", context.getFailedTests().size());
        System.out.printf("   ⏩ Skipped: %d%n", context.getSkippedTests().size());
        // Ghi report ra file HTML
        ExtentReportManager.flush();
    }

    // =====================================================================
    // Test lifecycle
    // =====================================================================

    @Override
    public void onTestStart(ITestResult result) {
        String testName  = result.getMethod().getMethodName();
        String testDesc  = result.getMethod().getDescription();
        String className = result.getTestClass().getName();

        System.out.printf("%n--- [START] %s.%s ---%n", className, testName);

        // Tạo ExtentTest node với mô tả nếu có
        ExtentTest test = (testDesc != null && !testDesc.isEmpty())
            ? ExtentReportManager.createTest(testName, testDesc)
            : ExtentReportManager.createTest(testName);

        test.assignCategory(className);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration   = result.getEndMillis() - result.getStartMillis();

        System.out.printf("--- [✅ PASS] %s (%.2fs) ---%n", testName, duration / 1000.0);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, String.format("PASSED ✅ (%.2fs)", duration / 1000.0));
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        long duration       = result.getEndMillis() - result.getStartMillis();

        System.out.printf("--- [❌ FAIL] %s (%.2fs) ---%n", testName, duration / 1000.0);
        if (throwable != null) {
            System.out.println("   Lý do: " + throwable.getMessage());
        }

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, String.format("FAILED ❌ (%.2fs)", duration / 1000.0));
            if (throwable != null) {
                test.fail(throwable);
            }

            // Chụp ảnh màn hình khi test UI thất bại
            captureAndAttachScreenshot(test, testName);
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();

        System.out.printf("--- [⏩ SKIP] %s ---%n", testName);
        if (throwable != null) {
            System.out.println("   Lý do skip: " + throwable.getMessage());
        }

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "SKIPPED ⏩");
            if (throwable != null) {
                test.skip(throwable);
            }
        }
        ExtentReportManager.removeTest();
    }

    // =====================================================================
    // Private: Screenshot helper
    // =====================================================================

    /**
     * Chụp ảnh màn hình và đính kèm vào ExtentReport node.
     * Chỉ thực hiện khi đang ở chế độ UI (execution.type=UI).
     *
     * @param test     ExtentTest node hiện tại
     * @param testName Tên test case (để đặt tên file ảnh)
     */
    private void captureAndAttachScreenshot(ExtentTest test, String testName) {
        try {
            // Kiểm tra cấu hình bật/tắt chụp màn hình từ config
            String screenshotOnFailureStr = ConfigReader.getProperty("ui.screenshot.on.failure");
            boolean isScreenshotEnabled = screenshotOnFailureStr == null || Boolean.parseBoolean(screenshotOnFailureStr.trim());
            if (!isScreenshotEnabled) {
                System.out.println("[TestListener] Bỏ qua chụp ảnh màn hình do ui.screenshot.on.failure = false");
                return;
            }

            String executionType = ConfigReader.getExecutionType();
            if (!"UI".equalsIgnoreCase(executionType)) {
                return; // Chỉ chụp ảnh khi chạy UI test
            }

            WebDriver driver = DriverManager.getDriver();
            if (driver == null) {
                return; // Driver đã bị đóng hoặc chưa khởi tạo
            }

            // Chụp ảnh dạng byte array
            byte[] screenshotBytes = BasePage.takeScreenshotAsBytes(driver);
            if (screenshotBytes == null) {
                return;
            }

            // Lưu file ảnh ra đĩa (để CI/CD có thể upload artifact)
            String timestamp = String.valueOf(System.currentTimeMillis());
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String screenshotDir = "target/screenshots/";
            String fileName = safeTestName + "_" + timestamp + ".png";
            File destFile = new File(screenshotDir + fileName);
            destFile.getParentFile().mkdirs();
            java.nio.file.Files.write(destFile.toPath(), screenshotBytes);

            System.out.printf("[TestListener] Screenshot đã lưu: %s%n", destFile.getAbsolutePath());

            // Đính kèm ảnh vào ExtentReport dạng Base64 inline
            String base64 = java.util.Base64.getEncoder().encodeToString(screenshotBytes);
            test.fail("📸 Screenshot lúc thất bại:",
                MediaEntityBuilder.createScreenCaptureFromBase64String(base64, testName).build());

            // Đính kèm ảnh vào Allure Report
            Allure.addAttachment("Screenshot lúc thất bại - " + testName, new ByteArrayInputStream(screenshotBytes));

        } catch (Exception e) {
            System.err.printf("[TestListener] Không thể chụp ảnh màn hình cho test '%s': %s%n",
                testName, e.getMessage());
        }
    }
}
