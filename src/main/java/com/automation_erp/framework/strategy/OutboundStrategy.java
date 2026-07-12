package com.automation_erp.framework.strategy;

import java.util.Map;

/**
 * Interface chiến dịch kiểm thử cho riêng nghiệp vụ Xuất kho (Outbound).
 */
public interface OutboundStrategy {
    String executeOutboundFlow(Map<String, Object> testData);
}
