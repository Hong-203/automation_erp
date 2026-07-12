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
     *
     * @param filePath Đường dẫn tương đối hoặc tuyệt đối tới file JSON
     * @return Danh sách các bản ghi dạng Map
     */
    public static List<Map<String, Object>> readJsonData(String filePath) {
        try {
            return mapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("[JsonReader] Không thể đọc file JSON tại: " + filePath, e);
        }
    }
}
