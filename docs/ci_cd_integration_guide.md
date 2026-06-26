# Hướng dẫn Tích hợp CI/CD (Cross-repo Trigger)

Tài liệu này hướng dẫn đội ngũ **Backend (BE)** hoặc **Frontend (FE)** cách cấu hình tự động gọi sang kho (repository) **Automation Test** này để chạy bộ kiểm thử mỗi khi họ deploy code mới.

## 1. Cơ chế hoạt động

Kho (repo) Automation Test đã được cấu hình mở cổng lắng nghe tín hiệu `repository_dispatch` từ bên ngoài.

Khi BE hoặc FE hoàn tất quá trình đưa (deploy) code lên môi trường Staging/Test, hệ thống CI/CD bên BE/FE sẽ dùng API của GitHub đánh một tín hiệu (WebHook) sang repo Automation Test. Repo Automation Test nhận tín hiệu, tự động kéo code mới nhất về và chạy.

Các cổng tín hiệu (event-type) hiện có:
- `run-api-tests` : Dành cho team Backend gọi để chạy riêng bộ Test API.
- `run-ui-tests` : Dành cho team Frontend gọi để chạy riêng bộ Test giao diện (UI).
- `run-all-tests` : Chạy toàn bộ (Cả API và UI).

## 2. Các bước tích hợp cho team Backend/Frontend

### Bước 2.1. Chuẩn bị Token (PAT)
Vì tính năng bảo mật, repo BE không thể gọi bừa sang repo Automation Test. Cần có một "chìa khóa":
1. Trưởng nhóm QA hoặc tài khoản chủ repo Automation Test vào GitHub > **Settings** > **Developer settings** > **Personal access tokens** > **Tokens (classic)**.
2. Tạo 1 token mới (chọn quyền `repo`).
3. Gửi Token đó cho Dev Backend/Frontend. Dev lưu Token đó vào **Secrets** của repo họ (Ví dụ đặt tên là `QA_AUTO_TRIGGER_TOKEN`).

### Bước 2.2. Thêm cấu hình gọi chéo vào file CI/CD của Backend/Frontend
Trong file chạy CI/CD (VD: `.github/workflows/deploy-staging.yml`) của repo Backend, thêm một **Job cuối cùng** như sau:

```yaml
jobs:
  # ... Các job build và deploy hiện tại của BE ...
  
  trigger-qa-automation:
    name: "Gửi lệnh chạy QA Auto Test"
    needs: [ tên-job-deploy-cua-BE ] # Đảm bảo BE deploy xong mới chạy job này
    runs-on: ubuntu-latest
    steps:
      - name: Kích hoạt chạy API Test bên kho QA
        uses: peter-evans/repository-dispatch@v3
        with:
          token: ${{ secrets.QA_AUTO_TRIGGER_TOKEN }}
          repository: <tên-tổ-chức>/<tên-repo-automation-erp> # VD: UpBase-Tech/automation_erp
          event-type: run-api-tests
```

## 3. Theo dõi kết quả

Sau khi BE trigger thành công, đội ngũ QA (hoặc Dev) có thể vào tab **Actions** của repo **automation_erp** để xem tiến trình chạy test.

Nếu có kết nối với công cụ chat (Slack/Teams/Telegram), kết quả Pass/Fail sẽ tự động được gửi về group chung kèm link tải **Extent Report** chi tiết.
