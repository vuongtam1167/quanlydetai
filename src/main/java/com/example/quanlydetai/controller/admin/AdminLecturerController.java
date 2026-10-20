package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.Labels;
import com.example.quanlydetai.dto.LecturerForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.service.DepartmentAdminService;
import com.example.quanlydetai.service.LecturerAdminService;
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

@Controller
@RequestMapping("/admin/lecturers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLecturerController {

    private final LecturerAdminService lecturerService;
    private final DepartmentAdminService departmentService;

    public AdminLecturerController(LecturerAdminService lecturerService, DepartmentAdminService departmentService) {
        this.lecturerService = lecturerService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "departmentId", required = false) Long departmentId,
                       Model model) {
        model.addAttribute("lecturers", lecturerService.search(q, departmentId));
        model.addAttribute("departments", departmentService.list());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("departmentId", departmentId);
        return "admin/lecturers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new LecturerForm());
        return formView(model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", lecturerService.toForm(id));
        return formView(model);
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") LecturerForm form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model);
        }
        try {
            lecturerService.save(form);
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            return formView(model);
        }
        redirect.addFlashAttribute("flashSuccess", "Đã lưu giảng viên \"" + form.getFullName() + "\".");
        return "redirect:/admin/lecturers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        lecturerService.delete(id);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa giảng viên.");
        return "redirect:/admin/lecturers";
    }

    private String formView(Model model) {
        model.addAttribute("departments", departmentService.list());
        model.addAttribute("roleLabels", Labels.lecturerRoles());
        return "admin/lecturers/form";
    }
}
