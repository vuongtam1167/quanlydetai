package com.example.quanlydetai.dto;

/**
 * Đề tài đã được duyệt trong danh sách để nhóm sinh viên đăng ký (Giai đoạn 2).
 */
public record TopicOption(Long id, String code, String title, String departmentName, String advisors,
        String description, String requirements, boolean taken, boolean takenByMyGroup) {
}
