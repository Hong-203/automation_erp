package com.automation_erp.framework.listeners;

import com.automation_erp.framework.reporters.ExtentReportManager;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Listener kết nối kết quả test với ExtentReports.
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
}
