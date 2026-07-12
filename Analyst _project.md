# Báo cáo Phân tích Kiến trúc Framework Kiểm thử: `automation_erp`

**Repo:** https://github.com/Hong-203/automation_erp

**Stack công nghệ:** Java 17 · Maven · TestNG 7.9 · RestAssured 5.4 · Selenium 4.18 · WebDriverManager 5.7 · ExtentReports 5.1 · Allure 2.26 · AssertJ 3.25 · Lombok · Log4j2

---

## 1. Nhận định Tổng quan (Executive Summary)

Dự án `automation_erp` sở hữu một framework kiểm thử tự động hóa có mức độ trưởng thành (maturity level) rất cao và kiến trúc chặt chẽ. Framework được thiết kế tối ưu cho các luồng nghiệp vụ ERP phức tạp (với nhiều trạng thái chứng từ như draft, pending, approved, received...) bằng cách kết hợp hài hòa giữa kiểm thử giao diện (UI) và kiểm thử API thông qua các Design Pattern kinh điển. 

Hạ tầng framework đảm bảo an toàn đa luồng (Thread-safe) tuyệt đối, kiểm soát lỗi chập chờn (flaky tests) tốt, có khả năng mở rộng dữ liệu ngoài (Data-driven) và hỗ trợ báo cáo đa kênh sinh động.

---

## 2. Đánh giá Chuyên sâu: Cấu trúc Dự án (Project Structure)

### 2.1. Cây thư mục và Phân bổ Vai trò các Layer

```
automation_erp/
├── src/
│   ├── main/java/.../framework/
│   │   ├── api/                # API Client, AuthManager (Quản lý token caching)
│   │   ├── config/             # ConfigReader (Nạp cấu hình môi trường qua ClassLoader)
│   │   ├── constants/          # Hằng số endpoints, HttpStatus, DocumentStatus
│   │   ├── driver/             # DriverFactory (Local & Remote Grid/BrowserStack), DriverManager
│   │   ├── listeners/          # TestListener (Screenshot), RetryAnalyzer, AnnotationTransformer
│   │   ├── models/             # Request POJOs (sử dụng Lombok @Builder)
│   │   ├── pages/              # Page Object Model (POM) cho UI testing
│   │   ├── strategy/           # Inbound/Outbound/Transfer Strategy & StrategyFactory
│   │   └── utils/              # CsvUtils, JsonReader, AssertionUtils, DataGenerator
│   └── test/
│       ├── java/.../tests/
│       │   ├── demo/           # DataDrivenTest.java (Hướng dẫn nạp CSV/JSON + AssertJ)
│       │   ├── fixtures/       # WarehouseFixture, ProductFixture (Khởi tạo/Dọn dẹp dữ liệu test)
│       │   ├── warehouse/      # WarehouseTest.java (Kiểm thử module kho)
│       │   └── inbound/        # InboundWorkflowTest.java (Kiểm thử luồng Nhập kho)
│       └── resources/
│           ├── testdata/       # users.csv, warehouses.json (Thư mục dữ liệu tĩnh)
│           ├── config.properties.example
│           ├── config-staging.properties.example
│           ├── config-prod.properties.example
│           └── testng.xml
└── pom.xml
```

### 2.2. Phân tích chi tiết thiết kế cấu trúc

1. **Phân tách rạch ròi Framework Layer & Test Layer:**
   - **Framework Layer (`src/main/`):** Đóng vai trò là một SDK kiểm thử nội bộ. Nó hoàn toàn không chứa bất kỳ kịch bản test cụ thể nào mà chỉ tập trung vào việc cung cấp các công cụ kỹ thuật (driver, HTTP client, file reader, listener, report engine). Điều này cho phép đội ngũ QA phát triển phần lõi độc lập với kịch bản kiểm thử nghiệp vụ.
   - **Test Layer (`src/test/`):** Chỉ tập trung vào việc mô tả hành vi nghiệp vụ của hệ thống ERP. Test layer giao tiếp với hệ thống thông qua các API Client hoặc Page Objects được cung cấp bởi Framework Layer. Thiết kế này giúp hạn chế tối đa việc viết mã nguồn trùng lặp và tăng tính thẩm mỹ của kịch bản kiểm thử.

2. **Thiết kế Package theo Domain Nghiệp vụ (Domain-Driven Package Layout):**
   - Thay vì chia nhỏ thư mục test theo cấu trúc kỹ thuật (`tests.api`, `tests.ui`), dự án tổ chức theo domain chức năng của ERP (`tests.warehouse`, `tests.inbound`).
   - *Ưu điểm:* Mỗi module ERP lớn thường đi kèm một vòng đời dữ liệu (State Machine) rất phức tạp. Tổ chức theo domain giúp QA quản lý các lớp hỗ trợ dữ liệu liên quan (`WarehouseFixture`, `ProductFixture`) ngay sát cạnh kịch bản test, giảm thiểu sự phân mảnh và giúp BA (Business Analyst) hoặc Manual QA dễ dàng đọc hiểu kịch bản automation.

