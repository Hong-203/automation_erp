package com.automation_erp.framework.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích xử lý ngày tháng dùng trong test:
 * - Format datetime cho filter query params
 * - Tính khoảng thời gian (addDays, subtractDays)
 * - Chuyển đổi sang ISO 8601 để gửi trong API request body
 */
public final class DateUtils {

    private DateUtils() {}

    public static final DateTimeFormatter ISO_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =====================================================================
    // Lấy thời điểm hiện tại
    // =====================================================================

    /** Trả về LocalDateTime hiện tại (UTC) */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /** Trả về ngày hiện tại (LocalDate) */
    public static LocalDate today() {
        return LocalDate.now();
    }

    // =====================================================================
    // Format → String
    // =====================================================================

    /** Format datetime hiện tại thành ISO 8601: "2024-07-01T14:30:00Z" */
    public static String nowAsIso() {
        return now().format(ISO_DATE_TIME);
    }

    /** Format ngày hiện tại thành ISO date: "2024-07-01" */
    public static String todayAsIso() {
        return today().format(ISO_DATE);
    }

    /** Format datetime tùy chỉnh */
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    /** Format ngày tùy chỉnh */
    public static String format(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    // =====================================================================
    // Tính khoảng thời gian
    // =====================================================================

    /** Cộng thêm N ngày từ hôm nay */
    public static LocalDate addDays(int days) {
        return today().plusDays(days);
    }

    /** Trừ N ngày từ hôm nay */
    public static LocalDate subtractDays(int days) {
        return today().minusDays(days);
    }

    /** Cộng thêm N ngày từ mốc ngày bất kỳ */
    public static LocalDate addDays(LocalDate base, int days) {
        return base.plusDays(days);
    }

    // =====================================================================
    // Chuyển đổi sang String phục vụ filter query param
    // =====================================================================

    /**
     * Trả về chuỗi ngày bắt đầu kỳ báo cáo (N ngày trước hôm nay).
     * VD dùng trong: /reports/inventory-xnt?from=2024-06-01&to=2024-07-01
     */
    public static String startDateOfLastNDays(int days) {
        return subtractDays(days).format(ISO_DATE);
    }

    /** Trả về ngày hôm nay dạng ISO cho end date */
    public static String endDateToday() {
        return todayAsIso();
    }
}
