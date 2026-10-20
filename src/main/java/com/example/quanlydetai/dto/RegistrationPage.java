package com.example.quanlydetai.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Dữ liệu trang đăng ký đề tài Giai đoạn 2 cho một đợt. */
public record RegistrationPage(String roundName, LocalDateTime windowStart, LocalDateTime windowEnd,
                               String windowState, boolean windowOpen, GroupView group,
                               List<TopicOption> topics) {

    public boolean hasGroup() {
        return group != null;
    }
}
