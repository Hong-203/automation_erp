package com.automation_erp.framework.strategy;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.config.ConfigReader;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;

public class ApiWorkFlowStrategy implements WorkFlowStrategy {

    @Override
    public String executeInboundFlow(Map<String, Object> testData) {
        System.out.println("[API Strategy] Đang bắt đầu luồng Nhập kho qua API...");

        
        boolean isMock = Boolean.parseBoolean(ConfigReader.getProperty("api.mock"));
        if (isMock) {
            System.out.println("[API Strategy] [MOCK MODE] Đang giả lập luồng API Nhập kho...");
            System.out.println("[API Strategy] [MOCK MODE] Step 1: Đăng nhập thành công.");
            System.out.println("[API Strategy] [MOCK MODE] Step 2: Tạo phiếu nháp (POST /inbound-documents) thành công. ID = inbound-mock-123");
            System.out.println("[API Strategy] [MOCK MODE] Step 3: Gửi duyệt phiếu (POST /inbound-documents/inbound-mock-123/submit) thành công.");
            System.out.println("[API Strategy] [MOCK MODE] Step 4: Quản lý đã duyệt phiếu (POST /inbound-documents/inbound-mock-123/approve) thành công.");
            System.out.println("[API Strategy] [MOCK MODE] Step 5: Hoàn tất thực hiện nhập kho (POST /inbound-documents/inbound-mock-123/post-receipt) thành công.");
            return "inbound-mock-123";
        }

        
        String staffToken = ApiClient.login(ConfigReader.getProperty("staff.username"), ConfigReader.getProperty("staff.password"));
        String adminToken = ApiClient.login(ConfigReader.getProperty("admin.username"), ConfigReader.getProperty("admin.password"));

        
        Map<String, Object> itemDetail = Map.of(
                "sku", testData.get("sku"),
                "quantity", testData.get("quantity"),
                "price", testData.get("price")
        );
        Map<String, Object> inboundPayload = Map.of(
                "warehouseCode", testData.get("warehouseCode"),
                "items", List.of(itemDetail),
                "notes", "API Automation Inbound Test"
        );
        
        Response createRes = ApiClient.post("/inbound-documents", staffToken, inboundPayload);
        String inboundId = createRes.jsonPath().getString("id");
        System.out.println("[API Strategy] Đã tạo phiếu nháp: ID = " + inboundId);

        
        ApiClient.post("/inbound-documents/" + inboundId + "/submit", staffToken, null);
        System.out.println("[API Strategy] Đã gửi duyệt phiếu: ID = " + inboundId);

        
        ApiClient.post("/inbound-documents/" + inboundId + "/approve", adminToken, null);
        System.out.println("[API Strategy] Quản lý đã duyệt phiếu: ID = " + inboundId);

        
        String idempotencyKey = (String) testData.get("idempotencyKey");
        Response receiptRes = ApiClient.post(
                "/inbound-documents/" + inboundId + "/post-receipt", 
                staffToken, 
                null, 
                idempotencyKey
        );
        System.out.println("[API Strategy] Hoàn tất thực hiện nhập kho: Status Code = " + receiptRes.getStatusCode());

        return inboundId;
    }

    @Override
    public String executeOutboundFlow(Map<String, Object> testData) {
        System.out.println("[API Strategy] Đang thực hiện luồng Xuất kho qua API...");
        
        return "outbound-id-123";
    }

    @Override
    public String executeTransferFlow(Map<String, Object> testData) {
        System.out.println("[API Strategy] Đang thực hiện luồng Điều chuyển kho qua API...");
        
        return "transfer-id-123";
    }
}
