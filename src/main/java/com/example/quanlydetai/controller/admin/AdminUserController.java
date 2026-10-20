package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.Labels;
import com.example.quanlydetai.service.UserAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserAdminService userService;

    public AdminUserController(UserAdminService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "role", required = false) String role, Model model) {
        model.addAttribute("users", userService.search(q, role));
        model.addAttribute("roleLabels", Labels.allRoles());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("role", role == null ? "" : role);
        return "admin/users/list";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        userService.toggleEnabled(id, principal.getName());
        redirect.addFlashAttribute("flashSuccess", "Đã cập nhật trạng thái tài khoản.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirect) {
        userService.resetPassword(id);
        redirect.addFlashAttribute("flashSuccess", "Đã đặt lại mật khẩu về mặc định (123456).");
        return "redirect:/admin/users";
    }
}
