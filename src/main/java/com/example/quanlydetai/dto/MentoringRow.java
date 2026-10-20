package com.example.quanlydetai.dto;

import java.util.List;

/** Tiến độ của nhóm sinh viên mà giảng viên đang hướng dẫn. */
public record MentoringRow(Long topicId, String topicCode, String topicTitle, String roundName,
                           String coAdvisor, String groupName, List<MemberView> members,
                           boolean submitted, String reportUrl, String sourceUrl, String notes) {

    public boolean hasGroup() {
        return groupName != null;
    }
}
