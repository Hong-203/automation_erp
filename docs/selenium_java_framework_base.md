# Kiến Trúc Framework Kiểm Thử Tự Động Hóa (Selenium + RestAssured + Java)
## Thiết kế theo Strategy Pattern — Sẵn sàng mở rộng đa Module và đa Phương Thức (UI/API)

Tài liệu này mô tả kiến trúc **hiện tại** của framework kiểm thử tự động hóa, bao gồm cách vận hành, cách sử dụng từng thành phần và hướng dẫn chi tiết để triển khai tính năng mới mà **không cần sửa code lõi (core framework)**.

---

## 1. Nguyên Tắc Thiết Kế Cốt Lõi

| Pattern | Mục đích |
|---|---|
| **Strategy Pattern** | Test class không biết đang chạy UI hay API. `StrategyFactory` quyết định dựa vào `execution.type` trong config. |
| **Factory Pattern** | `DriverFactory` khởi tạo browser. `StrategyFactory` khởi tạo strategy tương ứng. |
| **ThreadLocal** | `DriverManager` + `ExtentReportManager` đảm bảo an toàn khi chạy song song (parallel-safe). |
| **Page Object Model (POM)** | Tất cả tương tác UI bọc trong Page class. Test class không chứa locator hay Selenium code trực tiếp. |
| **Singleton** | `ConfigReader` và `ExtentReportManager` đọc/khởi tạo một lần duy nhất. |
| **Builder (Lombok)** | Tất cả Request model dùng `@Builder` → code test ngắn gọn, type-safe, có auto-complete. |

---

## 2. Cấu Trúc Thư Mục Đầy Đủ (Hiện Tại)

```text
automation_erp/
├── pom.xml                              # Maven: Selenium, RestAssured, TestNG, Lombok, ExtentReports
├── docs/                                # Tài liệu dự án
│
└── src/
    ├── main/java/com/automation_erp/framework/
    │   │
    │   ├── config/
    │   │   └── ConfigReader.java        # Đọc config.properties (Singleton, override bằng -D param)
    │   │
    │   ├── constants/                   # Hằng số toàn dự án - KHÔNG hardcode string trong code
    │   │   ├── ApiEndpoints.java        # Toàn bộ URL path + helper path(template, args...)
    │   │   ├── HttpStatus.java          # 200, 201, 400, 401, 403, 422...
    │   │   ├── DocumentStatus.java      # "Nháp", "Chờ duyệt", "Đã duyệt", "Hoàn tất"...
    │   │   └── WarehouseType.java       # "main", "branch", "sub", STATUS_ACTIVE/INACTIVE
    │   │
    │   ├── utils/                       # Tiện ích dùng chung
    │   │   ├── DataGenerator.java       # Sinh mã kho/SKU/UUID ngẫu nhiên theo timestamp
    │   │   ├── JsonUtils.java           # Đọc file JSON, serialize/deserialize object
    │   │   ├── DateUtils.java           # Format datetime, tính khoảng thời gian cho filter params
    │   │   └── AssertionUtils.java      # Bọc TestNG Assert với message log rõ ràng
    │   │
    │   ├── driver/
    │   │   ├── DriverFactory.java       # Tạo WebDriver (Chrome/Firefox/Edge, headless mode)
    │   │   └── DriverManager.java       # ThreadLocal<WebDriver> — parallel-safe
    │   │
    │   ├── api/
    │   │   ├── ApiClient.java           # HTTP wrapper: GET/POST/PUT/PATCH/DELETE + login()
    │   │   └── clients/                 # Client theo nhóm endpoint (1 class per nhóm)
    │   │       ├── WarehouseClient.java # CRUD + enable/disable kho
    │   │       ├── InboundClient.java   # Nhập kho: create/submit/approve/post-receipt/reject/cancel
    │   │       ├── OutboundClient.java  # Xuất kho: create/submit/approve/post-issue/reject/cancel
    │   │       ├── TransferClient.java  # Điều chuyển: create/submit/approve/dispatch/receive/return/cancel
    │   │       └── InventoryClient.java # Tồn kho, In-Transit, Stock Movements, Reports
    │   │
    │   ├── models/                      # Request body POJO — Lombok @Builder @Data
    │   │   ├── WarehouseRequest.java
    │   │   ├── ItemDetail.java          # Dòng sản phẩm dùng chung cho Inbound/Outbound/Transfer
    │   │   ├── InboundRequest.java
    │   │   ├── OutboundRequest.java
    │   │   └── TransferRequest.java
    │   │
    │   ├── pages/                       # Page Object Models — UI Testing
    │   │   ├── BasePage.java            # Wrapper đầy đủ: wait, click, input, dropdown, scroll, alert...
    │   │   ├── LoginPage.java
    │   │   └── InboundPage.java
    │   │
    │   ├── strategy/
    │   │   ├── WorkFlowStrategy.java    # Interface: executeInboundFlow / executeOutboundFlow / executeTransferFlow
    │   │   ├── ApiWorkFlowStrategy.java # Implement qua RestAssured
    │   │   ├── UiWorkFlowStrategy.java  # Implement qua Selenium
    │   │   └── StrategyFactory.java     # Đọc config → trả về đúng Strategy
    │   │
    │   ├── listeners/
    │   │   └── TestListener.java        # ITestListener: log pass/fail/skip + thời gian chạy
    │   │
    │   └── reporters/
    │       └── ExtentReportManager.java # ExtentReports v5: Singleton + ThreadLocal, dark theme HTML
    │
    └── test/
        ├── java/com/automation_erp/tests/
        │   ├── BaseTest.java            # @BeforeMethod/@AfterMethod: khởi tạo driver, teardown
        │   ├── fixtures/                # Test Data Isolation
        │   │   ├── WarehouseFixture.java # Tạo kho test + tự động cleanup
        │   │   └── ProductFixture.java  # Quản lý SKU test
        │   ├── m1/
        │   │   └── WarehouseTest.java
        │   └── m2/
        │       └── InboundWorkflowTest.java
        │
        └── resources/
            ├── config.properties        # ⚠️ KHÔNG commit — copy từ config.properties.example
            ├── config.properties.example
            ├── testng.xml               # Suite config: parallel, listener registration
            └── log4j2.xml
```

