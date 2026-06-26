package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body cho API tạo phiếu điều chuyển kho (POST /transfer-orders).
 *
 * Ví dụ sử dụng:
 *   TransferRequest req = TransferRequest.builder()
 *       .sourceWarehouseCode("WH-MAIN")
 *       .targetWarehouseCode("WH-BRANCH-01")
 *       .items(List.of(ItemDetail.builder().sku("SKU-X").quantity(30).build()))
 *       .notes("Điều chuyển tự động - Test")
 *       .build();
 *
 * Lưu ý nghiệp vụ:
 *   - sourceWarehouseCode KHÁC targetWarehouseCode (validate ngay khi tạo phiếu).
 *   - Số lượng điều chuyển KHÔNG vượt quá available stock tại kho nguồn.
 *   - Sau khi dispatch: tồn kho nguồn giảm, In-Transit tăng.
 *   - Sau khi receive: In-Transit giảm, tồn kho đích tăng.
 *   - In-Transit stock KHÔNG được xuất hoặc điều chuyển tiếp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    /**
     * Mã kho nguồn (kho xuất hàng để điều chuyển) - bắt buộc.
     */
    @JsonProperty("source_warehouse_code")
    private String sourceWarehouseCode;

    /**
     * Mã kho đích (kho nhận hàng điều chuyển) - bắt buộc.
     * PHẢI khác sourceWarehouseCode.
     */
    @JsonProperty("target_warehouse_code")
    private String targetWarehouseCode;

    /**
     * Danh sách sản phẩm điều chuyển - bắt buộc, phải có ít nhất 1 dòng.
     */
    private List<ItemDetail> items;

    /**
     * Ghi chú chứng từ - tùy chọn.
     */
    private String notes;
}
