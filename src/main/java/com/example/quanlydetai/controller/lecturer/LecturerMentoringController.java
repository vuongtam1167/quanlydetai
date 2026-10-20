package com.example.quanlydetai.controller.lecturer;

import com.example.quanlydetai.service.MentoringService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/** Theo dõi các nhóm sinh viên do mình hướng dẫn. */
@Controller
@RequestMapping("/lecturer/mentoring")
@PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_BO_MON')")
public class LecturerMentoringController {

    private final MentoringService mentoringService;

    public LecturerMentoringController(MentoringService mentoringService) {
        this.mentoringService = mentoringService;
    }

    @GetMapping
    public String view(Principal principal, Model model) {
        model.addAttribute("rows", mentoringService.rows(principal.getName()));
        return "lecturer/mentoring";
    }
}