---

## 3. Cách Chạy

### 3.1. Chuẩn bị lần đầu

```bash
# Bước 1: Copy file cấu hình mẫu
copy src\test\resources\config.properties.example src\test\resources\config.properties

# Bước 2: Điền thông tin thật vào config.properties
# Xem mục 3.2 bên dưới
```

### 3.2. Nội dung `config.properties`

```properties
# URL hệ thống
base.url=https://your-erp-url.com
api.base.url=https://your-erp-url.com/api/v1

# Chế độ chạy
execution.type=API      # API hoặc UI
api.mock=false          # true = giả lập, không gọi API thật
ui.mock=false

# Browser (chỉ dùng khi execution.type=UI)
browser=chrome
headless=false
timeout.seconds=15

# Tài khoản test
admin.username=admin@company.com
admin.password=your_password
staff.username=staff@company.com
staff.password=your_password

# Token tĩnh (tùy chọn — nếu có thể dùng thay vì gọi login)
auth.token=
```

### 3.3. Lệnh chạy

```bash
# Chạy toàn bộ suite (theo testng.xml)
mvn clean test

# Chạy theo chế độ
mvn clean test -Dexecution.type=API
mvn clean test -Dexecution.type=UI
mvn clean test -Dexecution.type=UI -Dheadless=true

# Chạy song song với N thread
mvn clean test -Dparallel=methods -DthreadCount=4

# Chạy một test class cụ thể
mvn clean test -Dtest=WarehouseTest

# Xóa kết quả cũ trước khi chạy
mvn clean
```

### 3.4. Xem kết quả

| Loại báo cáo | Đường dẫn |
|---|---|
| **ExtentReport HTML** (khuyên dùng) | `target/extent-reports/ExtentReport.html` |
| **TestNG HTML Report** | `target/surefire-reports/index.html` |
| **Emailable Report** | `target/surefire-reports/emailable-report.html` |
| **Plain Text Log** | `target/surefire-reports/<TestClassName>.txt` |

---

## 4. Cách Thêm Tính Năng Mới (Plugin New Feature)

> Quy tắc vàng: **Không sửa code lõi** (`driver/`, `config/`, `listeners/`, `reporters/`).
> Chỉ thêm file vào `models/`, `api/clients/`, `pages/`, `strategy/`, `tests/`.

