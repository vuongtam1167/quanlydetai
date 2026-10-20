package com.example.quanlydetai.dto;

import java.math.BigDecimal;

/** Điểm của một thành viên hội đồng cho một nhóm (score = null nếu chưa chấm). */
public record MemberScore(String lecturerName, String roleLabel, BigDecimal score, String comments) {
}
