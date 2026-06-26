package com.automation_erp.framework.constants;

/**
 * Tập trung toàn bộ API endpoint path của hệ thống ERP - Module 2.
 * Sử dụng String.format() với các hằng số có placeholder {0}, {1}...
 * để build đường dẫn động (ví dụ: /inbound-documents/{id}/submit).
 */
public final class ApiEndpoints {

    private ApiEndpoints() {}

    // =====================================================================
    // Auth
    // =====================================================================
    public static final String AUTH_LOGIN              = "/auth/login";
    public static final String AUTH_LOGOUT             = "/auth/logout";

    // =====================================================================
    // Warehouses - /warehouses
    // =====================================================================
    public static final String WAREHOUSES              = "/warehouses";
    public static final String WAREHOUSE_BY_ID         = "/warehouses/%d";
    public static final String WAREHOUSE_ENABLE        = "/warehouses/%d/enable";
    public static final String WAREHOUSE_DISABLE       = "/warehouses/%d/disable";

    // =====================================================================
    // Warehouse Scopes - /warehouse-scopes
    // =====================================================================
    public static final String WAREHOUSE_SCOPES        = "/warehouse-scopes";
    public static final String WAREHOUSE_SCOPE_BY_ID   = "/warehouse-scopes/%d";

    // =====================================================================
    // Inbound Documents - /inbound-documents
    // =====================================================================
    public static final String INBOUND_DOCUMENTS       = "/inbound-documents";
    public static final String INBOUND_BY_ID           = "/inbound-documents/%s";
    public static final String INBOUND_SUBMIT          = "/inbound-documents/%s/submit";
    public static final String INBOUND_APPROVE         = "/inbound-documents/%s/approve";
    public static final String INBOUND_POST_RECEIPT    = "/inbound-documents/%s/post-receipt";
    public static final String INBOUND_RECORD_LOSS     = "/inbound-documents/%s/record-loss";
    public static final String INBOUND_REJECT          = "/inbound-documents/%s/reject";
    public static final String INBOUND_CANCEL          = "/inbound-documents/%s/cancel";

    // =====================================================================
    // Outbound Documents - /outbound-documents
    // =====================================================================
    public static final String OUTBOUND_DOCUMENTS      = "/outbound-documents";
    public static final String OUTBOUND_BY_ID          = "/outbound-documents/%s";
    public static final String OUTBOUND_SUBMIT         = "/outbound-documents/%s/submit";
    public static final String OUTBOUND_APPROVE        = "/outbound-documents/%s/approve";
    public static final String OUTBOUND_POST_ISSUE     = "/outbound-documents/%s/post-issue";
    public static final String OUTBOUND_RECORD_LOSS    = "/outbound-documents/%s/record-loss";
    public static final String OUTBOUND_REJECT         = "/outbound-documents/%s/reject";
    public static final String OUTBOUND_CANCEL         = "/outbound-documents/%s/cancel";

    // =====================================================================
    // Transfer Orders - /transfer-orders
    // =====================================================================
    public static final String TRANSFER_ORDERS         = "/transfer-orders";
    public static final String TRANSFER_BY_ID          = "/transfer-orders/%s";
    public static final String TRANSFER_SUBMIT         = "/transfer-orders/%s/submit";
    public static final String TRANSFER_APPROVE        = "/transfer-orders/%s/approve";
    public static final String TRANSFER_DISPATCH       = "/transfer-orders/%s/dispatch";
    public static final String TRANSFER_RECEIVE        = "/transfer-orders/%s/receive";
    public static final String TRANSFER_RECORD_LOSS    = "/transfer-orders/%s/record-loss";
    public static final String TRANSFER_RETURN         = "/transfer-orders/%s/return";
    public static final String TRANSFER_REJECT         = "/transfer-orders/%s/reject";
    public static final String TRANSFER_CANCEL         = "/transfer-orders/%s/cancel";

    // =====================================================================
    // Inventory - /inventory
    // =====================================================================
    public static final String INVENTORY_BALANCES      = "/inventory/balances";
    public static final String IN_TRANSIT_LEDGER       = "/in-transit-ledger";
    public static final String INVENTORY_SNAPSHOTS     = "/inventory-snapshots";
    public static final String INVENTORY_SNAPSHOT_GEN  = "/inventory-snapshots/generate";
    public static final String INVENTORY_RESERVATIONS  = "/inventory-reservations";
    public static final String INVENTORY_RESERVATION_BY_ID = "/inventory-reservations/%s";

    // =====================================================================
    // Stock Movement - /stock-movements
    // =====================================================================
    public static final String STOCK_MOVEMENTS         = "/stock-movements";
    public static final String STOCK_MOVEMENT_BY_ID    = "/stock-movements/%s";
    public static final String STOCK_MOVEMENT_TRACE    = "/stock-movements/%s/trace-source";

    // =====================================================================
    // Safe Stock Configs - /safe-stock-configs
    // =====================================================================
    public static final String SAFE_STOCK_CONFIGS      = "/safe-stock-configs";
    public static final String SAFE_STOCK_CONFIG_BY_ID = "/safe-stock-configs/%s";

    // =====================================================================
    // Stock Requests - /stock-requests
    // =====================================================================
    public static final String STOCK_REQUESTS          = "/stock-requests";
    public static final String STOCK_REQUEST_BY_ID     = "/stock-requests/%s";
    public static final String STOCK_REQUEST_SUBMIT    = "/stock-requests/%s/submit";
    public static final String STOCK_REQUEST_APPROVE   = "/stock-requests/%s/approve";
    public static final String STOCK_REQUEST_REJECT    = "/stock-requests/%s/reject";
    public static final String STOCK_REQUEST_CONVERT   = "/stock-requests/%s/convert";

    // =====================================================================
    // Document History - /document-status-history
    // =====================================================================
    public static final String DOCUMENT_STATUS_HISTORY      = "/document-status-history";
    public static final String DOCUMENT_STATUS_TRANSITIONS  = "/document-status-transitions";
    public static final String DOC_TRANSITION_TOGGLE        = "/document-status-transitions/%s/toggle";

    // =====================================================================
    // Reports - /reports
    // =====================================================================
    public static final String REPORT_INVENTORY_XNT    = "/reports/inventory-xnt";
    public static final String REPORT_REVENUE          = "/reports/revenue";

    // =====================================================================
    // Helper: build dynamic path từ template có %s hoặc %d
    // Ví dụ: ApiEndpoints.path(INBOUND_SUBMIT, "abc-123")
    // =====================================================================
    public static String path(String template, Object... args) {
        return String.format(template, args);
    }
}
