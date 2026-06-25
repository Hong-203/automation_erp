package com.automation_erp.framework.strategy;

import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.driver.DriverManager;
import com.automation_erp.framework.pages.InboundPage;
import com.automation_erp.framework.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import java.util.Map;

public class UiWorkFlowStrategy implements WorkFlowStrategy {

    @Override
    public String executeInboundFlow(Map<String, Object> testData) {
        System.out.println("[UI Strategy] Đang bắt đầu luồng Nhập kho qua trình duyệt (Selenium)...");

        
        boolean isMock = Boolean.parseBoolean(ConfigReader.getProperty("ui.mock"));
        if (isMock) {
            System.out.println("[UI Strategy] [MOCK MODE] Đang giả lập luồng UI Nhập kho qua trình duyệt...");
            System.out.println("[UI Strategy] [MOCK MODE] Step 1: Điều hướng tới trang đăng nhập.");
            System.out.println("[UI Strategy] [MOCK MODE] Step 2: Đăng nhập bằng tài khoản Staff thành công.");
            System.out.println("[UI Strategy] [MOCK MODE] Step 3: Tạo phiếu nháp nhập kho thành công.");
            System.out.println("[UI Strategy] [MOCK MODE] Step 4: Gửi duyệt phiếu nhập kho thành công.");
            System.out.println("[UI Strategy] [MOCK MODE] Step 5: Quản lý đăng nhập và duyệt phiếu thành công.");
            System.out.println("[UI Strategy] [MOCK MODE] Step 6: Nhân viên xác nhận hoàn tất nhập kho.");
            
            
            if (DriverManager.getDriver() != null) {
                DriverManager.getDriver().get("about:blank");
            }
            return "inbound-ui-mock-123";
        }

        WebDriver driver = DriverManager.getDriver();
        LoginPage loginPage = new LoginPage(driver);
        InboundPage inboundPage = new InboundPage(driver);

        
        loginPage.navigateToLoginPage();
        loginPage.login(ConfigReader.getProperty("staff.username"), ConfigReader.getProperty("staff.password"));

        
        inboundPage.clickCreateInbound();
        inboundPage.selectWarehouse((String) testData.get("warehouseCode"));
        inboundPage.addProductLine(
                (String) testData.get("sku"), 
                (int) testData.get("quantity"), 
                (double) testData.get("price")
        );
        inboundPage.saveDraft();
        System.out.println("[UI Strategy] Đã tạo nháp phiếu nhập kho.");

        
        inboundPage.submitForApproval();
        System.out.println("[UI Strategy] Đã gửi duyệt phiếu nhập kho.");

        
        loginPage.navigateToLoginPage();
        loginPage.login(ConfigReader.getProperty("admin.username"), ConfigReader.getProperty("admin.password"));
        
        
        inboundPage.approveInbound();
        System.out.println("[UI Strategy] Admin đã phê duyệt phiếu nhập kho.");

        
        loginPage.navigateToLoginPage();
        loginPage.login(ConfigReader.getProperty("staff.username"), ConfigReader.getProperty("staff.password"));
        inboundPage.confirmReceipt();
        System.out.println("[UI Strategy] Nhân viên xác nhận hoàn tất nhập kho.");

        return "inbound-ui-id";
    }

    @Override
    public String executeOutboundFlow(Map<String, Object> testData) {
        System.out.println("[UI Strategy] Đang thực hiện luồng Xuất kho qua UI...");
        return "outbound-ui-id";
    }

    @Override
    public String executeTransferFlow(Map<String, Object> testData) {
        System.out.println("[UI Strategy] Đang thực hiện luồng Điều chuyển qua UI...");
        return "transfer-ui-id";
    }
}
