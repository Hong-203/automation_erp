package com.automation_erp.framework.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tiện ích sinh dữ liệu test ngẫu nhiên.
 * Đảm bảo mỗi lần chạy test có dữ liệu độc lập, tránh xung đột.
 */
public final class DataGenerator {

    private DataGenerator() {}

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // =====================================================================
    // UUID & Idempotency Key
    // =====================================================================

    /** Sinh UUID ngẫu nhiên dùng làm Idempotency-Key */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    // =====================================================================
    // Warehouse Codes
    // =====================================================================

    /**
     * Sinh mã kho test duy nhất theo timestamp.
     * VD: "WH-TEST-20240701143022"
     */
    public static String generateWarehouseCode() {
        return "WH-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Sinh mã kho test với prefix tùy chỉnh.
     * VD: generateWarehouseCode("BRANCH") → "WH-BRANCH-A1B2C3D4"
     */
    public static String generateWarehouseCode(String suffix) {
        return "WH-" + suffix.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // =====================================================================
    // SKU Codes
    // =====================================================================

    /**
     * Sinh mã SKU test duy nhất.
     * VD: "SKU-TEST-A1B2C3D4"
     */
    public static String generateSkuCode() {
        return "SKU-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Sinh mã SKU test với tên sản phẩm.
     * VD: generateSkuCode("IPHONE15") → "SKU-IPHONE15-A1B2C3D4"
     */
    public static String generateSkuCode(String productName) {
        return "SKU-" + productName.toUpperCase().replace(" ", "_")
               + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // =====================================================================
    // Warehouse Name
    // =====================================================================

    /**
     * Sinh tên kho test có timestamp.
     * VD: "Kho Test 143022"
     */
    public static String generateWarehouseName() {
        return "Kho Test " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
    }

    public static String generateWarehouseName(String prefix) {
        return prefix + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
    }

    // =====================================================================
    // Quantity & Price
    // =====================================================================

    /**
     * Sinh số lượng ngẫu nhiên trong range [min, max].
     */
    public static int randomQuantity(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int randomQuantity() {
        return randomQuantity(10, 200);
    }

    /**
     * Sinh giá ngẫu nhiên trong range [min, max] (đơn vị: nghìn đồng).
     */
    public static double randomPrice(double min, double max) {
        return Math.round(ThreadLocalRandom.current().nextDouble(min, max) * 100.0) / 100.0;
    }

    public static double randomPrice() {
        return randomPrice(50.0, 5000.0);
    }

    // =====================================================================
    // Timestamp-based unique suffix
    // =====================================================================

    /** Trả về chuỗi timestamp ngắn dùng làm suffix: "143022456" */
    public static String timestampSuffix() {
        return String.valueOf(System.currentTimeMillis() % 1_000_000_000L);
    }
}