### Checklist triển khai tính năng mới

```
[ ] Bước 1: Thêm endpoint vào ApiEndpoints.java (nếu chưa có)
[ ] Bước 2: Tạo Model (nếu có request body)
[ ] Bước 3: Tạo API Client
[ ] Bước 4: Tạo Page Object (nếu cần test UI)
[ ] Bước 5: Viết Test Class
[ ] Bước 6: Đăng ký vào testng.xml
```

---

### Bước 1 — Thêm endpoint (nếu chưa có trong `ApiEndpoints.java`)

```java
// Ví dụ: thêm endpoint cho Module Bán hàng
public static final String SALES_ORDERS         = "/sales-orders";
public static final String SALES_ORDER_BY_ID    = "/sales-orders/%s";
public static final String SALES_ORDER_CONFIRM  = "/sales-orders/%s/confirm";
public static final String SALES_ORDER_CANCEL   = "/sales-orders/%s/cancel";
```

---

### Bước 2 — Tạo Model Request (nếu có request body)

Đặt tại: `src/main/java/com/automation_erp/framework/models/`

```java
package com.automation_erp.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderRequest {

    @JsonProperty("customer_id")
    private Integer customerId;

    @JsonProperty("warehouse_code")
    private String warehouseCode;

    private List<ItemDetail> items;   // Dùng lại ItemDetail có sẵn

    private String notes;
}
```

**Cách dùng trong test:**
```java
SalesOrderRequest req = SalesOrderRequest.builder()
    .customerId(101)
    .warehouseCode("WH-MAIN")
    .items(List.of(
        ItemDetail.builder().sku("SKU-IPHONE15").quantity(2).build()
    ))
    .notes("Test đơn hàng tự động")
    .build();
```

---

### Bước 3 — Tạo API Client

Đặt tại: `src/main/java/com/automation_erp/framework/api/clients/`

```java
package com.automation_erp.framework.api.clients;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.constants.ApiEndpoints;
import com.automation_erp.framework.models.SalesOrderRequest;
import io.restassured.response.Response;
import java.util.Map;

public class SalesOrderClient {

    private SalesOrderClient() {}

    public static Response createOrder(String token, SalesOrderRequest payload) {
        return ApiClient.post(ApiEndpoints.SALES_ORDERS, token, payload);
    }

    public static Response getOrderById(String token, String id) {
        return ApiClient.get(ApiEndpoints.path(ApiEndpoints.SALES_ORDER_BY_ID, id), token, null);
    }

    public static Response getOrders(String token, Map<String, Object> queryParams) {
        return ApiClient.get(ApiEndpoints.SALES_ORDERS, token, queryParams);
    }

    public static Response confirmOrder(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.SALES_ORDER_CONFIRM, id), token, null);
    }

    public static Response cancelOrder(String token, String id) {
        return ApiClient.post(ApiEndpoints.path(ApiEndpoints.SALES_ORDER_CANCEL, id), token, null);
    }
}
```

---

### Bước 4 — Tạo Page Object (nếu có test UI)

Đặt tại: `src/main/java/com/automation_erp/framework/pages/`

```java
package com.automation_erp.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SalesOrderPage extends BasePage {   // Kế thừa BasePage

    // Locators — đặt private final, tên rõ ràng
    private final By createOrderBtn    = By.id("btn-create-order");
    private final By customerInput     = By.id("input-customer");
    private final By warehouseSelect   = By.id("select-warehouse");
    private final By addItemBtn        = By.id("btn-add-item");
    private final By skuInput          = By.cssSelector(".input-sku");
    private final By qtyInput          = By.cssSelector(".input-qty");
    private final By saveBtn           = By.id("btn-save-order");
    private final By confirmBtn        = By.id("btn-confirm-order");
    private final By statusBadge       = By.id("order-status-badge");

    public SalesOrderPage(WebDriver driver) {
        super(driver);   // BasePage tự tạo WebDriverWait
    }

    public void clickCreateOrder() {
        click(createOrderBtn);
    }

    public void selectWarehouse(String code) {
        // Dùng hàm từ BasePage — đã có sẵn
        selectByVisibleText(warehouseSelect, code);
    }

    public void addItem(String sku, int qty) {
        click(addItemBtn);
        writeText(skuInput, sku);
        writeText(qtyInput, String.valueOf(qty));
    }

    public void saveOrder() {
        click(saveBtn);
    }

    public void confirmOrder() {
        click(confirmBtn);
    }

    public String getOrderStatus() {
        return readText(statusBadge);
    }
}
```

