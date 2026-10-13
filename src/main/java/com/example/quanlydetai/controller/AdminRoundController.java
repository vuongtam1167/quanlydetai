package com.example.quanlydetai.controller;

import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.service.RegistrationRoundService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/rounds")
@PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')") // Bảo mật cấp phương thức/lớp[cite: 3]
public class AdminRoundController {

    private final RegistrationRoundService roundService;

    public AdminRoundController(RegistrationRoundService roundService) {
        this.roundService = roundService;
    }

    @GetMapping
    public String listRounds(Model model) {
        model.addAttribute("rounds", roundService.findAll());
        return "admin/rounds/list";
    }

    @GetMapping("/new")
    public String newRoundForm(Model model) {
        model.addAttribute("round", new RegistrationRound());
        return "admin/rounds/form";
    }

    @PostMapping
    public String saveRound(@ModelAttribute RegistrationRound round) {
        roundService.createRound(round);
        return "redirect:/admin/rounds";
    }

    @PostMapping("/{id}/publish")
    public String publishResults(@PathVariable Long id) {
        roundService.publishResults(id); // Gọi logic tổng hợp điểm & gửi email
        return "redirect:/admin/rounds";
    }
}