package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.CouncilForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.service.CouncilAdminService;
import com.example.quanlydetai.service.LecturerAdminService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Thành lập hội đồng bảo vệ (3-5 GV, có chủ tịch + thư ký) và phân nhóm vào hội đồng. */
@Controller
@RequestMapping("/admin/councils")
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
public class AdminCouncilController {

    private final CouncilAdminService councilService;
    private final LecturerAdminService lecturerService;
    private final RoundAdminService roundService;

    public AdminCouncilController(CouncilAdminService councilService, LecturerAdminService lecturerService,
                                  RoundAdminService roundService) {
        this.councilService = councilService;
        this.lecturerService = lecturerService;
        this.roundService = roundService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("councils", councilService.list());
        return "admin/councils/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new CouncilForm());
        return formView(model, null);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", councilService.toForm(id));
        return formView(model, id);
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") CouncilForm form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model, form.getId());
        }
        try {
            councilService.save(form);
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            return formView(model, form.getId());
        }
        redirect.addFlashAttribute("flashSuccess", "Đã lưu hội đồng \"" + form.getName() + "\".");
        return "redirect:/admin/councils";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        councilService.delete(id);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa hội đồng.");
        return "redirect:/admin/councils";
    }

    private String formView(Model model, Long councilId) {
        model.addAttribute("rounds", roundService.listViews(null));
        model.addAttribute("lecturers", lecturerService.search(null, null));
        model.addAttribute("groups", councilService.eligibleGroups(councilId));
        model.addAttribute("locked", councilService.isLocked(councilId));
        return "admin/councils/form";
    }
}
