package com.automation_erp.framework.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tiện ích đọc dữ liệu từ file CSV để nạp vào DataProvider của TestNG.
 */
public class CsvUtils {

    private CsvUtils() {}

    /**
     * Đọc file CSV và trả về mảng 2 chiều Object[][] phục vụ cho DataProvider.
     * Tự động bỏ qua dòng tiêu đề (header) đầu tiên.
     *
     * @param filePath Đường dẫn tương đối hoặc tuyệt đối tới file CSV
     * @return Mảng 2 chiều chứa các dòng dữ liệu
     */
    public static Object[][] readCsvData(String filePath) {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Bỏ qua dòng tiêu đề
                    continue;
                }
                // Tách cột bằng dấu phẩy
                String[] values = line.split(",");
                // Trim khoảng trắng thừa ở mỗi cột
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
                records.add(values);
            }
        } catch (IOException e) {
            throw new RuntimeException("[CsvUtils] Không thể đọc file CSV tại: " + filePath, e);
        }

        Object[][] data = new Object[records.size()][];
        for (int i = 0; i < records.size(); i++) {
            data[i] = records.get(i);
        }
        return data;
    }
}
