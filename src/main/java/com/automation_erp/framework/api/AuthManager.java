package com.automation_erp.framework.api;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý Token (Authentication Cache)
 * Đảm bảo mỗi user (email) chỉ gọi API login 1 lần duy nhất trong suốt phiên chạy Test Suite,
 * hoặc tự động gọi lại khi token hết hạn.
 */
public class AuthManager {

    // Thời gian sống giả định của token (Ví dụ: 55 phút = 55 * 60 * 1000 ms).
    // Đặt nhỏ hơn thời gian thực tế một chút (buffer) để tránh request bị failed sát nút.
    private static final long TOKEN_LIFETIME_MS = 55 * 60 * 1000;

    private static final ConcurrentHashMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    private AuthManager() {
        // Private constructor for Utility class
    }

    /**
     * Lấy token cho một user. Nếu chưa có hoặc đã hết hạn thì gọi API login.
     * @param email    Email của user (ví dụ admin@erp.vn)
     * @param password Mật khẩu
     * @return Auth Token
     */
    public static synchronized String getToken(String email, String password) {
        // Ưu tiên đọc token tĩnh từ config (nếu có)
        String staticToken = com.automation_erp.framework.config.ConfigReader.getProperty("auth.token");
        if (staticToken != null && !staticToken.trim().isEmpty()) {
            return staticToken;
        }

        TokenInfo info = tokenCache.get(email);
        long currentTime = System.currentTimeMillis();

        // Kiểm tra xem token có tồn tại và còn hạn hay không
        if (info != null && currentTime < info.getExpiryTime()) {
            return info.getToken(); // Token còn hợp lệ, lấy từ cache
        }

        // Nếu chưa có, hoặc đã hết hạn -> Gọi API lấy token mới
        System.out.println("[AuthManager] Đang gọi API lấy token mới cho user: " + email);
        String newToken = ApiClient.login(email, password);

        // Lưu vào cache kèm theo timestamp hết hạn
        tokenCache.put(email, new TokenInfo(newToken, currentTime + TOKEN_LIFETIME_MS));

        return newToken;
    }

    /**
     * Xóa token (Dùng khi test case yêu cầu force logout hoặc clean session)
     */
    public static void clearToken(String email) {
        tokenCache.remove(email);
    }

    /**
     * Xóa toàn bộ token trong bộ nhớ
     */
    public static void clearAll() {
        tokenCache.clear();
    }

    // Class nội bộ lưu trữ dữ liệu Token và Thời điểm hết hạn
    private static class TokenInfo {
        private final String token;
        private final long expiryTime;

        public TokenInfo(String token, long expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public String getToken() {
            return token;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }
}
