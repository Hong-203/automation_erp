package com.automation_erp.framework.constants;

/**
 * Loại kho trong hệ thống kho đa tầng (Multi-Warehouse) ERP - Module 2.
 *
 * Mô hình cây kho:
 *   MAIN (Kho tổng) → BRANCH (Kho chi nhánh) → SUB (Kho con cấp 2)
 *
 * Ràng buộc MVP:
 *   - Chỉ kho MAIN được nhập hàng từ nhà cung cấp.
 *   - Tối đa 3 cấp trong cây kho.
 *   - Mỗi kho chỉ có một kho cha trực tiếp.
 */
public final class WarehouseType {

    private WarehouseType() {}

    /** Kho tổng - nguồn nhập hàng duy nhất từ nhà cung cấp */
    public static final String MAIN   = "main";

    /** Kho chi nhánh - nhận hàng điều chuyển từ kho tổng */
    public static final String BRANCH = "branch";

    /** Kho con cấp 2 - nhận hàng điều chuyển từ kho chi nhánh */
    public static final String SUB    = "sub";

    /** Trạng thái kho hoạt động bình thường */
    public static final int STATUS_ACTIVE   = 1;

    /** Trạng thái kho đã bị ngừng hoạt động (disable) */
    public static final int STATUS_INACTIVE = 0;
}