3. **Cơ chế quản lý Test Data Isolation thông qua Fixtures:**
   - Việc sinh mã ngẫu nhiên qua UUID (`NK-` + random) kết hợp với các fixture setup/teardown (`WarehouseFixture.teardown()`) đảm bảo dữ liệu rác luôn được dọn dẹp sạch sẽ sau mỗi test suite. Điều này tránh tình trạng dữ liệu kiểm thử bị nhiễm chéo (data pollution) trên môi trường Staging/UAT dùng chung.

---

## 3. Đánh giá Chuyên sâu: Thiết kế Framework (Core Infrastructure)

### 3.1. Phân tách Chiến dịch thực thi (Strategy Pattern & ISP)

Một trong những thách thức lớn nhất khi làm Automation Test cho ERP là kiểm chứng nghiệp vụ ở cả 2 cổng: API (để chạy nhanh trong CI/CD pipeline) và UI (để kiểm tra giao diện người dùng thực tế). Framework giải quyết bài toán này một cách xuất sắc:

```
                  [ InboundStrategy ]   [ OutboundStrategy ]   [ TransferStrategy ]
                           \                     |                     /
                            \                    |                    /
                             └───► [ WorkFlowStrategy (Marker) ] ◄───┘
                                                 ▲
                                                 │
                                ┌────────────────┴────────────────┐
                                │                                 │
                     [ ApiWorkFlowStrategy ]           [ UiWorkFlowStrategy ]
                     (Thực thi qua RestAssured)          (Thực thi qua Selenium)
```

- **Nguyên lý Interface Segregation (ISP):** Việc phân rã interface dùng chung thành 3 interface domain chuyên biệt (`InboundStrategy`, `OutboundStrategy`, `TransferStrategy`) đảm bảo các lớp triển khai không bị ép buộc phải phụ thuộc vào các phương thức mà chúng không sử dụng.
- **Tối ưu hóa khả năng phân tích lỗi (Fail-safe):** Lớp `UiWorkFlowStrategy` ném ra `UnsupportedOperationException` cho các method UI chưa được viết Page Object. Điều này loại bỏ hoàn toàn rủi ro **False Positive** (khiến hệ thống báo Pass một cách sai lệch do trả về dữ liệu giả lập).

### 3.2. Hạ tầng kỹ thuật Đa luồng (Thread-Safety & Concurrency)

Khi chạy hàng trăm kịch bản song song trên CI/CD, tính an toàn đa luồng là yếu tố sống còn của một framework:
1. **ThreadLocal WebDriver Management:** 
   `DriverManager` bọc `WebDriver` bằng `ThreadLocal<WebDriver>`. Khi chạy parallel, JVM cô lập hoàn toàn phiên làm việc của từng trình duyệt trên mỗi thread riêng. Không bao giờ xảy ra hiện tượng thread này điều khiển hoặc ghi đè dữ liệu lên cửa sổ trình duyệt của thread khác.
2. **Khóa mịn (Fine-grained Lock) trong AuthManager:**
   Cơ chế `ConcurrentHashMap.compute(...)` giúp tối ưu hóa việc quản lý Token JWT. Khi 4 luồng chạy song song cần lấy token:
   - Nếu chúng dùng chung một tài khoản `admin@erb.vn`, luồng đầu tiên sẽ gọi API login thật, 3 luồng sau sẽ chờ đợi và lấy ngay token từ cache mà không gọi API trùng lặp.
   - Nếu chúng dùng các tài khoản khác nhau, việc lấy token sẽ diễn ra hoàn toàn song song mà không bị chặn (block) như khi dùng từ khóa `synchronized` cấp Class.

### 3.3. Config Loading & CSV Regex Parsing

- **Classpath Resource Loader:** Việc đọc properties/CSV/JSON qua ClassLoader giúp đóng gói dự án thành một tệp JAR thực thi duy nhất cực kỳ dễ dàng. Khi chạy trên Jenkins hay GitHub Actions, runner không cần quan tâm đến thư mục gốc của dự án ở đâu mà vẫn tìm được file cấu hình chuẩn xác.
- **CSV Parser chuẩn RFC 4180:** Lớp `CsvUtils` giải quyết bài toán dấu phẩy nằm trong giá trị cột (ví dụ: `"Sản phẩm A, màu xanh", 50000`) bằng Regex lookahead nâng cao, loại bỏ sự phụ thuộc vào các thư viện bên thứ ba nặng nề mà vẫn đảm bảo độ chính xác của bộ phân tích dữ liệu kiểm thử.

---

## 4. Phân tích Khả năng Bảo trì & Mở rộng (Maintainability & Scalability)

### 4.1. Khả năng bảo trì (Maintainability) - Điểm số: 9.5/10

Kiến trúc framework được thiết kế để cô lập tối đa tác động của sự thay đổi (Impact Isolation):

