package com.automation_erp.framework.reporters;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Quản lý ExtentReports v5 theo Singleton pattern.
 *
 * Cách dùng trong TestListener:
 * ExtentTest test = ExtentReportManager.createTest("Tên test case");
 * test.pass("Bước 1 thành công");
 * test.fail("Bước 2 thất bại: " + message);
 * // Gọi flush() khi kết thúc toàn bộ suite
 * ExtentReportManager.flush();
 *
 * Output: target/extent-reports/ExtentReport.html
 */
public class ExtentReportManager {

    private static ExtentReports extentReports;

    // ThreadLocal để mỗi thread test có ExtentTest riêng (parallel-safe)
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();

    private ExtentReportManager() {
    }

    // =====================================================================
    // Khởi tạo ExtentReports (gọi 1 lần khi suite bắt đầu)
    // =====================================================================

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            String reportPath = "target/extent-reports/ExtentReport.html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("ERP Automation Test Report");
            sparkReporter.config().setReportName("Warehouse Management - Module 2");
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setEncoding("UTF-8");
            sparkReporter.config().setTimeStampFormat("HH:mm:ss dd/MM/yyyy");

            String customCss = "body { font-family: 'Inter', 'Segoe UI', Tahoma, sans-serif !important; background: linear-gradient(135deg, #1e1e2f 0%, #252542 100%) !important; }"
                    + ".header { background-color: rgba(30, 30, 47, 0.9) !important; backdrop-filter: blur(10px); border-bottom: 1px solid rgba(255, 255, 255, 0.05); }"
                    + ".card { background: rgba(40, 40, 60, 0.6) !important; border-radius: 16px !important; border: 1px solid rgba(255, 255, 255, 0.05); box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3); backdrop-filter: blur(8px); }"
                    + ".badge-success { background: linear-gradient(135deg, #11998e, #38ef7d) !important; box-shadow: 0 4px 15px rgba(56, 239, 125, 0.3); }"
                    + ".badge-danger { background: linear-gradient(135deg, #ff416c, #ff4b2b) !important; box-shadow: 0 4px 15px rgba(255, 75, 43, 0.3); }"
                    + ".test-name { font-weight: 600 !important; letter-spacing: 0.5px; }"
                    + ".nav-wrapper { background: transparent !important; }"
                    + ".brand-logo { display: none !important; }";
            sparkReporter.config().setCss(customCss);

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Author", "QA Automation Team");
            extentReports.setSystemInfo("Framework", "Selenium + RestAssured + TestNG");
        }
        return extentReports;
    }

    // =====================================================================
    // Tạo test node cho mỗi test case
    // =====================================================================

    /**
     * Tạo một ExtentTest node mới cho test case.
     * Nên gọi trong @BeforeMethod hoặc onTestStart của listener.
     */
    public static ExtentTest createTest(String testName) {
        ExtentTest test = getInstance().createTest(testName);
        extentTestThreadLocal.set(test);
        return test;
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        extentTestThreadLocal.set(test);
        return test;
    }

    // =====================================================================
    // Lấy ExtentTest của thread hiện tại
    // =====================================================================

    /** Lấy ExtentTest của thread đang chạy (dùng trong test body để log bước) */
    public static ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }

    // =====================================================================
    // Flush report (gọi khi suite kết thúc)
    // =====================================================================

    /**
     * Ghi toàn bộ kết quả ra file HTML.
     * Phải gọi sau khi tất cả test kết thúc (trong @AfterSuite của listener).
     */
    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    /** Dọn dẹp ThreadLocal sau mỗi test */
    public static void removeTest() {
        extentTestThreadLocal.remove();
    }
}
