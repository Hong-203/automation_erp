package com.automation_erp.framework.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * Hỗ trợ tìm tệp trong classpath (ưu tiên) hoặc fallback đường dẫn tệp tuyệt đối/tương đối.
     * Cho phép các cột chứa dấu phẩy nằm trong dấu ngoặc kép (ví dụ: "Laptop A, cấu hình mạnh").
     *
     * @param filePath Đường dẫn hoặc tên file (ví dụ: "testdata/users.csv" hoặc "src/test/resources/testdata/users.csv")
     * @return Mảng 2 chiều chứa các dòng dữ liệu
     */
    public static Object[][] readCsvData(String filePath) {
        List<String[]> records = new ArrayList<>();
        
        // Trích xuất tên tệp tương đối nếu truyền kèm đường dẫn maven tĩnh
        String relativePath = filePath;
        if (filePath.contains("src/test/resources/")) {
            relativePath = filePath.substring(filePath.indexOf("src/test/resources/") + "src/test/resources/".length());
        }

        // Bước 1: Thử đọc từ ClassLoader
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath)) {
            if (is != null) {
                parseCsv(new BufferedReader(new InputStreamReader(is)), records);
            } else {
                // Bước 2: Fallback đọc bằng file hệ thống nếu không nằm trong classpath
                try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                    parseCsv(br, records);
                }
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

    private static void parseCsv(BufferedReader reader, List<String[]> records) throws IOException {
        String line;
        boolean isHeader = true;
        while ((line = reader.readLine()) != null) {
            if (isHeader) {
                isHeader = false; // Bỏ qua dòng tiêu đề
                continue;
            }
            // Regex phân tích CSV nâng cao: Bỏ qua dấu phẩy nằm bên trong cặp ngoặc kép
            String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            
            // Làm sạch dữ liệu (trim và loại bỏ dấu ngoặc kép thừa)
            for (int i = 0; i < values.length; i++) {
                String val = values[i].trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                values[i] = val;
            }
            records.add(values);
        }
    }
}
