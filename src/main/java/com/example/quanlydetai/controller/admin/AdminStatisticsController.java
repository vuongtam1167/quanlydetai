package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Trang thống kê trực quan cho Trưởng khoa / Admin (quản lý nhiều đợt đăng ký cùng lúc). */
@Controller
@RequestMapping("/admin/statistics")
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    public AdminStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("stats", statisticsService.build());
        return "admin/statistics";
    }
}
