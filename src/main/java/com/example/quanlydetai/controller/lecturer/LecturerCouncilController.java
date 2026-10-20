package com.example.quanlydetai.controller.lecturer;

import com.example.quanlydetai.service.CouncilGradingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

/** Không gian chấm điểm hội đồng; Chủ tịch có thêm bảng tổng hợp và nút hoàn tất hội đồng. */
@Controller
@RequestMapping("/lecturer/councils")
@PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_BO_MON')")
public class LecturerCouncilController {

    private final CouncilGradingService gradingService;

    public LecturerCouncilController(CouncilGradingService gradingService) {
        this.gradingService = gradingService;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("councils", gradingService.tasks(principal.getName()));
        return "lecturer/councils";
    }

    @PostMapping("/groups/{groupId}/score")
    public String score(@PathVariable Long groupId,
                        @RequestParam(value = "score", required = false) BigDecimal score,
                        @RequestParam(value = "comments", required = false) String comments,
                        Principal principal, RedirectAttributes redirect) {
        gradingService.saveScore(groupId, score, comments, principal.getName());
        redirect.addFlashAttribute("flashSuccess", "Đã lưu phiếu chấm.");
        return "redirect:/lecturer/councils";
    }

    @PostMapping("/{councilId}/complete")
    public String complete(@PathVariable Long councilId, Principal principal, RedirectAttributes redirect) {
        gradingService.complete(councilId, principal.getName());
        redirect.addFlashAttribute("flashSuccess", "Đã hoàn tất hội đồng. Khoa có thể tổng kết và công bố điểm.");
        return "redirect:/lecturer/councils";
    }
}
