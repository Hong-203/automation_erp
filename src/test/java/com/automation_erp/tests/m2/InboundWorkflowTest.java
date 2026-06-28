package com.automation_erp.tests.m2;

import com.automation_erp.framework.api.AuthManager;
import com.automation_erp.framework.api.clients.InboundClient;
import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.constants.DocumentStatus;
import com.automation_erp.framework.constants.HttpStatus;
import com.automation_erp.framework.models.InboundRequest;
import com.automation_erp.framework.models.ItemDetail;
import com.automation_erp.framework.utils.AssertionUtils;
import com.automation_erp.framework.utils.DataGenerator;
import com.automation_erp.framework.utils.DateUtils;
import com.automation_erp.tests.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InboundWorkflowTest extends BaseTest {

    private String adminToken;
    private String staffToken;

    private String happyPathInboundId;
    private Integer happyPathLineId;
    private Integer happyPathQtyPlanned;

    @BeforeClass
    public void setupTokens() {
        adminToken = AuthManager.getToken(ConfigReader.getProperty("admin.username"),
                ConfigReader.getProperty("admin.password"));
        staffToken = AuthManager.getToken(ConfigReader.getProperty("staff.username"),
                ConfigReader.getProperty("staff.password"));
    }

    // =====================================================================
    // LUỒNG 1: Happy Path - Nhập kho thành công
    // =====================================================================
    @Test(description = "TC-INBOUND-01: Tạo phiếu Nháp")
    public void test1_CreateDraftInbound() {
        happyPathQtyPlanned = 100;
        InboundRequest req = InboundRequest.builder()
                .docNo("NK-" + System.currentTimeMillis())
                .docDate(DateUtils.todayAsIso())
                .dstWarehouseId(1)
                .supplierId(1)
                .lines(List.of(
                        ItemDetail.builder().productId(3).qtyPlanned(happyPathQtyPlanned).unitCost(50000.0).build()))
                .note("Luồng Happy Path tự động")
                .build();

        Response res = InboundClient.createInbound(adminToken, req);

        AssertionUtils.assertStatusCode(res, HttpStatus.CREATED);
        AssertionUtils.assertFieldNotNull(res, "data.id");
        AssertionUtils.assertStringFieldEquals(res, "data.status", "draft");

        happyPathInboundId = res.jsonPath().getString("data.id");
        happyPathLineId = res.jsonPath().getInt("data.lines[0].id");
    }

    @Test(description = "TC-INBOUND-02: Cập nhật phiếu Nháp", dependsOnMethods = "test1_CreateDraftInbound")
    public void test2_UpdateDraftInbound() {
        happyPathQtyPlanned = 100;
        InboundRequest updateReq = InboundRequest.builder()
                .note("stringgg updated")
                .lines(List.of(ItemDetail.builder().id(happyPathLineId).productId(3).qtyPlanned(happyPathQtyPlanned)
                        .unitCost(50000.0).build()))
                .build();
        Response res = InboundClient.updateInbound(adminToken, happyPathInboundId, updateReq);

        AssertionUtils.assertStatusCode(res, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(res, "data.status", "draft");

        if (res.jsonPath().get("data.lines[0].id") != null) {
            happyPathLineId = res.jsonPath().getInt("data.lines[0].id");
        }
    }

    @Test(description = "TC-INBOUND-03: Gửi duyệt phiếu", dependsOnMethods = "test2_UpdateDraftInbound")
    public void test3_SubmitInbound() {
        Response res = InboundClient.submitInbound(adminToken, happyPathInboundId);
        AssertionUtils.assertStatusCode(res, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(res, "data.status", "pending"); // API trả về "pending"
    }

    @Test(description = "TC-INBOUND-04: Phê duyệt phiếu", dependsOnMethods = "test3_SubmitInbound")
    public void test4_ApproveInbound() {
        Response res = InboundClient.approveInbound(adminToken, happyPathInboundId);
        AssertionUtils.assertStatusCode(res, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(res, "data.status", "approved");
    }

    @Test(description = "TC-INBOUND-05: Nhập kho thực tế (Post-receipt)", dependsOnMethods = "test4_ApproveInbound")
    public void test5_PostReceiptInbound() {
        String idempotencyKey = UUID.randomUUID().toString();
        Object payload = Map.of(
                "lines", List.of(
                        Map.of(
                                "line_id", happyPathLineId,
                                "qty_actual", happyPathQtyPlanned,
                                "completion_type", "full")));
        Response res = InboundClient.postReceipt(adminToken, happyPathInboundId, idempotencyKey, payload);

        AssertionUtils.assertStatusCode(res, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(res, "data.status", "completed");
        AssertionUtils.assertFieldNotNull(res, "data.stock_movements");
    }

    @Test(description = "TC-INBOUND-06: Luồng Từ chối duyệt (Reject Flow)")
    public void testRejectInboundFlow() {
        InboundRequest req = InboundRequest.builder()
                .docNo("NK-" + System.currentTimeMillis())
                .docDate(DateUtils.todayAsIso())
                .dstWarehouseId(1)
                .lines(List.of(ItemDetail.builder().productId(3).qtyPlanned(10).unitCost(100.0).build()))
                .build();
        Response createRes = InboundClient.createInbound(adminToken, req);
        String inboundId = createRes.jsonPath().getString("data.id");

        InboundClient.submitInbound(adminToken, inboundId);

        Response rejectRes = InboundClient.rejectInbound(adminToken, inboundId,
                Map.of("reason", "Không hợp lệ"));
        AssertionUtils.assertStatusCode(rejectRes, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(rejectRes, "data.status", "rejected");

        Response submitAgainRes = InboundClient.submitInbound(adminToken, inboundId);
        AssertionUtils.assertStatusCode(submitAgainRes, HttpStatus.CONFLICT);
    }

    @Test(description = "TC-INBOUND-07: Luồng Hủy phiếu (Cancel Flow)")
    public void testCancelInboundFlow() {
        InboundRequest req = InboundRequest.builder()
                .docNo("NK-" + System.currentTimeMillis())
                .docDate(DateUtils.todayAsIso())
                .dstWarehouseId(1)
                .lines(List.of(ItemDetail.builder().productId(3).qtyPlanned(5).unitCost(50.0).build()))
                .build();
        Response createRes = InboundClient.createInbound(adminToken, req);
        String inboundId = createRes.jsonPath().getString("data.id");

        Response cancelRes = InboundClient.cancelInbound(adminToken, inboundId, Map.of("reason", "Hủy phiếu"));
        AssertionUtils.assertStatusCode(cancelRes, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(cancelRes, "data.status", "cancelled");

        Response updateRes = InboundClient.updateInbound(adminToken, inboundId,
                Map.of("note", "Cố tình cập nhật phiếu đã hủy"));
        AssertionUtils.assertStatusCode(updateRes, HttpStatus.CONFLICT);
    }
}
