package com.example.quanlydetai.dto;

/** Trạng thái nộp báo cáo của nhóm (chỉ nhóm trưởng được nộp). */
public record SubmissionView(Long groupId, String groupName, String roundName, boolean hasTopic,
        String topicCode, String topicTitle, String advisors, boolean iAmLeader,
        boolean submitted, String reportUrl, String sourceUrl, String notes,
        boolean locked) {
}
