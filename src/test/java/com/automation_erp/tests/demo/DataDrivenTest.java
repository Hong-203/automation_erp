package com.automation_erp.tests.demo;

import com.automation_erp.framework.utils.CsvUtils;
import com.automation_erp.framework.utils.JsonReader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import com.automation_erp.framework.config.ConfigReader;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

// Import tĩnh AssertJ Assertion API
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class demo trình bày phương pháp thực hiện Data-driven testing 
 * kết hợp với thư viện viết Assert dạng fluent (AssertJ).
 */
public class DataDrivenTest {

    @BeforeClass
    public void checkEnabled() {
        String runDemo = ConfigReader.getProperty("run.demo.tests");
        if (runDemo == null || !Boolean.parseBoolean(runDemo.trim())) {
            throw new SkipException("Bỏ qua chạy demo Data Driven Test vì run.demo.tests = false");
        }
    }

    // =====================================================================
    // 1. Data Provider đọc từ CSV
    // =====================================================================

    @DataProvider(name = "csvUserData")
    public Object[][] getCsvUserData() {
        return CsvUtils.readCsvData("src/test/resources/testdata/users.csv");
    }

    @Test(dataProvider = "csvUserData", description = "Demo đọc test data từ file CSV và assert bằng AssertJ")
    public void testLoginWithCsvData(String username, String password, String role) {
        System.out.printf("[Demo CSV] Đọc thành công dòng: User='%s' | Pass='%s' | Role='%s'%n", 
                username, password, role);

        // Sử dụng AssertJ viết câu lệnh assert mạch lạc, tự nhiên (Fluent API)
        assertThat(username)
                .as("Tên tài khoản không được rỗng")
                .isNotNull()
                .isNotEmpty()
                .contains("@");

        assertThat(password)
                .as("Mật khẩu không được để trắng")
                .isNotBlank();

        assertThat(role)
                .as("Quyền người dùng phải thuộc danh sách hợp lệ")
                .isIn("admin", "staff", "guest");
    }

    // =====================================================================
    // 2. Data Provider đọc từ JSON
    // =====================================================================

    @DataProvider(name = "jsonWarehouseData")
    public Object[][] getJsonWarehouseData() {
        List<Map<String, Object>> list = JsonReader.readJsonData("src/test/resources/testdata/warehouses.json");
        Object[][] data = new Object[list.size()][1];
        for (int i = 0; i < list.size(); i++) {
            data[i][0] = list.get(i);
        }
        return data;
    }

    @Test(dataProvider = "jsonWarehouseData", description = "Demo đọc test data từ file JSON và assert bằng AssertJ")
    public void testWarehouseWithJsonData(Map<String, Object> warehouse) {
        String code = (String) warehouse.get("code");
        String name = (String) warehouse.get("name");
        String type = (String) warehouse.get("type");

        System.out.printf("[Demo JSON] Đọc thành công Kho: Code='%s' | Name='%s' | Type='%s'%n", 
                code, name, type);

        // AssertJ hỗ trợ các phán đoán dữ liệu kiểu chuỗi rất phong phú
        assertThat(code)
                .as("Mã kho phải bắt đầu bằng WH-")
                .startsWith("WH-");

        assertThat(name)
                .as("Tên kho phải chứa chữ 'Kho'")
                .contains("Kho");

        assertThat(type)
                .as("Loại kho phải hợp lệ")
                .isNotNull()
                .isIn("distribution", "fulfillment");
    }
}
