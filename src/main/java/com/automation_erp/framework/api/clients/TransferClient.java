package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.constants.ApiEndpoints;
import com.automation_erp.framework.models.TransferRequest;
import io.restassured.response.Response;

import java.util.Map;

/**
 * API Client cho nhóm Transfer Order endpoints:
 *   GET/POST /transfer-orders
 *   GET /transfer-orders/{id}
 *   POST /transfer-orders/{id}/submit
 *   POST /transfer-orders/{id}/approve
 *   POST /transfer-orders/{id}/dispatch
 *   POST /transfer-orders/{id}/receive
 *   POST /transfer-orders/{id}/record-loss
 *   POST /transfer-orders/{id}/return
 *   POST /transfer-orders/{id}/reject
 *   POST /transfer-orders/{id}/cancel
 *
 * State Machine đầy đủ:
 *   Nháp → (submit) → Chờ duyệt → (approve) → Đã duyệt
 *     → (dispatch) → Đang vận chuyển [In-Transit tăng, Available nguồn giảm]
 *       → (receive) → Hoàn tất [In-Transit giảm, Available đích tăng]
 *       → (return) → Bị trả lại [In-Transit giảm, Available nguồn hoàn lại]
 *   Nháp/Chờ duyệt → (cancel) → Hủy
 *   Chờ duyệt → (reject) → Bị từ chối
 */
public class TransferClient {

    private TransferClient() {}

    // =====================================================================
    // CRUD
    // =====================================================================

    /** POST /transfer-orders — Tạo phiếu điều chuyển kho (trạng thái Nháp) */
    public static Response createTransfer(String token, TransferRequest payload) {
        return ApiClient.post(ApiEndpoints.TRANSFER_ORDERS, token, payload);
    }

    /** GET /transfer-orders — Lấy danh sách phiếu điều chuyển */
    public static Response getTransferList(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.TRANSFER_ORDERS, token, queryParams);
    }

    /** GET /transfer-orders — Không filter */
    public static Response getTransferList(String token) {
        return getTransferList(token, null);
    }

    /** GET /transfer-orders/{id} — Lấy chi tiết phiếu điều chuyển */
    public static Response getTransferById(String token, String id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.TRANSFER_BY_ID, id), token, null);
    }

    // =====================================================================
    // State Machine Actions
    // =====================================================================

    /** POST /transfer-orders/{id}/submit — Gửi phiếu chờ duyệt */
    public static Response submitTransfer(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_SUBMIT, id), token, null);
    }

    /** POST /transfer-orders/{id}/approve — Quản lý phê duyệt phiếu điều chuyển */
    public static Response approveTransfer(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_APPROVE, id), token, null);
    }

    /**
     * POST /transfer-orders/{id}/dispatch — Xuất kho nguồn, hàng chuyển sang In-Transit.
     * Sau khi gọi:
     *   - Available stock tại kho nguồn GIẢM
     *   - In-Transit stock TĂNG
     *   - Trạng thái phiếu → "Đang vận chuyển"
     */
    public static Response dispatchTransfer(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_DISPATCH, id), token, null);
    }

    /**
     * POST /transfer-orders/{id}/receive — Nhận hàng tại kho đích.
     * Sau khi gọi:
     *   - In-Transit stock GIẢM
     *   - Available stock tại kho đích TĂNG
     *   - Trạng thái phiếu → "Hoàn tất"
     * @param idempotencyKey UUID đảm bảo không nhận hàng 2 lần
     */
    public static Response receiveTransfer(String token, String id, String idempotencyKey) {
        return ApiClient.post(
            ApiEndpoints.path(ApiEndpoints.TRANSFER_RECEIVE, id),
            token,
            null,
            idempotencyKey
        );
    }

    /** POST /transfer-orders/{id}/receive — Không dùng idempotency key */
    public static Response receiveTransfer(String token, String id) {
        return receiveTransfer(token, id, null);
    }

    /**
     * POST /transfer-orders/{id}/record-loss — Ghi nhận hao hụt trong quá trình vận chuyển.
     * Hàng hao hụt sẽ bị trừ khỏi In-Transit nhưng không cộng vào kho đích.
     */
    public static Response recordLoss(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_RECORD_LOSS, id), token, payload);
    }

    /**
     * POST /transfer-orders/{id}/return — Trả lại điều chuyển (kho đích từ chối nhận).
     * Sau khi gọi:
     *   - In-Transit stock GIẢM
     *   - Available stock tại kho NGUỒN hoàn lại
     *   - Trạng thái phiếu → "Bị trả lại"
     */
    public static Response returnTransfer(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_RETURN, id), token, null);
    }

    /** POST /transfer-orders/{id}/reject — Từ chối phiếu điều chuyển */
    public static Response rejectTransfer(String token, String id, Object payload) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_REJECT, id), token, payload);
    }

    /** POST /transfer-orders/{id}/cancel — Hủy phiếu điều chuyển (chỉ hủy được trước khi dispatch) */
    public static Response cancelTransfer(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.TRANSFER_CANCEL, id), token, null);
    }
}
