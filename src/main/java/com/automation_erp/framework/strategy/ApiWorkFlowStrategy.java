package com.automation_erp.framework.strategy;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.api.clients.InboundClient;
import com.automation_erp.framework.api.clients.OutboundClient;
import com.automation_erp.framework.api.clients.TransferClient;
import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.models.InboundRequest;
import com.automation_erp.framework.models.ItemDetail;
import com.automation_erp.framework.models.OutboundRequest;
import com.automation_erp.framework.models.TransferRequest;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class ApiWorkFlowStrategy implements WorkFlowStrategy {

    // =====================================================================
    // Inbound Flow
    // =====================================================================

    @Override
    public String executeInboundFlow(Map<String, Object> testData) {
        System.out.println("[API Strategy] Bắt đầu luồng Nhập kho qua API...");

        boolean isMock = Boolean.parseBoolean(ConfigReader.getProperty("api.mock"));
        if (isMock) {
            System.out.println("[API Strategy] [MOCK] Giả lập luồng Nhập kho thành công → ID: inbound-mock-123");
            return "inbound-mock-123";
        }

        String staffToken = ApiClient.login(ConfigReader.getProperty("staff.username"),
                                            ConfigReader.getProperty("staff.password"));
        String adminToken = ApiClient.login(ConfigReader.getProperty("admin.username"),
                                            ConfigReader.getProperty("admin.password"));

        // Build request dùng Model thay vì Map
        ItemDetail item = ItemDetail.builder()
                .productId(3) // mock productId thay cho sku
                .qtyPlanned((Integer) testData.get("quantity"))
                .unitCost(testData.containsKey("price") ? (Double) testData.get("price") : 50000.0)
                .build();

        InboundRequest inboundPayload = InboundRequest.builder()
                .docNo("NK-" + System.currentTimeMillis())
                .docDate("2026-06-29")
                .dstWarehouseId(1)
                .lines(List.of(item))
                .note("API Automation Inbound Test")
                .build();

        // Step 1: Tạo nháp
        Response createRes = InboundClient.createInbound(staffToken, inboundPayload);
        String inboundId = createRes.jsonPath().getString("data.id");
        System.out.println("[API Strategy] Tạo phiếu nháp: ID=" + inboundId);

        // Step 2: Gửi duyệt
        InboundClient.submitInbound(staffToken, inboundId);
        System.out.println("[API Strategy] Đã gửi duyệt phiếu: ID=" + inboundId);

        // Step 3: Duyệt phiếu
        InboundClient.approveInbound(adminToken, inboundId);
        System.out.println("[API Strategy] Admin đã duyệt phiếu: ID=" + inboundId);

        // Step 4: Nhập kho thực tế (với idempotency key)
        String idempotencyKey = (String) testData.get("idempotencyKey");
        Object payload = Map.of("lines", List.of(Map.of("id", 1))); // Mock line id for stub
        Response receiptRes = InboundClient.postReceipt(staffToken, inboundId, idempotencyKey, payload);
        System.out.println("[API Strategy] Nhập kho hoàn tất: Status=" + receiptRes.getStatusCode());

        return inboundId;
    }

    // =====================================================================
    // Outbound Flow
    // =====================================================================

    @Override
    public String executeOutboundFlow(Map<String, Object> testData) {
        System.out.println("[API Strategy] Bắt đầu luồng Xuất kho qua API...");

        boolean isMock = Boolean.parseBoolean(ConfigReader.getProperty("api.mock"));
        if (isMock) {
            System.out.println("[API Strategy] [MOCK] Giả lập luồng Xuất kho thành công → ID: outbound-mock-123");
            return "outbound-mock-123";
        }

        String staffToken = ApiClient.login(ConfigReader.getProperty("staff.username"),
                                            ConfigReader.getProperty("staff.password"));
        String adminToken = ApiClient.login(ConfigReader.getProperty("admin.username"),
                                            ConfigReader.getProperty("admin.password"));

        // Build request
        ItemDetail item = ItemDetail.builder()
                .productId(3)
                .qtyPlanned((Integer) testData.get("quantity"))
                .build();

        OutboundRequest outboundPayload = OutboundRequest.builder()
                .warehouseCode((String) testData.get("warehouseCode"))
                .reason("API Automation Outbound Test")
                .items(List.of(item))
                .notes("Xuất kho tự động - Test")
                .build();

        // Step 1: Tạo nháp
        Response createRes = OutboundClient.createOutbound(staffToken, outboundPayload);
        String outboundId = createRes.jsonPath().getString("data.id");
        System.out.println("[API Strategy] Tạo phiếu xuất nháp: ID=" + outboundId);

        // Step 2: Gửi duyệt
        OutboundClient.submitOutbound(staffToken, outboundId);
        System.out.println("[API Strategy] Đã gửi duyệt phiếu xuất: ID=" + outboundId);

        // Step 3: Duyệt (validate available stock tại bước này)
        OutboundClient.approveOutbound(adminToken, outboundId);
        System.out.println("[API Strategy] Admin đã duyệt phiếu xuất: ID=" + outboundId);

        // Step 4: Xuất kho thực tế
        String idempotencyKey = (String) testData.getOrDefault("idempotencyKey", null);
        Response issueRes = OutboundClient.postIssue(staffToken, outboundId, idempotencyKey);
        System.out.println("[API Strategy] Xuất kho hoàn tất: Status=" + issueRes.getStatusCode());

        return outboundId;
    }

    // =====================================================================
    // Transfer Flow
    // =====================================================================

    @Override
    public String executeTransferFlow(Map<String, Object> testData) {
        System.out.println("[API Strategy] Bắt đầu luồng Điều chuyển kho qua API...");

        boolean isMock = Boolean.parseBoolean(ConfigReader.getProperty("api.mock"));
        if (isMock) {
            System.out.println("[API Strategy] [MOCK] Giả lập luồng Điều chuyển thành công → ID: transfer-mock-123");
            return "transfer-mock-123";
        }

        String staffToken = ApiClient.login(ConfigReader.getProperty("staff.username"),
                                            ConfigReader.getProperty("staff.password"));
        String adminToken = ApiClient.login(ConfigReader.getProperty("admin.username"),
                                            ConfigReader.getProperty("admin.password"));

        // Build request
        ItemDetail item = ItemDetail.builder()
                .productId(3)
                .qtyPlanned((Integer) testData.get("quantity"))
                .build();

        TransferRequest transferPayload = TransferRequest.builder()
                .sourceWarehouseCode((String) testData.get("sourceWarehouseCode"))
                .targetWarehouseCode((String) testData.get("targetWarehouseCode"))
                .items(List.of(item))
                .notes("API Automation Transfer Test")
                .build();

        // Step 1: Tạo nháp
        Response createRes = TransferClient.createTransfer(staffToken, transferPayload);
        String transferId = createRes.jsonPath().getString("data.id");
        System.out.println("[API Strategy] Tạo phiếu điều chuyển nháp: ID=" + transferId);

        // Step 2: Gửi duyệt
        TransferClient.submitTransfer(staffToken, transferId);
        System.out.println("[API Strategy] Đã gửi duyệt phiếu điều chuyển: ID=" + transferId);

        // Step 3: Duyệt
        TransferClient.approveTransfer(adminToken, transferId);
        System.out.println("[API Strategy] Admin đã duyệt phiếu điều chuyển: ID=" + transferId);

        // Step 4: Xuất điều chuyển (kho nguồn giảm, In-Transit tăng)
        TransferClient.dispatchTransfer(staffToken, transferId);
        System.out.println("[API Strategy] Đã xuất điều chuyển - hàng đang In-Transit: ID=" + transferId);

        // Step 5: Nhận tại kho đích (In-Transit giảm, kho đích tăng)
        String idempotencyKey = (String) testData.getOrDefault("idempotencyKey", null);
        Response receiveRes = TransferClient.receiveTransfer(staffToken, transferId, idempotencyKey);
        System.out.println("[API Strategy] Nhận hàng tại kho đích hoàn tất: Status=" + receiveRes.getStatusCode());

        return transferId;
    }
}

