package com.automation_erp.framework.strategy;

import java.util.Map;

/**
 * Interface chiến dịch kiểm thử cho riêng nghiệp vụ Điều chuyển kho (Transfer).
 */
public interface TransferStrategy {
    String executeTransferFlow(Map<String, Object> testData);
}