---

### Bước 5 — Viết Test Class

Đặt tại: `src/test/java/com/automation_erp/tests/m3/` (module mới thì tạo folder mới)

```java
package com.automation_erp.tests.m3;

import com.automation_erp.framework.api.ApiClient;
import com.automation_erp.framework.api.clients.SalesOrderClient;
import com.automation_erp.framework.config.ConfigReader;
import com.automation_erp.framework.constants.DocumentStatus;
import com.automation_erp.framework.constants.HttpStatus;
import com.automation_erp.framework.models.ItemDetail;
import com.automation_erp.framework.models.SalesOrderRequest;
import com.automation_erp.framework.utils.AssertionUtils;
import com.automation_erp.framework.utils.DataGenerator;
import com.automation_erp.tests.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

public class SalesOrderTest extends BaseTest {

    private String adminToken;
    private String staffToken;

    @BeforeClass
    public void setupTokens() {
        adminToken = ApiClient.login(ConfigReader.getProperty("admin.username"),
                                     ConfigReader.getProperty("admin.password"));
        staffToken = ApiClient.login(ConfigReader.getProperty("staff.username"),
                                     ConfigReader.getProperty("staff.password"));
    }

    @Test(description = "TC-SALES-01: Tạo đơn hàng mới ở trạng thái Nháp")
    public void testCreateSalesOrderSuccess() {
        // Dùng DataGenerator sinh SKU test ngẫu nhiên
        String sku = DataGenerator.generateSkuCode("MACBOOK");

        SalesOrderRequest req = SalesOrderRequest.builder()
            .customerId(101)
            .warehouseCode("WH-MAIN")
            .items(List.of(ItemDetail.builder().sku(sku).quantity(1).build()))
            .notes("Test TC-SALES-01 tự động")
            .build();

        Response response = SalesOrderClient.createOrder(staffToken, req);

        // Dùng AssertionUtils thay vì Assert.assertEquals trực tiếp
        AssertionUtils.assertStatusCode(response, HttpStatus.CREATED);
        AssertionUtils.assertFieldNotNull(response, "data.id");
        AssertionUtils.assertStringFieldEquals(response, "data.status", DocumentStatus.DRAFT);
    }

    @Test(description = "TC-SALES-02: Tạo và xác nhận đơn hàng thành công")
    public void testCreateAndConfirmOrderFlow() {
        String sku = DataGenerator.generateSkuCode("IPHONE");
        String idempotencyKey = UUID.randomUUID().toString();

        SalesOrderRequest req = SalesOrderRequest.builder()
            .customerId(101)
            .warehouseCode("WH-MAIN")
            .items(List.of(ItemDetail.builder().sku(sku).quantity(5).build()))
            .build();

        // Step 1: Tạo đơn hàng
        Response createRes = SalesOrderClient.createOrder(staffToken, req);
        AssertionUtils.assertStatusCode(createRes, HttpStatus.CREATED);
        String orderId = createRes.jsonPath().getString("data.id");

        // Step 2: Xác nhận đơn hàng
        Response confirmRes = SalesOrderClient.confirmOrder(adminToken, orderId);
        AssertionUtils.assertStatusCode(confirmRes, HttpStatus.OK);
        AssertionUtils.assertStringFieldEquals(confirmRes, "data.status", DocumentStatus.COMPLETED);
    }

    @Test(description = "TC-SALES-03 (Negative): Nhân viên không được xác nhận đơn hàng → 403")
    public void testStaffCannotConfirmOrder() {
        SalesOrderRequest req = SalesOrderRequest.builder()
            .customerId(101)
            .warehouseCode("WH-MAIN")
            .items(List.of(ItemDetail.builder().sku("SKU-TEST").quantity(1).build()))
            .build();

        Response createRes = SalesOrderClient.createOrder(staffToken, req);
        String orderId = createRes.jsonPath().getString("data.id");

        // Staff gọi confirm → expect 403 Forbidden
        Response confirmRes = SalesOrderClient.confirmOrder(staffToken, orderId);
        AssertionUtils.assertStatusCode(confirmRes, HttpStatus.FORBIDDEN);
    }
}
```

