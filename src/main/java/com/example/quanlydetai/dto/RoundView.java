package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.RegistrationRound;

import java.time.LocalDateTime;

/** Dữ liệu đợt đăng ký đã tính sẵn tiến độ hiện tại để hiển thị. */
public record RoundView(Long id, String name, String typeLabel,
        LocalDateTime lecturerStart, LocalDateTime lecturerEnd,
        LocalDateTime studentStart, LocalDateTime studentEnd,
        boolean published, String phaseLabel, String phaseClass) {

    public static RoundView of(RegistrationRound r, LocalDateTime now) {
        String label;
        String css;
        if (now.isBefore(r.getLecturerStartDate())) {
            label = "Sắp mở";
            css = "badge-gray";
        } else if (!now.isAfter(r.getLecturerEndDate())) {
            label = "GĐ1: Giảng viên đề xuất";
            css = "badge-warn";
        } else if (now.isBefore(r.getStudentStartDate())) {
            label = "Chờ duyệt đề tài";
            css = "badge-gray";
        } else if (!now.isAfter(r.getStudentEndDate())) {
            label = "GĐ2: Sinh viên đăng ký";
            css = "badge-blue";
        } else if (Boolean.TRUE.equals(r.getIsPublished())) {
            label = "Đã công bố kết quả";
            css = "badge-green";
        } else {
            label = "Đang thực hiện / chấm điểm";
            css = "badge-info";
        }
        String type = switch (r.getType()) {
            case MON_HOC -> "Môn học";
            case NCKH -> "NCKH";
            case TLCN -> "TLCN";
            case KLTN -> "KLTN";
        };
        return new RoundView(r.getId(), r.getName(), type,
                r.getLecturerStartDate(), r.getLecturerEndDate(),
                r.getStudentStartDate(), r.getStudentEndDate(),
                Boolean.TRUE.equals(r.getIsPublished()), label, css);
    }
}
