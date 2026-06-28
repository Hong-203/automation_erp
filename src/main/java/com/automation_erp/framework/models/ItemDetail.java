package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemDetail {

    // ID của dòng, dùng khi update phiếu
    private Integer id;

    // ID của dòng, dùng riêng cho API post-receipt/dispatch
    @JsonProperty("line_id")
    private Integer lineId;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("qty_planned")
    private Integer qtyPlanned;

    @JsonProperty("qty_actual")
    private Integer qtyActual;

    @JsonProperty("unit_cost")
    private Double unitCost;
}
