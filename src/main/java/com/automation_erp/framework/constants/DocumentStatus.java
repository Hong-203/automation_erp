package com.automation_erp.framework.constants;

/**
 * Trạng thái chứng từ (State Machine) của hệ thống kho ERP - Module 2.
 *
 * Luồng chứng từ nhập / xuất kho:
 *   DRAFT → PENDING_APPROVAL → APPROVED → COMPLETED / CANCELLED / REJECTED
 *
 * Luồng điều chuyển kho:
 *   DRAFT → PENDING_APPROVAL → APPROVED → IN_TRANSIT → COMPLETED / RETURNED / REJECTED
 */
public final class DocumentStatus {

    private DocumentStatus() {}

    /** Nháp - vừa tạo, chưa gửi duyệt */
    public static final String DRAFT             = "Nháp";

    /** Chờ duyệt - đã submit, chờ quản lý phê duyệt */
    public static final String PENDING_APPROVAL  = "Chờ duyệt";

    /** Đã duyệt - quản lý đã approve, chờ thực hiện */
    public static final String APPROVED          = "Đã duyệt";

    /**
     * Đang vận chuyển - dành riêng cho Transfer Order
     * sau khi gọi dispatch (xuất kho nguồn)
     */
    public static final String IN_TRANSIT        = "Đang vận chuyển";

    /** Đã nhập kho - Inbound hoàn tất (post-receipt thành công) */
    public static final String RECEIVED          = "Đã nhập kho";

    /** Đã xuất kho - Outbound hoàn tất (post-issue thành công) */
    public static final String ISSUED            = "Đã xuất kho";

    /** Hoàn tất - Transfer Order kết thúc thành công */
    public static final String COMPLETED         = "Hoàn tất";

    /** Hủy - chứng từ bị hủy trước khi thực hiện */
    public static final String CANCELLED         = "Hủy";

    /** Bị từ chối - quản lý reject phiếu */
    public static final String REJECTED          = "Bị từ chối";

    /** Bị trả lại - Transfer Order bị kho đích trả về */
    public static final String RETURNED          = "Bị trả lại";
}
