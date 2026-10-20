package com.example.quanlydetai.controller.api;

import com.example.quanlydetai.dto.RoundView;
import com.example.quanlydetai.dto.StatisticsView;
import com.example.quanlydetai.dto.TopicRow;
import com.example.quanlydetai.service.RoundAdminService;
import com.example.quanlydetai.service.StatisticsService;
import com.example.quanlydetai.service.TopicApprovalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API (JSON) - dùng HTTP Basic (vd Postman). Chỉ trả DTO, không bao giờ trả entity
 * nên không lộ mật khẩu. Người dùng thường chỉ xem được đề tài đã phê duyệt.
 */
@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final RoundAdminService roundService;
    private final TopicApprovalService topicService;
    private final StatisticsService statisticsService;

    public ApiController(RoundAdminService roundService, TopicApprovalService topicService,
                         StatisticsService statisticsService) {
        this.roundService = roundService;
        this.topicService = topicService;
        this.statisticsService = statisticsService;
    }

    /** GET /api/v1/rounds?type=KLTN - danh sách đợt đăng ký kèm tiến độ. */
    @GetMapping("/rounds")
    public List<RoundView> rounds(@RequestParam(value = "type", required = false) String type) {
        return roundService.listViews(type);
    }

    /** GET /api/v1/topics?roundId=1&status=APPROVED - danh sách đề tài. */
    @GetMapping("/topics")
    public List<TopicRow> topics(@RequestParam(value = "roundId", required = false) Long roundId,
                                 @RequestParam(value = "status", required = false) String status,
                                 Authentication auth) {
        boolean manager = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TRUONG_KHOA"));
        String effective = manager && status != null ? status : "APPROVED";
        return topicService.list(effective, roundId);
    }

    /** GET /api/v1/statistics - chỉ Admin / Trưởng khoa. */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
    public StatisticsView statistics() {
        return statisticsService.build();
    }
}
