package com.automation_erp.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tiện ích đọc dữ liệu test dạng JSON bằng Jackson ObjectMapper.
 */
public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonReader() {}

    /**
     * Đọc file JSON danh sách các object và trả về List Map.
     * Hỗ trợ tìm tệp trong classpath (ưu tiên) hoặc fallback đường dẫn tệp tuyệt đối/tương đối.
     *
     * @param filePath Đường dẫn tương đối hoặc tuyệt đối tới file JSON
     * @return Danh sách các bản ghi dạng Map
     */
    public static List<Map<String, Object>> readJsonData(String filePath) {
        String relativePath = filePath;
        if (filePath.contains("src/test/resources/")) {
            relativePath = filePath.substring(filePath.indexOf("src/test/resources/") + "src/test/resources/".length());
        }

        try (java.io.InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath)) {
            if (is != null) {
                return mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                // Fallback đọc file hệ thống trực tiếp
                return mapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (IOException e) {
            throw new RuntimeException("[JsonReader] Không thể đọc file JSON tại: " + filePath, e);
        }
    }
}
