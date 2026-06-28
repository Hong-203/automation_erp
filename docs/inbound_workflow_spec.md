# Tài liệu Đặc tả luồng API Nhập kho (Inbound Workflow)

Tài liệu này mô tả chi tiết logic, cấu trúc dữ liệu, và các luồng kiểm thử tự động (Automation Test) đối với module Nhập kho (Inbound). Tài liệu có thể được dùng làm căn cứ cốt lõi để AI tự động bảo trì và sinh lại (re-generate) mã nguồn kiểm thử trong tương lai. Tuyệt đối không được bỏ sót bất kỳ quy tắc nào trong tài liệu này khi thực hiện bảo trì code.

## 1. Thông tin hệ thống
- **Đối tượng cốt lõi:** Inbound Document (Phiếu nhập kho) & Inbound Lines (Chi tiết dòng sản phẩm).
- **Trạng thái (State Machine):**
  1. `draft` (Nháp): Trạng thái khởi tạo. Có thể cập nhật (`PATCH`), gửi duyệt (`submit`), hủy (`cancel`).
  2. `pending` (Chờ duyệt): Chuyển từ `draft`. Có thể phê duyệt (`approve`) hoặc từ chối (`reject`).
  3. `approved` (Đã duyệt): Chuyển từ `pending`. Có thể thực hiện nhập kho thực tế (`post-receipt`).
  4. `completed` (Hoàn thành): Chuyển từ `approved` sau khi `post-receipt` thành công.
  5. `rejected` (Từ chối): Chuyển từ `pending`. Trạng thái cuối.
  6. `cancelled` (Đã hủy): Chuyển từ `draft`. Trạng thái cuối.
- **Xác thực:** Yêu cầu Admin Token (`adminToken`) đối với toàn bộ các bước.

## 2. Cấu trúc dữ liệu (Data Models)

Để Mapping dữ liệu thành JSON chính xác, hệ thống sử dụng 2 Data Model chính:

### 2.1. `InboundRequest` (Phiếu Nhập)
Đại diện cho Object ngoài cùng gửi đi. Các trường bắt buộc phải có annotation `@JsonInclude(JsonInclude.Include.NON_NULL)` để không gửi đi trường rỗng.
- `doc_no` (String): Mã phiếu nhập.
- `doc_date` (String): Ngày chứng từ, định dạng `yyyy-MM-dd`.
- `dst_warehouse_id` (Integer): ID kho đích.
- `supplier_id` (Integer): ID nhà cung cấp.
- `note` (String): Ghi chú.
- `lines` (List<ItemDetail>): Danh sách dòng sản phẩm.

### 2.2. `ItemDetail` (Dòng sản phẩm)
Đại diện cho các element bên trong mảng `lines`.
- `id` (Integer): Bắt buộc khi truyền vào API Cập nhật phiếu Nháp.
- `line_id` (Integer): Bắt buộc khi truyền vào API `post-receipt`. Chú ý `@JsonProperty("line_id")`.
- `product_id` (Integer): ID sản phẩm.
- `qty_planned` (Integer): Số lượng dự kiến. Chú ý `@JsonProperty("qty_planned")`.
- `qty_actual` (Integer): Số lượng thực nhập (Dùng riêng cho `post-receipt`). Chú ý `@JsonProperty("qty_actual")`.
- `unit_cost` (Double): Đơn giá nhập.
- `completion_type` (String): Đánh dấu trạng thái nhập (Dùng riêng cho `post-receipt`). Ví dụ: `"full"`.

## 3. Chi tiết các luồng kiểm thử (Test Cases)
Toàn bộ mã nguồn nằm ở lớp `com.automation_erp.tests.m2.InboundWorkflowTest`. Sử dụng `DateUtils.todayAsIso()` cho tất cả các trường `doc_date`.

### Luồng 1: Happy Path - Luồng Nhập kho thành công
Bao gồm 5 Test Cases liên tiếp (`dependsOnMethods`). Cần sử dụng các biến toàn cục (Global Variables) để truyền dữ liệu giữa các Test:
- `happyPathInboundId`: Lưu ID phiếu nhập.
- `happyPathLineId`: Lưu ID dòng sản phẩm (thay đổi sau khi Update).
- `happyPathQtyPlanned`: Lưu số lượng dự kiến để dùng lúc `post-receipt`.

#### Bước 1: Tạo phiếu Nháp (`TC-INBOUND-01`)
- **API:** `POST /api/v1/inbound-documents`
- **Logic code:**
  - Gán `happyPathQtyPlanned = 100`.
  - Tạo `InboundRequest`: `docNo = "NK-" + System.currentTimeMillis()`, `docDate = DateUtils.todayAsIso()`, `dstWarehouseId = 1`, `supplierId = 1`.
  - Thêm 1 dòng `ItemDetail`: `productId = 3`, `qtyPlanned = happyPathQtyPlanned`, `unitCost = 50000.0`.
