package com.example.quanlydetai.dto;

/**
 * Kết quả công bố điểm: số nhóm có điểm, số nhóm thiếu điểm, số email gửi được/thất bại.
 */
public record PublishSummary(int scoredGroups, int missingGroups, int emailsSent, int emailsFailed) {
}
