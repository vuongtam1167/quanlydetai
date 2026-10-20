package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.service.LecturerAdminService;
import com.example.quanlydetai.service.ReviewerAdminService;
import com.example.quanlydetai.service.RoundAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Trưởng khoa phân công GV phản biện; chặn GVPB trùng GVHD (BR-05). */
@Controller
@RequestMapping("/admin/reviewers")
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
public class AdminReviewerController {

    private final ReviewerAdminService reviewerService;
    private final LecturerAdminService lecturerService;
    private final RoundAdminService roundService;

    public AdminReviewerController(ReviewerAdminService reviewerService, LecturerAdminService lecturerService,
                                   RoundAdminService roundService) {
        this.reviewerService = reviewerService;
        this.lecturerService = lecturerService;
        this.roundService = roundService;
    }

    @GetMapping
    public String list(@RequestParam(value = "roundId", required = false) Long roundId, Model model) {
        model.addAttribute("rows", reviewerService.rows(roundId));
        model.addAttribute("lecturers", lecturerService.search(null, null));
        model.addAttribute("workload", reviewerService.workload());
        model.addAttribute("rounds", roundService.listViews(null));
        model.addAttribute("roundId", roundId);
        return "admin/reviewers/list";
    }

    @PostMapping("/{topicId}")
    public String assign(@PathVariable Long topicId, @RequestParam("reviewerId") Long reviewerId,
                         @RequestParam(value = "roundId", required = false) Long roundId,
                         RedirectAttributes redirect) {
        reviewerService.assign(topicId, reviewerId);
        redirect.addFlashAttribute("flashSuccess", "Đã phân công giảng viên phản biện.");
        return roundId == null ? "redirect:/admin/reviewers" : "redirect:/admin/reviewers?roundId=" + roundId;
    }
}
