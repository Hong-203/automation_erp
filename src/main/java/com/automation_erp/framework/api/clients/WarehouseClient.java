package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.constants.ApiEndpoints;
import com.automation_erp.framework.models.WarehouseRequest;
import io.restassured.response.Response;

import java.util.Map;

/**
 * API Client cho nhóm Warehouse endpoints:
 *   GET/POST /warehouses
 *   GET/PUT/DELETE /warehouses/{id}
 *   PATCH /warehouses/{id}/enable
 *   PATCH /warehouses/{id}/disable
 */
public class WarehouseClient {

    private WarehouseClient() {}

    // =====================================================================
    // CRUD
    // =====================================================================

    /** POST /warehouses — Tạo kho mới */
    public static Response createWarehouse(String token, WarehouseRequest payload) {
        return ApiClient.post(ApiEndpoints.WAREHOUSES, token, payload);
    }

    /** GET /warehouses — Lấy danh sách kho (có thể truyền query params để filter) */
    public static Response getWarehouses(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.WAREHOUSES, token, queryParams);
    }

    /** GET /warehouses — Lấy toàn bộ danh sách không filter */
    public static Response getWarehouses(String token) {
        return getWarehouses(token, null);
    }

    /** GET /warehouses/{id} — Lấy chi tiết kho theo ID */
    public static Response getWarehouseById(String token, int id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.WAREHOUSE_BY_ID, id), token, null);
    }

    /** PUT /warehouses/{id} — Cập nhật thông tin kho */
    public static Response updateWarehouse(String token, int id, WarehouseRequest payload) {
        return ApiClient.put(ApiEndpoints.path(ApiEndpoints.WAREHOUSE_BY_ID, id), token, payload);
    }

    /** DELETE /warehouses/{id} — Xóa kho */
    public static Response deleteWarehouse(String token, int id) {
        return ApiClient.delete(ApiEndpoints.path(ApiEndpoints.WAREHOUSE_BY_ID, id), token);
    }

    // =====================================================================
    // Enable / Disable
    // =====================================================================

    /** PATCH /warehouses/{id}/enable — Kích hoạt lại kho đã bị disable */
    public static Response enableWarehouse(String token, int id) {
        return ApiClient.patch(ApiEndpoints.path(ApiEndpoints.WAREHOUSE_ENABLE, id), token, null);
    }

    /** PATCH /warehouses/{id}/disable — Ngừng hoạt động kho */
    public static Response disableWarehouse(String token, int id) {
        return ApiClient.patch(ApiEndpoints.path(ApiEndpoints.WAREHOUSE_DISABLE, id), token, null);
    }
}
