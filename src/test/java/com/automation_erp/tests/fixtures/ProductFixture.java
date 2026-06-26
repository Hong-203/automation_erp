package com.automation_erp.tests.fixtures;

import com.automation_erp.framework.utils.DataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixture quản lý dữ liệu SKU test.
 *
 * Trong MVP, SKU được quản lý bởi Module khác (Module Sản phẩm).
 * ProductFixture giả định SKU đã tồn tại trong hệ thống, chỉ quản lý
 * danh sách các mã SKU dùng trong test để dễ tra cứu và đối chiếu kết quả.
 *
 * Nếu hệ thống có API tạo/xóa SKU, bổ sung vào các phương thức create/teardown.
 *
 * Cách dùng:
 *   ProductFixture productFixture = new ProductFixture();
 *   String sku = productFixture.useExistingSku("SKU-IPHONE15");
 *   // hoặc sinh SKU test ngẫu nhiên:
 *   String testSku = productFixture.generateTestSku("MACBOOK");
 */
public class ProductFixture {

    /** Danh sách SKU đã dùng trong test (để track, log, và cleanup nếu cần) */
    private final List<String> usedSkus = new ArrayList<>();

    // =====================================================================
    // Dùng SKU đã có sẵn trong hệ thống
    // =====================================================================

    /**
     * Đăng ký một SKU đã tồn tại để dùng trong test.
     * Thêm vào danh sách tracking để log và cleanup nếu cần.
     */
    public String useExistingSku(String sku) {
        usedSkus.add(sku);
        return sku;
    }

    // =====================================================================
    // Sinh SKU test ngẫu nhiên
    // =====================================================================

    /**
     * Sinh mã SKU test mới duy nhất theo timestamp.
     * VD: "SKU-IPHONE15-20240701143022"
     *
     * Dùng khi test cần SKU mới không xung đột với dữ liệu hiện tại.
     */
    public String generateTestSku(String productName) {
        String sku = DataGenerator.generateSkuCode(productName);
        usedSkus.add(sku);
        System.out.printf("[ProductFixture] ✅ Đã sinh SKU test: %s%n", sku);
        return sku;
    }

    /**
     * Sinh mã SKU test mới hoàn toàn ngẫu nhiên.
     */
    public String generateTestSku() {
        String sku = DataGenerator.generateSkuCode();
        usedSkus.add(sku);
        System.out.printf("[ProductFixture] ✅ Đã sinh SKU test: %s%n", sku);
        return sku;
    }

    // =====================================================================
    // Getters
    // =====================================================================

    /** Lấy SKU đầu tiên đã đăng ký (tiện cho test case dùng 1 SKU) */
    public String getFirstSku() {
        if (usedSkus.isEmpty()) {
            throw new IllegalStateException("[ProductFixture] Chưa có SKU nào được đăng ký!");
        }
        return usedSkus.get(0);
    }

    /** Lấy toàn bộ danh sách SKU đã dùng */
    public List<String> getAllSkus() {
        return new ArrayList<>(usedSkus);
    }

    // =====================================================================
    // Teardown
    // =====================================================================

    /**
     * Dọn dẹp SKU test sau khi test kết thúc.
     *
     * TODO: Khi hệ thống có API xóa SKU, implement gọi API xóa tại đây.
     * Hiện tại chỉ log danh sách SKU đã dùng để dễ trace.
     */
    public void teardown() {
        if (!usedSkus.isEmpty()) {
            System.out.println("[ProductFixture] 🧹 Danh sách SKU test đã dùng: " + usedSkus);
            // TODO: Gọi API xóa SKU test nếu hệ thống hỗ trợ
            usedSkus.clear();
        }
    }
}
