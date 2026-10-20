package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.Labels;
import com.example.quanlydetai.dto.RoundForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.service.RoundAdminService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Trưởng khoa tạo/sửa/xóa đợt đăng ký (2 giai đoạn). */
@Controller
@RequestMapping("/admin/rounds")
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
public class AdminRoundController {

    private final RoundAdminService roundService;

    public AdminRoundController(RoundAdminService roundService) {
        this.roundService = roundService;
    }

    @GetMapping
    public String list(@RequestParam(value = "type", required = false) String type, Model model) {
        model.addAttribute("rounds", roundService.listViews(type));
        model.addAttribute("topicCounts", roundService.topicCounts());
        model.addAttribute("groupCounts", roundService.groupCounts());
        model.addAttribute("typeLabels", Labels.roundTypes());
        model.addAttribute("type", type == null ? "" : type);
        return "admin/rounds/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new RoundForm());
        return formView(model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", roundService.toForm(id));
        return formView(model);
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") RoundForm form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model);
        }
        try {
            roundService.save(form);
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            return formView(model);
        }
        redirect.addFlashAttribute("flashSuccess", "Đã lưu đợt đăng ký \"" + form.getName() + "\".");
        return "redirect:/admin/rounds";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        roundService.delete(id);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa đợt đăng ký.");
        return "redirect:/admin/rounds";
    }

    private String formView(Model model) {
        model.addAttribute("typeLabels", Labels.roundTypes());
        return "admin/rounds/form";
    }
}
