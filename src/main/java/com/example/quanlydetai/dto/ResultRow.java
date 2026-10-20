package com.example.quanlydetai.dto;

import java.math.BigDecimal;

/** Điểm tổng kết của một nhóm: điểm GVPB, điểm TB hội đồng và điểm cuối (trung bình cộng các thành phần). */
public record ResultRow(Long groupId, String groupName, String topicCode, String topicTitle,
                        BigDecimal reviewerScore, BigDecimal councilScore, int councilCount,
                        BigDecimal finalScore) {

    public boolean hasFinal() {
        return finalScore != null;
    }

    public boolean passed() {
        return finalScore != null && finalScore.compareTo(new BigDecimal("5.0")) >= 0;
    }
}
