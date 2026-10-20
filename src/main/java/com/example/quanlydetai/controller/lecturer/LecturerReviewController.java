package com.example.quanlydetai.controller.lecturer;

import com.example.quanlydetai.service.ReviewService;
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

/** Giảng viên phản biện chấm điểm và nhận xét các đề tài được phân công. */
@Controller
@RequestMapping("/lecturer/reviews")
@PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_BO_MON')")
public class LecturerReviewController {

    private final ReviewService reviewService;

    public LecturerReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("tasks", reviewService.tasks(principal.getName()));
        return "lecturer/reviews";
    }

    @PostMapping("/{topicId}")
    public String save(@PathVariable Long topicId,
                       @RequestParam(value = "score", required = false) BigDecimal score,
                       @RequestParam(value = "comments", required = false) String comments,
                       Principal principal, RedirectAttributes redirect) {
        reviewService.saveScore(topicId, score, comments, principal.getName());
        redirect.addFlashAttribute("flashSuccess", "Đã lưu điểm phản biện.");
        return "redirect:/lecturer/reviews";
    }
}
