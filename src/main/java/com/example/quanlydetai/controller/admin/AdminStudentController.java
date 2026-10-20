package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.StudentForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.service.StudentAdminService;
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
@RequestMapping("/admin/students")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {

    private final StudentAdminService studentService;

    public AdminStudentController(StudentAdminService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("students", studentService.search(q));
        model.addAttribute("q", q == null ? "" : q);
        return "admin/students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new StudentForm());
        return "admin/students/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", studentService.toForm(id));
        return "admin/students/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") StudentForm form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/students/form";
        }
        try {
            studentService.save(form);
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            return "admin/students/form";
        }
        redirect.addFlashAttribute("flashSuccess", "Đã lưu sinh viên \"" + form.getFullName() + "\".");
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        studentService.delete(id);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa sinh viên.");
        return "redirect:/admin/students";
    }
}
