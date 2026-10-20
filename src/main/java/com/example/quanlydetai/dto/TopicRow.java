package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.enums.TopicStatus;

/** Một dòng đề tài hiển thị trong các bảng duyệt/thẩm định. */
public record TopicRow(Long id, String code, String title, String departmentName, String roundName,
                       String advisors, TopicStatus status, String note) {

    public static TopicRow of(Topic t, String advisors) {
        return new TopicRow(t.getId(), t.getCode(), t.getTitle(), t.getDepartment().getName(),
                t.getRound().getName(), advisors, t.getStatus(), t.getReviewNote());
    }

    /**
     * Quy ước trạng thái (giữ nguyên enum đã có trong CSDL):
     * PENDING = chờ Trưởng bộ môn; DEAN_APPROVED = bộ môn đã thông qua, chờ Trưởng khoa;
     * APPROVED = Trưởng khoa phê duyệt; REJECTED = bị từ chối / cần chỉnh sửa.
     */
    public String statusLabel() {
        return switch (status) {
            case PENDING -> "Chờ bộ môn thẩm định";
            case DEAN_APPROVED -> "Chờ Trưởng khoa duyệt";
            case APPROVED -> "Đã phê duyệt";
            case REJECTED -> "Bị từ chối / cần sửa";
        };
    }

    public String statusClass() {
        return switch (status) {
            case PENDING -> "badge-warn";
            case DEAN_APPROVED -> "badge-blue";
            case APPROVED -> "badge-green";
            case REJECTED -> "badge-red";
        };
    }

    public boolean waitingDean() {
        return status == TopicStatus.DEAN_APPROVED;
    }
}
