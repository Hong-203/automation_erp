package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.constants.ApiEndpoints;
import io.restassured.response.Response;

import java.util.Map;

/**
 * API Client cho nhóm Inventory & Reporting endpoints:
 *   GET /inventory/balances           — Tồn kho khả dụng theo kho/SKU
 *   GET /in-transit-ledger            — Sổ cái hàng đang vận chuyển
 *   GET /stock-movements              — Lịch sử biến động tồn kho
 *   GET /stock-movements/{id}
 *   GET /stock-movements/{id}/trace-source
 *   GET /document-status-history      — Lịch sử trạng thái chứng từ
 *   GET /reports/inventory-xnt        — Báo cáo xuất nhập tồn
 *   GET /reports/revenue
 */
public class InventoryClient {

    private InventoryClient() {}

    // =====================================================================
    // Inventory Balances — Tồn kho khả dụng
    // =====================================================================

    /**
     * GET /inventory/balances — Lấy tồn kho khả dụng.
     *
     * Gợi ý query params:
     *   - warehouse_code: lọc theo kho
     *   - sku: lọc theo sản phẩm
     *   - include_in_transit: true/false
     *
     * Dùng sau nhập kho để verify tồn kho tăng.
     * Dùng sau xuất kho để verify tồn kho giảm.
     */
    public static Response getInventoryBalances(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.INVENTORY_BALANCES, token, queryParams);
    }

    public static Response getInventoryBalances(String token) {
        return getInventoryBalances(token, null);
    }

    /**
     * Shorthand: lấy tồn kho của một SKU tại một kho cụ thể.
     */
    public static Response getInventoryBalance(String token, String warehouseCode, String sku) {
        Map<String, Object> params = Map.of("warehouse_code", warehouseCode, "sku", sku);
        return getInventoryBalances(token, params);
    }

    // =====================================================================
    // In-Transit Ledger — Sổ cái hàng đang vận chuyển
    // =====================================================================

    /**
     * GET /in-transit-ledger — Lấy danh sách hàng đang In-Transit.
     * Dùng sau dispatch để verify In-Transit tăng.
     * Dùng sau receive/return để verify In-Transit về 0.
     */
    public static Response getInTransitLedger(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.IN_TRANSIT_LEDGER, token, queryParams);
    }

    public static Response getInTransitLedger(String token) {
        return getInTransitLedger(token, null);
    }

    // =====================================================================
    // Stock Movements — Lịch sử biến động tồn kho
    // =====================================================================

    /**
     * GET /stock-movements — Lấy lịch sử biến động tồn kho.
     *
     * Gợi ý query params:
     *   - warehouse_code, sku, transaction_type
     *   - from_date, to_date
     *   - document_id: lọc theo phiếu cụ thể
     */
    public static Response getStockMovements(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.STOCK_MOVEMENTS, token, queryParams);
    }

    public static Response getStockMovements(String token) {
        return getStockMovements(token, null);
    }

    /** GET /stock-movements/{id} — Chi tiết một bản ghi biến động */
    public static Response getStockMovementById(String token, String id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.STOCK_MOVEMENT_BY_ID, id), token, null);
    }

    /** GET /stock-movements/{id}/trace-source — Truy vết nguồn gốc biến động */
    public static Response traceStockMovement(String token, String id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.STOCK_MOVEMENT_TRACE, id), token, null);
    }

    // =====================================================================
    // Document Status History — Lịch sử trạng thái chứng từ
    // =====================================================================

    /**
     * GET /document-status-history — Lấy lịch sử thay đổi trạng thái chứng từ.
     *
     * Gợi ý query params:
     *   - document_id, document_type: "inbound" | "outbound" | "transfer"
     */
    public static Response getDocumentStatusHistory(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.DOCUMENT_STATUS_HISTORY, token, queryParams);
    }

    // =====================================================================
    // Reports — Báo cáo xuất nhập tồn
    // =====================================================================

    /**
     * GET /reports/inventory-xnt — Báo cáo xuất nhập tồn.
     *
     * Gợi ý query params:
     *   - from_date, to_date (ISO date: yyyy-MM-dd)
     *   - warehouse_code
     *   - sku
     */
    public static Response getInventoryXntReport(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.REPORT_INVENTORY_XNT, token, queryParams);
    }

    /** GET /reports/revenue — Báo cáo doanh thu */
    public static Response getRevenueReport(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.REPORT_REVENUE, token, queryParams);
    }
}
