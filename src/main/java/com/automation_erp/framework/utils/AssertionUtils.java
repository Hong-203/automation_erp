package com.automation_erp.framework.utils;

import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Tiện ích assertion dùng chung trong API test.
 * Bọc TestNG Assert với message log rõ ràng hơn để dễ debug khi test fail.
 */
public final class AssertionUtils {

    private AssertionUtils() {}

    // =====================================================================
    // HTTP Status Code
    // =====================================================================

    /**
     * Kiểm tra HTTP status code của response.
     * Tự động log actual status code khi fail để dễ debug.
     */
    public static void assertStatusCode(Response response, int expectedStatusCode) {
        int actual = response.getStatusCode();
        Assert.assertEquals(
            actual,
            expectedStatusCode,
            String.format("HTTP Status Code không đúng. Expected: %d | Actual: %d | Body: %s",
                expectedStatusCode, actual, response.asPrettyString())
        );
    }

    /**
     * Kiểm tra status code là 2xx (thành công).
     */
    public static void assertSuccess(Response response) {
        int actual = response.getStatusCode();
        Assert.assertTrue(
            actual >= 200 && actual < 300,
            String.format("Expect HTTP 2xx nhưng nhận được: %d | Body: %s",
                actual, response.asPrettyString())
        );
    }

    // =====================================================================
    // JSON Field assertions
    // =====================================================================

    /**
     * Kiểm tra field trong JSON response không null.
     * VD: assertFieldNotNull(response, "data.id")
     */
    public static void assertFieldNotNull(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        Assert.assertNotNull(
            value,
            String.format("Field '%s' không được null. Body: %s", jsonPath, response.asPrettyString())
        );
    }

    /**
     * Kiểm tra giá trị field trong JSON response bằng giá trị mong đợi.
     * VD: assertFieldEquals(response, "data.status", "Nháp")
     */
    public static void assertFieldEquals(Response response, String jsonPath, Object expected) {
        Object actual = response.jsonPath().get(jsonPath);
        Assert.assertEquals(
            actual,
            expected,
            String.format("Field '%s' không đúng. Expected: '%s' | Actual: '%s' | Body: %s",
                jsonPath, expected, actual, response.asPrettyString())
        );
    }

    /**
     * Kiểm tra field string trong JSON response bằng giá trị mong đợi.
     */
    public static void assertStringFieldEquals(Response response, String jsonPath, String expected) {
        String actual = response.jsonPath().getString(jsonPath);
        Assert.assertEquals(
            actual,
            expected,
            String.format("Field '%s' không đúng. Expected: '%s' | Actual: '%s'", jsonPath, expected, actual)
        );
    }

    /**
     * Kiểm tra response body chứa chuỗi ký tự nhất định.
     */
    public static void assertBodyContains(Response response, String keyword) {
        String body = response.asString();
        Assert.assertTrue(
            body.contains(keyword),
            String.format("Response body không chứa '%s'. Actual body: %s", keyword, body)
        );
    }

    // =====================================================================
    // Inventory-specific assertions
    // =====================================================================

    /**
     * Kiểm tra số lượng tồn kho khả dụng (available) tại một kho.
     * Dùng sau khi gọi GET /inventory/balances
     */
    public static void assertAvailableStock(Response balanceResponse,
                                            String warehouseCode,
                                            String sku,
                                            int expectedQty) {
        // Tìm trong mảng items của response
        // Path mẫu: data[0].available_quantity
        // Adjust jsonPath theo cấu trúc response thực tế của hệ thống
        Integer actual = balanceResponse.jsonPath()
                .get(String.format("data.find{it.warehouse_code=='%s' && it.sku=='%s'}.available_quantity",
                        warehouseCode, sku));
        Assert.assertEquals(
            actual,
            (Integer) expectedQty,
            String.format("Available stock của SKU '%s' tại kho '%s' không đúng. Expected: %d | Actual: %s",
                sku, warehouseCode, expectedQty, actual)
        );
    }

    // =====================================================================
    // General
    // =====================================================================

    /**
     * Fail test với thông báo tùy chỉnh.
     */
    public static void fail(String message) {
        Assert.fail("[TEST FAILED] " + message);
    }

    /**
     * Log thông tin (không fail test) - dùng khi muốn debug.
     */
    public static void log(String message) {
        System.out.println("[ASSERTION LOG] " + message);
    }
}
