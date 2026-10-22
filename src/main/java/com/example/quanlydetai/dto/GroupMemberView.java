package com.example.quanlydetai.dto;

public record GroupMemberView(Long memberId, Long studentId, String studentCode, String fullName,
        String className, boolean leader, boolean me) {
}
