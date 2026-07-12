package com.automation_erp.framework.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Listener quản lý cơ chế chạy lại (Retry) test case khi gặp lỗi mạng/môi trường chập chờn.
 * Số lần chạy lại tối đa mặc định là 3.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int count = 0;
    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess()) {
            if (count < MAX_RETRY_COUNT) {
                count++;
                System.err.printf("[RetryAnalyzer] Test '%s' lỗi. Đang chạy lại lần %d/%d...%n",
                        result.getMethod().getMethodName(), count, MAX_RETRY_COUNT);
                return true;
            }
        }
        return false;
    }
}
