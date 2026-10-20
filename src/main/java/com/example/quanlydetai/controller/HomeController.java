package com.example.quanlydetai.controller;

import com.example.quanlydetai.service.AnnouncementService;
import com.example.quanlydetai.service.RoundAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Trang chủ (thông báo + đợt đăng ký) và trang đăng nhập.
 * Controller chỉ điều phối: nhận tham số -> gọi Service -> đưa dữ liệu vào Model -> trả tên View.
 */
@Controller
public class HomeController {

    private static final int PAGE_SIZE = 10;

    private final AnnouncementService announcementService;
    private final RoundAdminService roundService;
    private final boolean showDemoAccounts;

    public HomeController(AnnouncementService announcementService,
                          RoundAdminService roundService,
                          @Value("${app.demo-accounts:false}") boolean showDemoAccounts) {
        this.announcementService = announcementService;
        this.roundService = roundService;
        this.showDemoAccounts = showDemoAccounts;
    }

    @GetMapping({"/", "/home"})
    public String index(@RequestParam(value = "q", required = false) String q,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        Model model) {
        model.addAttribute("page", announcementService.search(q, page, PAGE_SIZE));
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("rounds", roundService.listViews(null).stream().limit(8).toList());
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        model.addAttribute("showDemo", showDemoAccounts);
        return "login";
    }
}
