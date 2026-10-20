package com.example.quanlydetai.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/** Nhãn tiếng Việt dùng cho các ô chọn (select) trên giao diện. */
public final class Labels {

    private Labels() {
    }

    public static Map<String, String> roundTypes() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("MON_HOC", "Môn học");
        m.put("NCKH", "Nghiên cứu khoa học (NCKH)");
        m.put("TLCN", "Tiểu luận chuyên ngành (TLCN)");
        m.put("KLTN", "Khóa luận tốt nghiệp (KLTN)");
        return m;
    }

    public static Map<String, String> topicStatuses() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("DEAN_APPROVED", "Chờ Trưởng khoa duyệt");
        m.put("PENDING", "Chờ bộ môn thẩm định");
        m.put("APPROVED", "Đã phê duyệt");
        m.put("REJECTED", "Bị từ chối / cần sửa");
        m.put("ALL", "Tất cả");
        return m;
    }

    public static Map<String, String> lecturerRoles() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("GIANG_VIEN", "Giảng viên");
        m.put("TRUONG_BO_MON", "Trưởng bộ môn");
        m.put("TRUONG_KHOA", "Trưởng khoa");
        return m;
    }

    public static Map<String, String> allRoles() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("ADMIN", "Quản trị viên");
        m.put("TRUONG_KHOA", "Trưởng khoa");
        m.put("TRUONG_BO_MON", "Trưởng bộ môn");
        m.put("GIANG_VIEN", "Giảng viên");
        m.put("SINH_VIEN", "Sinh viên");
        return m;
    }

    public static String roundTypeShort(com.example.quanlydetai.model.enums.RoundType type) {
        return switch (type) {
            case MON_HOC -> "Môn học";
            case NCKH -> "NCKH";
            case TLCN -> "TLCN";
            case KLTN -> "KLTN";
        };
    }
}
