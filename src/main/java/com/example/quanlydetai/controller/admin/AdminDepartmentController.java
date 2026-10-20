package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.DepartmentForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.service.DepartmentAdminService;
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

@Controller
@RequestMapping("/admin/departments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDepartmentController {

    private final DepartmentAdminService departmentService;

    public AdminDepartmentController(DepartmentAdminService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", departmentService.list());
        model.addAttribute("lecturerCounts", departmentService.lecturerCounts());
        return "admin/departments/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new DepartmentForm());
        return "admin/departments/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", DepartmentForm.from(departmentService.get(id)));
        return "admin/departments/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") DepartmentForm form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/departments/form";
        }
        try {
            departmentService.save(form);
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            return "admin/departments/form";
        }
        redirect.addFlashAttribute("flashSuccess", "Đã lưu bộ môn \"" + form.getName() + "\".");
        return "redirect:/admin/departments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        departmentService.delete(id);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa bộ môn.");
        return "redirect:/admin/departments";
    }
}
