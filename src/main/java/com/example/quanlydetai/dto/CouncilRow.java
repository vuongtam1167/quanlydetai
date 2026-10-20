package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.enums.CouncilStatus;

import java.time.LocalDateTime;

public record CouncilRow(Long id, String name, String roundName, LocalDateTime defenseDate, String location,
                         CouncilStatus status, String chairName, String secretaryName,
                         int memberCount, long groupCount, boolean locked) {

    public String statusLabel() {
        return switch (status) {
            case PLANNED -> "Chưa bảo vệ";
            case IN_SESSION -> "Đang bảo vệ";
            case COMPLETED -> "Đã hoàn thành";
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
