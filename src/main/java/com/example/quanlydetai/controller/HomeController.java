package com.example.quanlydetai.controller;

import com.example.quanlydetai.service.AnnouncementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AnnouncementService announcementService;

    // Constructor Injection
    public HomeController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Truyền dữ liệu vào model để view Thymeleaf hiển thị
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        return "index"; // Trả về file index.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}