package com.automation_erp.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        properties = new Properties();
        String env = System.getProperty("env");
        String fileName = "config.properties";
        if (env != null && !env.trim().isEmpty()) {
            fileName = "config-" + env.trim().toLowerCase() + ".properties";
        }
        
        String path = "src/test/resources/" + fileName;
        System.out.println("[ConfigReader] Đang tải cấu hình môi trường từ: " + path);
        
        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
        } catch (IOException e) {
            if (!"config.properties".equals(fileName)) {
                System.err.println("[ConfigReader] [Cảnh báo] Không thể tải " + path + ". Đang fallback tải config.properties...");
                try (FileInputStream fallbackInput = new FileInputStream("src/test/resources/config.properties")) {
                    properties.load(fallbackInput);
                } catch (IOException ex) {
                    throw new RuntimeException("Không thể load file config.properties mặc định", ex);
                }
            } else {
                throw new RuntimeException("Không thể load file config.properties mặc định", e);
            }
        }
    }

    public static String getProperty(String key) {
        
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.trim().isEmpty()) {
            return systemProp;
        }
        return properties.getProperty(key);
    }

    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public static String getExecutionType() {
        return getProperty("execution.type");
    }

    public static String getBrowser() {
        return getProperty("browser");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless"));
    }

    public static int getTimeout() {
        return Integer.parseInt(getProperty("timeout.seconds"));
    }

    // =====================================================================
    // Default Entity IDs (Master Data)
    // =====================================================================

    /** ID kho mặc định dùng trong test (cấu hình tại default.warehouse.id) */
    public static int getDefaultWarehouseId() {
        String val = getProperty("default.warehouse.id");
        if (val == null || val.trim().isEmpty()) {
            throw new RuntimeException("[ConfigReader] Thiếu key 'default.warehouse.id' trong config.properties");
        }
        return Integer.parseInt(val.trim());
    }

    /** ID nhà cung cấp mặc định dùng trong test (cấu hình tại default.supplier.id) */
    public static int getDefaultSupplierId() {
        String val = getProperty("default.supplier.id");
        if (val == null || val.trim().isEmpty()) {
            throw new RuntimeException("[ConfigReader] Thiếu key 'default.supplier.id' trong config.properties");
        }
        return Integer.parseInt(val.trim());
    }

    /** ID sản phẩm mặc định dùng trong test (cấu hình tại default.product.id) */
    public static int getDefaultProductId() {
        String val = getProperty("default.product.id");
        if (val == null || val.trim().isEmpty()) {
            throw new RuntimeException("[ConfigReader] Thiếu key 'default.product.id' trong config.properties");
        }
        return Integer.parseInt(val.trim());
    }
}
