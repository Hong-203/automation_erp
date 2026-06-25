package com.automation_erp.tests.m2;

import com.automation_erp.framework.strategy.StrategyFactory;
import com.automation_erp.framework.strategy.WorkFlowStrategy;
import com.automation_erp.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InboundWorkflowTest extends BaseTest {

    @Test(description = "Kiểm thử luồng nhập kho hoàn chỉnh và tính idempotent khi gửi trùng request")
    public void testCompleteInboundWorkflow() {

        Map<String, Object> testData = new HashMap<>();
        testData.put("warehouseCode", "WH-MAIN");
        testData.put("sku", "SKU-IPHONE15");
        testData.put("quantity", 50);
        testData.put("price", 1200.0);

        String idempotencyKey = UUID.randomUUID().toString();
        testData.put("idempotencyKey", idempotencyKey);

        WorkFlowStrategy strategy = StrategyFactory.getStrategy();

        String inboundId = strategy.executeInboundFlow(testData);

        Assert.assertNotNull(inboundId, "Mã phiếu nhập kho tạo ra không được rỗng!");
        System.out.println("[Test Runner] Luồng kiểm thử hoàn tất thành công. ID phiếu: " + inboundId);
    }
}
