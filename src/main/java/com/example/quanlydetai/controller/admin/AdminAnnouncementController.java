package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.AnnouncementForm;
import com.example.quanlydetai.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/announcements")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    public AdminAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        model.addAttribute("form", new AnnouncementForm());
        return "admin/announcements/list";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        model.addAttribute("form", announcementService.toForm(id));
        return "admin/announcements/list";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") AnnouncementForm form, BindingResult result,
            Principal principal, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            if (form.getId() != null) {
                return "admin/announcements/form";
            }

            model.addAttribute(
                    "announcements",
                    announcementService.getAllAnnouncements()
            );
            return "admin/announcements/list";
        }

        boolean editing = form.getId() != null;
        if (editing) {
            announcementService.update(form);
        } else {
            announcementService.create(form, principal.getName());
        }

        redirect.addFlashAttribute("flashSuccess",
                editing ? "Đã cập nhật thông báo." : "Đã đăng thông báo.");
        return "redirect:/admin/announcements";
    }

    @PostMapping("/{id}/pin")
    public String pin(@PathVariable Long id, RedirectAttributes redirect) {
        announcementService.togglePin(id);
        redirect.addFlashAttribute("flashSuccess", "Đã cập nhật trạng thái ghim.");
        return "redirect:/admin/announcements";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        announcementService.delete(id);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa thông báo.");
        return "redirect:/admin/announcements";
    }
}