- **Assertion:** 
  - Status Code = `201 CREATED`.
  - Kiểm tra `data.id` Not Null.
  - Kiểm tra `data.status` == `"draft"`.
- **Dữ liệu xuất (Extract):** Gán `happyPathInboundId` và `happyPathLineId` từ JSON Response.

#### Bước 2: Cập nhật phiếu Nháp (`TC-INBOUND-02`)
- **API:** `PATCH /api/v1/inbound-documents/{id}`
- **Logic code:**
  - Giữ nguyên `happyPathQtyPlanned = 100` (Không tăng số lượng để tránh lỗi logic nghiệp vụ over-receive).
  - Tạo `InboundRequest` mới: `note = "stringgg updated"`.
  - Dòng `ItemDetail`: Truyền vào `id = happyPathLineId`, `productId = 3`, `qtyPlanned = happyPathQtyPlanned`, `unitCost = 50000.0`.
- **QUAN TRỌNG (Backend Quirk):** Hệ thống Backend sử dụng cơ chế **Full Replace** cho mảng `lines`. Nó xóa toàn bộ dòng hiện tại và `INSERT` lại dòng mới. Do đó, **`line_id` cũ bị xóa và thay bằng `line_id` mới**.
- **Assertion:** 
  - Status Code = `200 OK`.
  - `data.status` == `"draft"`.
- **Dữ liệu xuất (Extract):** Gán lại `happyPathLineId = res.jsonPath().getInt("data.lines[0].id")` để dùng cho Bước 5. Tuyệt đối không được quên!

#### Bước 3: Gửi duyệt phiếu (`TC-INBOUND-03`)
- **API:** `POST /api/v1/inbound-documents/{id}/submit`
- **Assertion:** Status Code = `200 OK`. `data.status` == `"pending"`.

#### Bước 4: Phê duyệt phiếu (`TC-INBOUND-04`)
- **API:** `POST /api/v1/inbound-documents/{id}/approve`
- **Assertion:** Status Code = `200 OK`. `data.status` == `"approved"`.

#### Bước 5: Nhập kho thực tế (`TC-INBOUND-05`)
- **API:** `POST /api/v1/inbound-documents/{id}/post-receipt`
- **Headers:** Yêu cầu biến `Idempotency-Key = UUID.randomUUID().toString()`.
- **Logic code:** Thay vì dùng Class model, phải dùng cấu trúc `Map.of()` gốc để ép chuẩn định dạng JSON, tránh việc thư viện Jackson tự động render các thuộc tính thừa gây lỗi.
  ```java
  Object payload = Map.of(
      "lines", List.of(
          Map.of(
              "line_id", happyPathLineId, // Đã được cập nhật từ Bước 2
              "qty_actual", happyPathQtyPlanned,
              "completion_type", "full"
          )
      )
  );
  ```
- **Assertion:** 
  - Status Code = `200 OK`.
  - `data.status` == `"completed"`.
  - Kiểm tra `data.stock_movements` Not Null (Chứng minh tồn kho đã biến động).

---

### Luồng 2: Từ chối duyệt (Reject Flow)
Hoạt động độc lập hoàn toàn, không phụ thuộc Luồng 1.
- **TC-INBOUND-06:** Tạo phiếu $\rightarrow$ Submit $\rightarrow$ Reject.
- **Logic tạo:** Dùng `DateUtils.todayAsIso()`, `qtyPlanned = 10`, `unitCost = 100.0`.
- **API Reject:** `POST /api/v1/inbound-documents/{id}/reject`. Payload: `{ "reason": "Không hợp lệ" }`.
- **Assertion:** Status Code = `200 OK`. `data.status` == `"rejected"`.
- **Negative Test:** Gọi tiếp lệnh Submit lên phiếu vừa reject $\rightarrow$ Phải trả về Status Code = `409 CONFLICT`.

---

### Luồng 3: Hủy phiếu (Cancel Flow)
Hoạt động độc lập hoàn toàn, không phụ thuộc Luồng 1.
- **TC-INBOUND-07:** Tạo phiếu $\rightarrow$ Cancel.
- **Logic tạo:** Dùng `DateUtils.todayAsIso()`, `qtyPlanned = 5`, `unitCost = 50.0`.
- **API Cancel:** `POST /api/v1/inbound-documents/{id}/cancel`. Payload: `{ "reason": "Hủy phiếu" }`.
- **Assertion:** Status Code = `200 OK`. `data.status` == `"cancelled"`.
- **Negative Test:** Gọi lệnh PATCH cập nhật thông tin (truyền note) lên phiếu vừa cancel $\rightarrow$ Phải trả về Status Code = `409 CONFLICT`.
