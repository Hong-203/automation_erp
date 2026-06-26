package com.automation_erp.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Tiện ích xử lý JSON: đọc file JSON test data, serialize/deserialize object.
 * Dùng Jackson ObjectMapper làm engine chính.
 */
public final class JsonUtils {

    private JsonUtils() {}

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // =====================================================================
    // Đọc file JSON → Object
    // =====================================================================

    /**
     * Đọc file JSON và deserialize thành object của kiểu T.
     *
     * VD: InboundRequest req = JsonUtils.readFromFile(
     *         "src/test/resources/data/inbound_data.json", InboundRequest.class);
     */
    public static <T> T readFromFile(String filePath, Class<T> clazz) {
        try {
            return MAPPER.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file JSON: " + filePath, e);
        }
    }

    /**
     * Đọc file JSON thành Map<String, Object> (dùng khi không có POJO).
     */
    public static Map<String, Object> readMapFromFile(String filePath) {
        try {
            return MAPPER.readValue(new File(filePath), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file JSON thành Map: " + filePath, e);
        }
    }

    // =====================================================================
    // Object → JSON String
    // =====================================================================

    /**
     * Serialize object thành chuỗi JSON (pretty printed).
     */
    public static String toJsonString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("Không thể serialize object thành JSON", e);
        }
    }

    // =====================================================================
    // JSON String → Object
    // =====================================================================

    /**
     * Deserialize chuỗi JSON thành object của kiểu T.
     *
     * VD: WarehouseRequest req = JsonUtils.fromJsonString(jsonStr, WarehouseRequest.class);
     */
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Không thể deserialize JSON string thành " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Deserialize chuỗi JSON thành Map<String, Object>.
     */
    public static Map<String, Object> fromJsonString(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Không thể deserialize JSON string thành Map", e);
        }
    }

    // =====================================================================
    // Lấy giá trị field từ JSON response string
    // =====================================================================

    /**
     * Lấy giá trị của một field từ JSON response string.
     * VD: getField(responseBody, "data.id")  → trả về String
     *
     * Lưu ý: chỉ hỗ trợ path 1 cấp, dùng RestAssured jsonPath() cho path phức tạp hơn.
     */
    public static String getField(String json, String field) {
        try {
            Map<String, Object> map = fromJsonString(json);
            Object value = map.get(field);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
