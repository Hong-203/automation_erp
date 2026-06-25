package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {
    @JsonProperty("company_id")
    private Integer companyId;

    @JsonProperty("branch_id")
    private Integer branchId;

    @JsonProperty("parent_id")
    private Integer parentId;

    private String code;
    private String name;

    @JsonProperty("warehouse_type")
    private String warehouseType;

    private String address;

    @JsonProperty("manager_id")
    private Integer managerId;

    private Integer status;
}
