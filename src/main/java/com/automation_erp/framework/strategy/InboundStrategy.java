package com.automation_erp.framework.strategy;

import java.util.Map;

/**
 * Interface chiến dịch kiểm thử cho riêng nghiệp vụ Nhập kho (Inbound).
 */
public interface InboundStrategy {
    String executeInboundFlow(Map<String, Object> testData);
}
