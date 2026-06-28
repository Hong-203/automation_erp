package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboundRequest {

    @JsonProperty("doc_no")
    private String docNo;

    @JsonProperty("doc_date")
    private String docDate;

    @JsonProperty("dst_warehouse_id")
    private Integer dstWarehouseId;

    @JsonProperty("supplier_id")
    private Integer supplierId;

    private String note;

    private List<ItemDetail> lines;
}
