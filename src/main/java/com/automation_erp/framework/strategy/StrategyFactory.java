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
}
