package com.example.quanlydetai.dto;

import java.math.BigDecimal;
import java.util.Set;

/** Một dòng phân công GV phản biện: nhóm + đề tài + GVPB hiện tại. */
public record ReviewerRow(Long topicId, String groupName, String topicCode, String topicTitle,
                          String roundName, String advisorNames, Set<Long> advisorIds,
                          Long reviewerId, String reviewerName, BigDecimal score) {

    public boolean scored() {
        return score != null;
    }
}
