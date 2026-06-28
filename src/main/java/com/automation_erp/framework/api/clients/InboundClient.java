package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.constants.ApiEndpoints;
import com.automation_erp.framework.models.InboundRequest;
import io.restassured.response.Response;

import java.util.Map;

/**
 * API Client cho nhóm Inbound Document endpoints:
 *   GET/POST /inbound-documents
 *   GET /inbound-documents/{id}
 *   POST /inbound-documents/{id}/submit
 *   POST /inbound-documents/{id}/approve
 *   POST /inbound-documents/{id}/post-receipt
 *   POST /inbound-documents/{id}/record-loss
 *   POST /inbound-documents/{id}/reject
 *   POST /inbound-documents/{id}/cancel
 *
 * State Machine:
 *   Nháp → (submit) → Chờ duyệt → (approve) → Đã duyệt → (post-receipt) → Đã nhập kho
 *   Nháp/Chờ duyệt → (cancel) → Hủy
 *   Chờ duyệt → (reject) → Bị từ chối
 */
public class InboundClient {

    private InboundClient() {}

    // =====================================================================
    // CRUD
    // =====================================================================

    /** POST /inbound-documents — Tạo phiếu nhập kho (trạng thái Nháp) */
    public static Response createInbound(String token, InboundRequest payload) {
        return ApiClient.post(ApiEndpoints.INBOUND_DOCUMENTS, token, payload);
    }

    /** GET /inbound-documents — Lấy danh sách phiếu nhập kho */
    public static Response getInboundList(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.INBOUND_DOCUMENTS, token, queryParams);
    }

    /** GET /inbound-documents — Không filter */
    public static Response getInboundList(String token) {
        return getInboundList(token, null);
    }

    /** PATCH /inbound-documents/{id} — Cập nhật thông tin phiếu nháp */
    public static Response updateInbound(String token, String id, Object payload) {
        return ApiClient.patch(ApiEndpoints.path(ApiEndpoints.INBOUND_BY_ID, id), token, payload);
    }

    /** GET /inbound-documents/{id} — Lấy chi tiết phiếu nhập */
    public static Response getInboundById(String token, String id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.INBOUND_BY_ID, id), token, null);
    }

    // =====================================================================
    // State Machine Actions
    // =====================================================================

    /** POST /inbound-documents/{id}/submit — Gửi phiếu chờ duyệt */
    public static Response submitInbound(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_SUBMIT, id), token, null);
    }

    /** POST /inbound-documents/{id}/approve — Quản lý phê duyệt phiếu */
    public static Response approveInbound(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_APPROVE, id), token, null);
    }

    /**
     * POST /inbound-documents/{id}/post-receipt — Thực hiện nhập kho thực tế.
     * Cập nhật tồn kho khả dụng tăng lên.
     * @param idempotencyKey UUID để đảm bảo tính idempotent (tránh nhập kho 2 lần)
     */
    public static Response postReceipt(String token, String id, String idempotencyKey) {
        return ApiClient.post(
            ApiEndpoints.path(ApiEndpoints.INBOUND_POST_RECEIPT, id),
            token,
            null,
            idempotencyKey
        );
    }

    /** POST /inbound-documents/{id}/post-receipt — Không dùng idempotency key */
    public static Response postReceipt(String token, String id) {
        return postReceipt(token, id, null);
    }
    /** POST /inbound-documents/{id}/record-loss — Ghi nhận hao hụt khi nhập kho */
    public static Response recordLoss(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_RECORD_LOSS, id), token, payload);
    }

    /** POST /inbound-documents/{id}/post-receipt — Thực tế nhập kho */
    public static Response postReceipt(String token, String id, String idempotencyKey, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_POST_RECEIPT, id), token, payload, idempotencyKey);
    }

    /** POST /inbound-documents/{id}/post-receipt — Không dùng idempotency key */
    public static Response postReceipt(String token, String id, Object payload) {
        return postReceipt(token, id, null, payload);
    }

    /** POST /inbound-documents/{id}/reject — Từ chối phiếu nhập */
    public static Response rejectInbound(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_REJECT, id), token, payload);
    }

    /** POST /inbound-documents/{id}/cancel — Hủy phiếu nhập (chỉ được hủy khi chưa hoàn tất) */
    public static Response cancelInbound(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_CANCEL, id), token, payload);
    }
}