---

### Bước 6 — Đăng ký vào `testng.xml`

```xml
<suite name="Warehouse Automation Test Suite" parallel="methods" thread-count="4">

    <listeners>
        <listener class-name="com.automation_erp.framework.listeners.TestListener"/>
    </listeners>

    <test name="Module 2 - Warehouse">
        <classes>
            <class name="com.automation_erp.tests.m1.WarehouseTest"/>
            <class name="com.automation_erp.tests.m2.InboundWorkflowTest"/>
        </classes>
    </test>

    <!-- Thêm module mới vào đây -->
    <test name="Module 3 - Sales">
        <classes>
            <class name="com.automation_erp.tests.m3.SalesOrderTest"/>
        </classes>
    </test>

</suite>
```

---

## 5. Cách Dùng Các Utility Classes

### 5.1. `DataGenerator` — Sinh dữ liệu test ngẫu nhiên

```java
// Sinh mã kho test duy nhất (tránh xung đột khi chạy song song)
String warehouseCode = DataGenerator.generateWarehouseCode();
// → "WH-TEST-20240701143022"

String branchCode = DataGenerator.generateWarehouseCode("BRANCH");
// → "WH-BRANCH-20240701143022"

// Sinh SKU test
String sku = DataGenerator.generateSkuCode("IPHONE15");
// → "SKU-IPHONE15-20240701143022"

// UUID cho Idempotency-Key
String idempotencyKey = DataGenerator.generateUUID();

// Số lượng ngẫu nhiên [10, 100]
int qty = DataGenerator.randomQuantity(10, 100);
```

### 5.2. `AssertionUtils` — Kiểm tra kết quả API

```java
// Kiểm tra HTTP status code (có log body nếu fail)
AssertionUtils.assertStatusCode(response, HttpStatus.CREATED);

// Kiểm tra field không null
AssertionUtils.assertFieldNotNull(response, "data.id");

// Kiểm tra giá trị field
AssertionUtils.assertStringFieldEquals(response, "data.status", DocumentStatus.DRAFT);
AssertionUtils.assertFieldEquals(response, "data.quantity", 50);

// Kiểm tra body chứa chuỗi nhất định
AssertionUtils.assertBodyContains(response, "Không đủ tồn kho");
```

### 5.3. `DateUtils` — Thời gian cho filter params

```java
// Lấy khoảng thời gian 30 ngày gần nhất (cho filter báo cáo)
Map<String, Object> params = new HashMap<>();
params.put("from_date", DateUtils.startDateOfLastNDays(30));  // "2024-06-01"
params.put("to_date",   DateUtils.endDateToday());            // "2024-07-01"

Response reportRes = InventoryClient.getInventoryXntReport(adminToken, params);
```

### 5.4. `JsonUtils` — Data-driven testing từ file JSON

```java
// Đọc test data từ file JSON (data-driven testing)
// File: src/test/resources/data/inbound_data.json
InboundRequest req = JsonUtils.readFromFile(
    "src/test/resources/data/inbound_data.json",
    InboundRequest.class
);

// Serialize object thành JSON string để log
System.out.println(JsonUtils.toJsonString(req));
```

### 5.5. Fixtures — Test Data Isolation

```java
public class SomeTest extends BaseTest {

    private WarehouseFixture warehouseFixture;
    private String testWarehouseCode;

    @BeforeMethod
    public void setupTestData() {
        // Mỗi test method có kho riêng, không xung đột nhau
        warehouseFixture = new WarehouseFixture(adminToken);
        int warehouseId   = warehouseFixture.createAndGetId();
        testWarehouseCode = warehouseFixture.getCode();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanupTestData() {
        // Luôn cleanup dù test pass hay fail
        warehouseFixture.teardown();
    }

    @Test
    public void testSomething() {
        // Dùng testWarehouseCode trong test — được đảm bảo là kho sạch
    }
}
```

---

## 6. Cách Dùng `ApiEndpoints` — Không Hardcode URL

