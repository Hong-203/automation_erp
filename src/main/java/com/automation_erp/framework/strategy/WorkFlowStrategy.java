package com.automation_erp.framework.strategy;

import java.util.Map;

public interface WorkFlowStrategy {
    
    String executeInboundFlow(Map<String, Object> testData);

    
    String executeOutboundFlow(Map<String, Object> testData);

    
    String executeTransferFlow(Map<String, Object> testData);
}
