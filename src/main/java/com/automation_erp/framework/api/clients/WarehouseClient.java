package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.models.WarehouseRequest;
import io.restassured.response.Response;

public class WarehouseClient {

    public static Response createWarehouse(String token, WarehouseRequest payload) {
        return ApiClient.post("/warehouses", token, payload);
    }

    public static Response getWarehouseById(String token, int id) {
        return ApiClient.get("/warehouses/" + id, token, null);
    }
}
