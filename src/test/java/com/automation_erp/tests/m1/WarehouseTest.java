package com.automation_erp.tests.m1;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.api.AuthManager;
import com.automation_erp.framework.api.clients.WarehouseClient;
import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.constants.HttpStatus;
import com.automation_erp.framework.constants.WarehouseType;
import com.automation_erp.framework.models.WarehouseRequest;
import com.automation_erp.framework.utils.AssertionUtils;
import com.automation_erp.framework.utils.DataGenerator;
import com.automation_erp.tests.BaseTest;
import com.automation_erp.tests.fixtures.WarehouseFixture;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WarehouseTest extends BaseTest {

    private String adminToken;
    private WarehouseFixture warehouseFixture;

    @BeforeMethod
    public void setupTest() {
        adminToken = AuthManager.getToken(ConfigReader.getProperty("admin.username"),
                                          ConfigReader.getProperty("admin.password"));
        warehouseFixture = new WarehouseFixture(adminToken);
    }

    @AfterMethod(alwaysRun = true)
    public void teardownTest() {
        warehouseFixture.teardown();
    }

    @Test(description = "TC-WH-01: Luồng tạo kho thành công với model mới")
    public void testCreateWarehouseSuccess() {
        String uniqueCode = DataGenerator.generateWarehouseCode("WH");
        
        WarehouseRequest payload = WarehouseRequest.builder()
                .companyId(1)
                .branchId(1)
                .parentId(1)
                .code(uniqueCode)
                .name("Kho Quận 7 - Auto Test version 2")
                .warehouseType(WarehouseType.BRANCH)
                .address("Quận 7, TP.HCM")
                .managerId(1)
                .status(WarehouseType.STATUS_ACTIVE)
                .build();

        Response response = WarehouseClient.createWarehouse(adminToken, payload);

        AssertionUtils.assertStatusCode(response, HttpStatus.CREATED);
        AssertionUtils.assertFieldNotNull(response, "data.id");
        AssertionUtils.assertStringFieldEquals(response, "data.code", uniqueCode);
        
        // Gắn ID vào fixture để dọn dẹp tự động
        int id = 0;
        try {
            id = response.jsonPath().getInt("data.id");
        } catch (Exception e) {
            id = 123; // MOCK ID if API is mocked
        }
        warehouseFixture.setId(id);
        warehouseFixture.setCode(uniqueCode);
    }
}
