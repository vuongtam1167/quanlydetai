package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.MenuGroup;
import com.example.quanlydetai.dto.MenuItem;
import com.example.quanlydetai.model.enums.Role;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu sidebar theo vai trò. Mục có ready=false hiển thị mờ ("Sắp có") cho tới khi
 * chức năng tương ứng hoàn thành - đổi thành true là mục sáng lên.
 */
@Service
public class MenuService {

    public String roleLabel(Role role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case ADMIN -> "Quản trị viên";
            case TRUONG_KHOA -> "Trưởng khoa";
            case TRUONG_BO_MON -> "Trưởng bộ môn";
            case GIANG_VIEN -> "Giảng viên";
            case SINH_VIEN -> "Sinh viên";
        };
    }

    public List<MenuGroup> menuFor(Role role) {
        List<MenuGroup> groups = new ArrayList<>();
        if (role == null) {
            return groups;
        }
        groups.add(new MenuGroup("TRANG CÁ NHÂN", List.of(
                new MenuItem("Thông báo & đợt đăng ký", "🏠", "/", true))));

        switch (role) {
            case ADMIN, TRUONG_KHOA -> {
                groups.add(new MenuGroup("QUẢN LÝ ĐỀ TÀI", List.of(
                        new MenuItem("Đợt đăng ký", "📅", "/admin/rounds", true),
                        new MenuItem("Phê duyệt đề tài", "✅", "/admin/topics", true),
                        new MenuItem("Phân công GV phản biện", "👨‍🏫", "/admin/reviewers", true),
                        new MenuItem("Hội đồng bảo vệ", "⚖️", "/admin/councils", true),
                        new MenuItem("Kết quả & công bố", "📢", "/admin/results", true),
                        new MenuItem("Thống kê", "📊", "/admin/statistics", true))));
                if (role == Role.ADMIN) {
                    groups.add(new MenuGroup("DANH MỤC HỆ THỐNG", List.of(
                            new MenuItem("Tài khoản", "🔑", "/admin/users", true),
                            new MenuItem("Bộ môn", "🏢", "/admin/departments", true),
                            new MenuItem("Giảng viên", "🎓", "/admin/lecturers", true),
                            new MenuItem("Sinh viên", "👥", "/admin/students", true),
                            new MenuItem("Thông báo", "🔔", "/admin/announcements", true))));
                }
            }
            case TRUONG_BO_MON -> {
                groups.add(new MenuGroup("BỘ MÔN", List.of(
                        new MenuItem("Thẩm định đề tài", "📑", "/department/topics", true))));
                groups.add(lecturerGroup());
            }
            case GIANG_VIEN -> groups.add(lecturerGroup());
            case SINH_VIEN -> groups.add(new MenuGroup("SINH VIÊN", List.of(
                    new MenuItem("Không gian nhóm", "👥", "/student/group", true),
                    new MenuItem("Đăng ký đề tài", "🎯", "/student/topics", true),
                    new MenuItem("Nộp báo cáo", "📤", "/student/submission", true),
                    new MenuItem("Tra cứu điểm", "📊", "/student/scores", true))));
        }
        return groups;
    }

    private MenuGroup lecturerGroup() {
        return new MenuGroup("GIẢNG VIÊN", List.of(
                new MenuItem("Đề xuất đề tài", "💡", "/lecturer/topics", true),
                new MenuItem("Nhóm SV hướng dẫn", "👥", "/lecturer/mentoring", true),
                new MenuItem("Chấm điểm phản biện", "📝", "/lecturer/reviews", true),
                new MenuItem("Chấm điểm hội đồng", "🏛️", "/lecturer/councils", true)));
    }
}
