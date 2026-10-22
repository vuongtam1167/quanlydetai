package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.enums.CouncilStatus;

import java.time.LocalDateTime;
import java.util.List;

/** Hội đồng mà giảng viên là thành viên, cùng danh sách nhóm cần chấm. */
public record CouncilTask(Long councilId, String name, String roundName, LocalDateTime defenseDate,
        String location, CouncilStatus status, boolean chair, String myRoleLabel,
        boolean readyToComplete, List<CouncilGroupTask> groups) {

    public boolean completed() {
        return status == CouncilStatus.COMPLETED;
    }

    public String statusLabel() {
        return switch (status) {
            case PLANNED -> "Chưa bảo vệ";
            case IN_SESSION -> "Đang chấm điểm";
            case COMPLETED -> "Đã hoàn tất";
        };
    }

    public String statusClass() {
        return switch (status) {
            case PLANNED -> "badge-gray";
            case IN_SESSION -> "badge-warn";
            case COMPLETED -> "badge-green";
        };
    }
}