*   **Trường hợp 1: Backend thay đổi Endpoint hoặc cấu trúc JSON Response**
    *   *Tác động:* Bạn chỉ cần cập nhật lại các hằng số trong [ApiEndpoints](file:///d:/1-Hong-Tester/automation_erp/src/main/java/com/automation_erp/framework/constants/ApiEndpoints.java) hoặc thay đổi cấu trúc của POJO model tương ứng trong package `models/` (được Lombok hỗ trợ sinh getter/setter/builder tự động). Kịch bản test nghiệp vụ không cần thay đổi một dòng code nào.
*   **Trường hợp 2: Frontend thay đổi thiết kế giao diện (Locator/UI element)**
    *   *Tác động:* Toàn bộ các định vị phần tử (Locators) và thao tác Selenium đều được bọc trong các lớp Page Object (`LoginPage`, `InboundPage`). Bạn chỉ cần cập nhật locator tại đúng file Page Object đó. Lớp `UiWorkFlowStrategy` và kịch bản test nghiệp vụ hoàn toàn được bảo vệ an toàn.
*   **Trường hợp 3: Luồng nghiệp vụ (Business Workflow) thay đổi**
    *   *Tác động:* Bạn chỉ cần điều chỉnh logic ghép nối các bước trong `ApiWorkFlowStrategy` hoặc `UiWorkFlowStrategy`. Mã nguồn của kịch bản test (Test Cases) vẫn được giữ nguyên tính khai báo (declarative style) rất trực quan.
*   **Không còn hard-code ID nhạy cảm:** Bằng việc hỗ trợ giá trị mặc định fallback (`1`, `1`, `3`) trực tiếp trong `ConfigReader`, dự án loại bỏ hoàn toàn nguy cơ bị crash khi chuyển giao mã nguồn giữa các máy cá nhân hoặc môi trường CI/CD khác nhau khi thiếu file properties.

### 4.2. Khả năng mở rộng (Scalability) - Điểm số: 9.5/10

Framework sở hữu các yếu tố thiết kế sẵn sàng đáp ứng khi quy mô dự án mở rộng lên hàng nghìn test case chạy hàng ngày:

1. **Quy mô hạ tầng (Infrastructure Scale):**
   `DriverFactory` hỗ trợ song song cả Local và Remote WebDriver (kết nối trực tiếp tới Selenium Grid nội bộ hoặc các nền tảng đám mây như BrowserStack). Khi số lượng UI test tăng lên, bạn chỉ cần thay đổi cờ `remote.web.driver=true` trong properties và tăng số lượng Node trên Grid. Code framework tự động scale-out mà không cần bất kỳ sự sửa đổi nào.
2. **Quy mô dữ liệu (Data-driven Scale):**
   Nhờ tích hợp sẵn bộ đôi đọc dữ liệu CSV (`CsvUtils`) và JSON (`JsonReader`), QA có thể mở rộng diện bao phủ (test coverage) bằng cách viết thêm hàng nghìn dòng dữ liệu kiểm thử (dùng chung 1 test logic kết hợp với TestNG `@DataProvider`) giúp tiết kiệm tối đa thời gian viết code kiểm thử mới.
3. **Mở rộng môi trường kiểm thử (Multi-environment Scale):**
   Hỗ trợ nạp cấu hình động thông qua cờ JVM `-Denv` (staging/prod/dev) giúp bộ kiểm thử tự động thích ứng ngay lập tức với các môi trường triển khai khác nhau của đội ngũ phát triển sản phẩm.

---

## 5. Đề xuất cải tiến nâng cao trong tương lai

Mặc dù framework hiện tại đã rất hoàn chỉnh và ổn định, để chuẩn bị cho các hệ thống lớn hàng vạn test case, có thể cân nhắc các cải tiến nâng cao sau:

1. **Tích hợp Database Assertion:**
   - *Giải pháp:* Viết thêm lớp tiện ích JDBC/Hibernate helper để kết nối trực tiếp vào Database của ERP. Sau khi API/UI báo hoàn thành nhập kho (`received`), tiến hành truy vấn DB để đối chiếu trực tiếp dữ liệu tồn kho thực tế ở bảng `stock_movements` thay vì chỉ kiểm tra JSON Response của API.
2. **Áp dụng Thư viện Sinh Dữ liệu Động (DataFaker):**
   - *Giải pháp:* Thay thế các hàm tự sinh chuỗi UUID trong `DataGenerator` bằng thư viện `net.datafaker:datafaker` để sinh tên kho, tên sản phẩm, địa chỉ thật và sinh động hơn.
3. **generic BaseApiClient:**
   - *Giải pháp:* Viết một lớp abstract `BaseApiClient<T>` định nghĩa sẵn các phương thức CRUD cơ bản (GET list, GET by ID, POST create, DELETE, PATCH update) để giảm thiểu mã nguồn trùng lặp giữa các client cụ thể.
4. **Allure Step Annotations:**
   - *Giải pháp:* Tận dụng thuộc tính `@Step` của Allure trên các method nghiệp vụ của Page Object và Client Class để báo cáo hiển thị từng bước nghiệp vụ trực quan hơn nữa.
