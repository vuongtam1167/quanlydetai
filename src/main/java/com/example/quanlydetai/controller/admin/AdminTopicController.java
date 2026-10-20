package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.Labels;
import com.example.quanlydetai.service.RoundAdminService;
import com.example.quanlydetai.service.TopicApprovalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Trưởng khoa phê duyệt đề tài (Giai đoạn 1) sau khi Trưởng bộ môn đã thông qua. */
@Controller
@RequestMapping("/admin/topics")
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
public class AdminTopicController {

    private final TopicApprovalService approvalService;
    private final RoundAdminService roundService;

    public AdminTopicController(TopicApprovalService approvalService, RoundAdminService roundService) {
        this.approvalService = approvalService;
        this.roundService = roundService;
    }

    @GetMapping
    public String list(@RequestParam(value = "status", defaultValue = "DEAN_APPROVED") String status,
                       @RequestParam(value = "roundId", required = false) Long roundId,
                       Model model) {
        model.addAttribute("topics", approvalService.list(status, roundId));
        model.addAttribute("counts", approvalService.counts());
        model.addAttribute("statusLabels", Labels.topicStatuses());
        model.addAttribute("rounds", roundService.listViews(null));
        model.addAttribute("status", status);
        model.addAttribute("roundId", roundId);
        return "admin/topics/list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        approvalService.approve(id);
        redirect.addFlashAttribute("flashSuccess", "Đã phê duyệt đề tài. Sinh viên sẽ thấy đề tài này ở Giai đoạn 2.");
        return "redirect:/admin/topics";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam("reason") String reason,
                         RedirectAttributes redirect) {
        approvalService.reject(id, reason);
        redirect.addFlashAttribute("flashSuccess", "Đã từ chối đề tài và gửi lý do cho giảng viên.");
        return "redirect:/admin/topics";
    }
}
