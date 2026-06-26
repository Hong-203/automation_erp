package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dòng chi tiết sản phẩm dùng chung cho Inbound, Outbound và Transfer request.
 *
 * Dùng @Builder để tạo instance:
 *   ItemDetail item = ItemDetail.builder()
 *       .sku("SKU-IPHONE15")
 *       .quantity(50)
 *       .price(1200.0)
 *       .build();
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetail {

    /** Mã sản phẩm (SKU) - bắt buộc */
    private String sku;

    /** Số lượng - bắt buộc, phải > 0 */
    private Integer quantity;

    /**
     * Đơn giá nhập - chỉ dùng cho Inbound.
     * MVP: lưu để phục vụ mở rộng sau, không ảnh hưởng tồn kho.
     * Có thể null cho Outbound và Transfer.
     */
    private Double price;

    /** Ghi chú cho dòng sản phẩm (tùy chọn) */
    @JsonProperty("line_note")
    private String lineNote;
}
