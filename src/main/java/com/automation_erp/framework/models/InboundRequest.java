package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body cho API tạo phiếu nhập kho (POST /inbound-documents).
 *
 * Ví dụ sử dụng:
 *   InboundRequest req = InboundRequest.builder()
 *       .warehouseCode("WH-MAIN")
 *       .items(List.of(ItemDetail.builder().sku("SKU-X").quantity(50).price(1200.0).build()))
 *       .notes("Nhập kho tự động - Test")
 *       .build();
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundRequest {

    /**
     * Mã kho nhận hàng - bắt buộc.
     * MVP: chỉ kho tổng (MAIN) được nhập hàng từ nhà cung cấp.
     */
    @JsonProperty("warehouse_code")
    private String warehouseCode;

    /**
     * ID nhà cung cấp - tùy chọn trong MVP.
     * Khi null, hệ thống tạo phiếu mà không liên kết nhà cung cấp.
     */
    @JsonProperty("supplier_id")
    private Integer supplierId;

    /**
     * Số tham chiếu đơn hàng mua / PO number - tùy chọn.
     */
    @JsonProperty("reference_no")
    private String referenceNo;

    /**
     * Danh sách sản phẩm nhập kho - bắt buộc, phải có ít nhất 1 dòng.
     */
    private List<ItemDetail> items;

    /**
     * Ghi chú chứng từ - tùy chọn.
     */
    private String notes;
}
