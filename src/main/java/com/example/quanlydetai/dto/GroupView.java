package com.example.quanlydetai.dto;

import java.util.List;

/** Nhóm sinh viên trong một đợt, nhìn từ góc độ của sinh viên đang đăng nhập. */
public record GroupView(Long id, String name, Long roundId, String roundName, String status,
                        boolean iAmLeader, boolean topicRegistered, String topicCode, String topicTitle,
                        List<GroupMemberView> members, int minMembers, int maxMembers) {

    public int size() {
        return members.size();
    }

    /** Chỉ thay đổi thành viên khi nhóm chưa đăng ký đề tài. */
    public boolean editable() {
        return !topicRegistered;
    }

    public boolean enoughMembers() {
        return members.size() >= minMembers && members.size() <= maxMembers;
    }

    public boolean canAddMore() {
        return members.size() < maxMembers;
    }
}
