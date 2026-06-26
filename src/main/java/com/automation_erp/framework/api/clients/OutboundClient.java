package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.constants.ApiEndpoints;
import com.automation_erp.framework.models.OutboundRequest;
import io.restassured.response.Response;

import java.util.Map;

/**
 * API Client cho nhóm Outbound Document endpoints:
 *   GET/POST /outbound-documents
 *   GET /outbound-documents/{id}
 *   POST /outbound-documents/{id}/submit
 *   POST /outbound-documents/{id}/approve
 *   POST /outbound-documents/{id}/post-issue
 *   POST /outbound-documents/{id}/record-loss
 *   POST /outbound-documents/{id}/reject
 *   POST /outbound-documents/{id}/cancel
 *
 * State Machine:
 *   Nháp → (submit) → Chờ duyệt → (approve) → Đã duyệt → (post-issue) → Đã xuất kho
 *   Nháp/Chờ duyệt → (cancel) → Hủy
 *   Chờ duyệt → (reject) → Bị từ chối
 *
 * Ràng buộc: approve sẽ validate tồn kho khả dụng đủ trước khi duyệt.
 */
public class OutboundClient {

    private OutboundClient() {}

    // =====================================================================
    // CRUD
    // =====================================================================

    /** POST /outbound-documents — Tạo phiếu xuất kho (trạng thái Nháp) */
    public static Response createOutbound(String token, OutboundRequest payload) {
        return ApiClient.post(ApiEndpoints.OUTBOUND_DOCUMENTS, token, payload);
    }

    /** GET /outbound-documents — Lấy danh sách phiếu xuất kho */
    public static Response getOutboundList(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.OUTBOUND_DOCUMENTS, token, queryParams);
    }

    /** GET /outbound-documents — Không filter */
    public static Response getOutboundList(String token) {
        return getOutboundList(token, null);
    }

    /** GET /outbound-documents/{id} — Lấy chi tiết phiếu xuất */
    public static Response getOutboundById(String token, String id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.OUTBOUND_BY_ID, id), token, null);
    }

    // =====================================================================
    // State Machine Actions
    // =====================================================================

    /** POST /outbound-documents/{id}/submit — Gửi phiếu chờ duyệt */
    public static Response submitOutbound(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.OUTBOUND_SUBMIT, id), token, null);
    }

    /**
     * POST /outbound-documents/{id}/approve — Quản lý phê duyệt phiếu.
     * Hệ thống validate available stock tại bước này.
     */
    public static Response approveOutbound(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.OUTBOUND_APPROVE, id), token, null);
    }

    /**
     * POST /outbound-documents/{id}/post-issue — Thực hiện xuất kho thực tế.
     * Cập nhật tồn kho khả dụng giảm xuống.
     * @param idempotencyKey UUID để đảm bảo tính idempotent
     */
    public static Response postIssue(String token, String id, String idempotencyKey) {
        return ApiClient.post(
            ApiEndpoints.path(ApiEndpoints.OUTBOUND_POST_ISSUE, id),
            token,
            null,
            idempotencyKey
        );
    }

    /** POST /outbound-documents/{id}/post-issue — Không dùng idempotency key */
    public static Response postIssue(String token, String id) {
        return postIssue(token, id, null);
    }

    /** POST /outbound-documents/{id}/record-loss — Ghi nhận hao hụt khi xuất */
    public static Response recordLoss(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.OUTBOUND_RECORD_LOSS, id), token, payload);
    }

    /** POST /outbound-documents/{id}/reject — Từ chối phiếu xuất */
    public static Response rejectOutbound(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.OUTBOUND_REJECT, id), token, payload);
    }

    /** POST /outbound-documents/{id}/cancel — Hủy phiếu xuất */
    public static Response cancelOutbound(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.OUTBOUND_CANCEL, id), token, null);
    }
}