```java
// ❌ Sai — hardcode URL rải rác trong code
ApiClient.post("/inbound-documents/" + id + "/post-receipt", token, null);

// ✅ Đúng — dùng constant + helper path()
ApiClient.post(ApiEndpoints.path(ApiEndpoints.INBOUND_POST_RECEIPT, id), token, null);

// Ví dụ khác
String url = ApiEndpoints.path(ApiEndpoints.WAREHOUSE_BY_ID, 123);
// → "/warehouses/123"

String url2 = ApiEndpoints.path(ApiEndpoints.TRANSFER_DISPATCH, "abc-xyz");
// → "/transfer-orders/abc-xyz/dispatch"
```

---

## 7. Cách Viết Assertions cho cả UI và API

### API — Kiểm tra response

```java
// Trong test method (API mode)
Response createRes = InboundClient.createInbound(staffToken, payload);

// Dùng AssertionUtils (khuyên dùng)
AssertionUtils.assertStatusCode(createRes, HttpStatus.CREATED);
AssertionUtils.assertStringFieldEquals(createRes, "data.status", DocumentStatus.DRAFT);

// Hoặc dùng RestAssured jsonPath() trực tiếp cho case phức tạp
String documentNo = createRes.jsonPath().getString("data.document_no");
Assert.assertNotNull(documentNo, "Số phiếu phải được sinh tự động");
```

### UI — Kiểm tra hiển thị trên trình duyệt

```java
// Trong Page class (UI mode)
public String getOrderStatus() {
    return readText(statusBadge);  // BasePage.readText()
}

// Trong test method
String status = salesOrderPage.getOrderStatus();
Assert.assertEquals(status, DocumentStatus.COMPLETED, "Trạng thái hiển thị trên UI không đúng!");
```

---

## 8. Cách Dùng `ExtentReportManager` để Log Bước Test

```java
import com.automation_erp.framework.reporters.ExtentReportManager;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

@Test
public void testCreateWarehouse() {
    ExtentTest test = ExtentReportManager.getTest();  // Lấy test của thread hiện tại

    test.log(Status.INFO, "Bước 1: Chuẩn bị request tạo kho");
    WarehouseRequest req = WarehouseRequest.builder()...build();

    test.log(Status.INFO, "Bước 2: Gửi POST /warehouses");
    Response res = WarehouseClient.createWarehouse(adminToken, req);

    test.log(Status.INFO, "Bước 3: Kiểm tra response HTTP 201");
    AssertionUtils.assertStatusCode(res, HttpStatus.CREATED);

    test.pass("✅ Tạo kho thành công! ID: " + res.jsonPath().get("data.id"));
}
```

---

## 9. Hướng Dẫn Chạy và Xem Log

### 9.1. Báo cáo HTML trực quan (ExtentReports — Khuyên dùng)

Sau khi chạy test, mở file `target/extent-reports/ExtentReport.html` bằng trình duyệt:
- Dashboard tổng hợp Pass/Fail/Skip theo màu
- Chi tiết từng bước test case có thể expand/collapse
- Thống kê thời gian chạy từng test

### 9.2. Báo cáo TestNG

- `target/surefire-reports/index.html` — Suite report đầy đủ
- `target/surefire-reports/emailable-report.html` — Bảng tóm tắt, tiện gửi email

### 9.3. Dọn dẹp trước khi chạy mới

```bash
mvn clean   # Xóa toàn bộ target/ để báo cáo không bị lẫn lộn với lần chạy trước
```

---

## 10. Tóm Tắt — Khi Có Tính Năng Mới

```
Tính năng mới
    │
    ├─ Có endpoint mới?     → Thêm vào ApiEndpoints.java
    │
    ├─ Có request body?     → Tạo XxxRequest.java trong models/ (@Builder @Data)
    │
    ├─ Cần gọi API?         → Tạo XxxClient.java trong api/clients/
    │                           (extends ApiClient methods, dùng ApiEndpoints constants)
    │
    ├─ Cần test UI?         → Tạo XxxPage.java trong pages/ (extends BasePage)
    │                           (chỉ chứa locators và wrapper methods)
    │
    └─ Viết test?           → Tạo XxxTest.java trong tests/mX/
                                (extends BaseTest, dùng Clients + Models + AssertionUtils)
                                Đăng ký vào testng.xml
```

> **Không cần sửa** `config/`, `driver/`, `listeners/`, `reporters/`, `strategy/WorkFlowStrategy.java`.
> Core framework đóng lại với phần mở rộng, mở ra với tính năng mới.