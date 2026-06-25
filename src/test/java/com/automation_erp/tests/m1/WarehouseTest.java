package com.automation_erp.tests.m1;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.api.clients.WarehouseClient;
import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.models.WarehouseRequest;
import com.automation_erp.tests.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WarehouseTest extends BaseTest {

    @Test(description = "Test luồng tạo kho thành công bằng API thực tế")
    public void testCreateWarehouseSuccess() {

        String token = ConfigReader.getProperty("auth.token");
        if (token == null || token.isEmpty() || token.startsWith("mock")) {
            System.out.println("[Test] Không thấy static token, tiến hành gọi API Login...");
            token = ApiClient.login(ConfigReader.getProperty("admin.username"),
                    ConfigReader.getProperty("admin.password"));
        } else {
            System.out.println("[Test] Sử dụng token được cấu hình sẵn trong config.properties");
        }

        String uniqueCode = "WH" + (System.currentTimeMillis() % 1000000);
        WarehouseRequest payload = WarehouseRequest.builder()
                .companyId(1)
                .branchId(1)
                .parentId(1)
                .code(uniqueCode)
                .name("Kho Quận 7 - Auto Test version 2")
                .warehouseType("branch")
                .address("Quận 7, TP.HCM")
                .managerId(1)
                .status(1)
                .build();

        System.out.println("[Test] Gửi request tạo kho: " + payload.getName() + " [Mã: " + payload.getCode() + "]");

        Response response = WarehouseClient.createWarehouse(token, payload);

        System.out.println("[Test] Response Status Code: " + response.getStatusCode());
        System.out.println("[Test] Response Body:\n" + response.asPrettyString());

        Assert.assertEquals(response.getStatusCode(), 201, "Mã phản hồi HTTP trả về phải là 201 Created!");
        Assert.assertNotNull(response.jsonPath().get("data.id"), "Trường 'data.id' của kho mới tạo không được null!");
        Assert.assertEquals(response.jsonPath().getString("data.code"), payload.getCode(), "Mã kho trả về không khớp!");
    }
}
