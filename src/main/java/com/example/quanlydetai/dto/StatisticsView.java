package com.example.quanlydetai.dto;

import java.util.List;

/**
 * Số liệu thống kê tổng quan cho Trưởng khoa / Admin
 */
public record StatisticsView(long rounds, long publishedRounds, long topics, long groups, long students,
        long lecturers, long councils,
        long awaitingDean, long awaitingDepartment, long groupsWithoutReviewer,
        long groupsWithoutCouncil,
        List<StatItem> topicsByStatus, List<StatItem> topicsByDepartment,
        List<StatItem> groupsByRound, List<StatItem> scoreBuckets,
        List<StatItem> reviewerLoad) {
}
