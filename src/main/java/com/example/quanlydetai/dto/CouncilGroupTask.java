package com.example.quanlydetai.dto;

import java.math.BigDecimal;
import java.util.List;

/*
 Một nhóm bảo vệ trong hội đồng, nhìn từ góc độ của giảng viên đang đăng nhập.
 */
public record CouncilGroupTask(Long groupId, String groupName, String topicCode, String topicTitle,
        List<MemberView> members, boolean submitted, String reportUrl,
        String sourceUrl, boolean conflict, BigDecimal myScore, String myComments,
        List<MemberScore> scores, BigDecimal average, int scoredCount,
        int totalMembers) {

    public boolean scoredByMe() {
        return myScore != null;
    }

    public boolean allScored() {
        return totalMembers > 0 && scoredCount >= totalMembers;
    }
}
