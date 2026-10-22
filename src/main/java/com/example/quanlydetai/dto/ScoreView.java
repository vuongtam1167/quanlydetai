package com.example.quanlydetai.dto;

import java.math.BigDecimal;

/**
 * Kết quả đánh giá của nhóm trong một đợt (chỉ có số liệu khi Khoa đã công bố).
 */
public record ScoreView(String roundName, String groupName, String topicCode, String topicTitle,
        boolean published, BigDecimal reviewerScore, String reviewerComments,
        BigDecimal councilScore, String councilComments, BigDecimal finalScore,
        boolean passed) {

    public boolean hasFinal() {
        return finalScore != null;
    }
}
