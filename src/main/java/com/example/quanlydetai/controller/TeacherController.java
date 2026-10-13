package com.example.quanlydetai.controller;

import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.service.ReviewAssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/giangvien")
@PreAuthorize("hasRole('GIANG_VIEN')")
public class TeacherController {

    private final ReviewAssignmentRepository reviewAssignmentRepository;
    private final ReviewAssignmentService reviewAssignmentService;
    private final LecturerRepository lecturerRepository;

    public TeacherController(ReviewAssignmentRepository reviewAssignmentRepository, 
                             ReviewAssignmentService reviewAssignmentService,
                             LecturerRepository lecturerRepository) {
        this.reviewAssignmentRepository = reviewAssignmentRepository;
        this.reviewAssignmentService = reviewAssignmentService;
        this.lecturerRepository = lecturerRepository;
    }

    @GetMapping("/cham-diem")
    public String listAssignments(Model model, Principal principal) {
        Lecturer lecturer = lecturerRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Tài khoản chưa liên kết Giảng viên"));
                
        model.addAttribute("assignments", reviewAssignmentRepository.findAll()); // Thực tế sẽ filter theo lecturer.getId()
        return "giangvien/cham-diem";
    }

    @PostMapping("/cham-diem/{id}")
    public String submitScore(@PathVariable Long id, 
                              @RequestParam("score") BigDecimal score, 
                              @RequestParam("comments") String comments) {
        reviewAssignmentService.submitReviewScore(id, score, comments);
        return "redirect:/giangvien/cham-diem?success";
    }
}