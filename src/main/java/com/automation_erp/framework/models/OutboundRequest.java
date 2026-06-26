package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body cho API tạo phiếu xuất kho (POST /outbound-documents).
 *
 * Ví dụ sử dụng:
 *   OutboundRequest req = OutboundRequest.builder()
 *       .warehouseCode("WH-MAIN")
 *       .reason("Xuất bán hàng - Đơn #1234")
 *       .items(List.of(ItemDetail.builder().sku("SKU-X").quantity(10).build()))
 *       .notes("Xuất kho tự động - Test")
 *       .build();
 *
 * Lưu ý nghiệp vụ:
 *   - Số lượng xuất KHÔNG được vượt quá available stock tại kho.
 *   - In-Transit stock KHÔNG được phép xuất.
 *   - Kho phải đang ở trạng thái active (status = 1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundRequest {

    /**
     * Mã kho xuất hàng - bắt buộc.
     */
    @JsonProperty("warehouse_code")
    private String warehouseCode;

    /**
     * Lý do xuất kho - tùy chọn hoặc bắt buộc tùy cấu hình hệ thống.
     * VD: "Xuất bán hàng", "Xuất điều phối nội bộ"
     */
    private String reason;

    /**
     * ID đơn hàng / chứng từ nguồn - tùy chọn.
     */
    @JsonProperty("reference_id")
    private String referenceId;

    /**
     * Số tham chiếu - tùy chọn.
     */
    @JsonProperty("reference_no")
    private String referenceNo;

    /**
     * Danh sách sản phẩm xuất kho - bắt buộc, phải có ít nhất 1 dòng.
     * price trong ItemDetail không cần thiết cho Outbound.
     */
    private List<ItemDetail> items;

    /**
     * Ghi chú chứng từ - tùy chọn.
     */
    private String notes;
}
