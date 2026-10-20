package com.example.quanlydetai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Một đề tài được phân công cho giảng viên phản biện chấm điểm. */
public record ReviewTaskRow(Long topicId, String topicCode, String topicTitle, String groupName,
                            String roundName, String advisorNames, List<MemberView> members,
                            boolean submitted, String reportUrl, String sourceUrl,
                            BigDecimal score, String comments, LocalDateTime deadline,
                            boolean overdue, boolean locked, boolean deadlineClosed) {

    public boolean scored() {
        return score != null;
    }
}
