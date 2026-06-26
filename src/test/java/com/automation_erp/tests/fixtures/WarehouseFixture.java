package com.automation_erp.tests.fixtures;

import com.automation_erp.framework.api.clients.WarehouseClient;
import com.automation_erp.framework.constants.HttpStatus;
import com.automation_erp.framework.constants.WarehouseType;
import com.automation_erp.framework.models.WarehouseRequest;
import com.automation_erp.framework.utils.DataGenerator;
import io.restassured.response.Response;

/**
 * Fixture tạo và dọn dẹp dữ liệu Warehouse trong test.
 *
 * Đảm bảo Test Data Isolation: mỗi test case dùng kho test riêng,
 * không ảnh hưởng nhau khi chạy song song.
 *
 * Cách dùng trong test class (với @BeforeMethod / @AfterMethod):
 *
 *   private WarehouseFixture warehouseFixture;
 *   private int testWarehouseId;
 *
 *   @BeforeMethod
 *   public void createTestData() {
 *       warehouseFixture = new WarehouseFixture(adminToken);
 *       testWarehouseId = warehouseFixture.createAndGetId();
 *   }
 *
 *   @AfterMethod(alwaysRun = true)
 *   public void cleanupTestData() {
 *       warehouseFixture.teardown();
 *   }
 */
public class WarehouseFixture {

    private final String token;
    private Integer createdWarehouseId;
    private String createdWarehouseCode;

    public WarehouseFixture(String token) {
        this.token = token;
    }

    // =====================================================================
    // Setup: Tạo kho test
    // =====================================================================

    /**
     * Tạo một kho chi nhánh test mới và trả về ID.
     * Code kho sinh ngẫu nhiên để tránh trùng khi chạy song song.
     */
    public int createAndGetId() {
        String code = DataGenerator.generateWarehouseCode();
        String name = DataGenerator.generateWarehouseName("Kho Chi Nhánh Test");
        return createAndGetId(code, name, WarehouseType.BRANCH, 1);
    }

    /**
     * Tạo kho test với thông tin đầy đủ tùy chỉnh.
     *
     * @param code         Mã kho (nên dùng DataGenerator.generateWarehouseCode())
     * @param name         Tên kho
     * @param warehouseType Loại kho: WarehouseType.MAIN / BRANCH / SUB
     * @param parentId     ID kho cha (null nếu là kho tổng)
     */
    public int createAndGetId(String code, String name, String warehouseType, Integer parentId) {
        WarehouseRequest payload = WarehouseRequest.builder()
                .companyId(1)
                .branchId(1)
                .parentId(parentId)
                .code(code)
                .name(name)
                .warehouseType(warehouseType)
                .address("Auto Test Address - " + code)
                .managerId(1)
                .status(WarehouseType.STATUS_ACTIVE)
                .build();

        Response response = WarehouseClient.createWarehouse(token, payload);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException(
                String.format("[WarehouseFixture] Tạo kho test thất bại! Status: %d | Body: %s",
                    response.getStatusCode(), response.asPrettyString())
            );
        }

        createdWarehouseId   = response.jsonPath().getInt("data.id");
        createdWarehouseCode = code;

        System.out.printf("[WarehouseFixture] ✅ Đã tạo kho test: ID=%d | Code=%s%n",
            createdWarehouseId, createdWarehouseCode);

        return createdWarehouseId;
    }

    // =====================================================================
    // Getters
    // =====================================================================

    public Integer getId() {
        return createdWarehouseId;
    }

    public String getCode() {
        return createdWarehouseCode;
    }

    public void setId(Integer id) {
        this.createdWarehouseId = id;
    }

    public void setCode(String code) {
        this.createdWarehouseCode = code;
    }

    // =====================================================================
    // Teardown: Dọn dẹp kho sau test
    // =====================================================================

    /**
     * Xóa hoặc disable kho test đã tạo.
     * Nên gọi trong @AfterMethod(alwaysRun = true) để đảm bảo luôn cleanup.
     */
    public void teardown() {
        if (createdWarehouseId == null) {
            return;
        }

        try {
            // Thử disable kho trước (an toàn hơn delete khi có tồn kho)
            Response disableRes = WarehouseClient.disableWarehouse(token, createdWarehouseId);
            if (disableRes.getStatusCode() == HttpStatus.OK || disableRes.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.printf("[WarehouseFixture] 🧹 Đã disable kho test: ID=%d | Code=%s%n",
                    createdWarehouseId, createdWarehouseCode);
            } else {
                // Nếu disable thất bại thì thử delete
                Response deleteRes = WarehouseClient.deleteWarehouse(token, createdWarehouseId);
                System.out.printf("[WarehouseFixture] 🧹 Đã xóa kho test: ID=%d | Status=%d%n",
                    createdWarehouseId, deleteRes.getStatusCode());
            }
        } catch (Exception e) {
            System.err.printf("[WarehouseFixture] ⚠️ Không thể cleanup kho ID=%d: %s%n",
                createdWarehouseId, e.getMessage());
        } finally {
            createdWarehouseId   = null;
            createdWarehouseCode = null;
        }
    }
}
