package com.automation_erp.framework.strategy;

import com.automation_erp.framework.config.ConfigReader;

public class StrategyFactory {

    public static WorkFlowStrategy getStrategy() {
        String executionType = ConfigReader.getExecutionType().toUpperCase();

        switch (executionType) {
            case "API":
                return new ApiWorkFlowStrategy();
            case "UI":
                return new UiWorkFlowStrategy();
            default:
                throw new IllegalArgumentException("Unsupported execution type in config: " + executionType);
        }
    }

    /** Lấy strategy chuyên biệt cho nghiệp vụ Nhập kho (Inbound) */
    public static InboundStrategy getInboundStrategy() {
        return getStrategy();
    }

    /** Lấy strategy chuyên biệt cho nghiệp vụ Xuất kho (Outbound) */
    public static OutboundStrategy getOutboundStrategy() {
        return getStrategy();
    }

    /** Lấy strategy chuyên biệt cho nghiệp vụ Điều chuyển kho (Transfer) */
    public static TransferStrategy getTransferStrategy() {
        return getStrategy();
    }
}
