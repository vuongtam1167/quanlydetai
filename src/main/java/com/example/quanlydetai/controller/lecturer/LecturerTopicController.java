package com.example.quanlydetai.controller.lecturer;

import com.example.quanlydetai.dto.TopicForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.service.LecturerTopicService;
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

/** Giảng viên đề xuất, sửa, xóa đề tài ở Giai đoạn 1 (gửi Trưởng bộ môn thẩm định). */
@Controller
@RequestMapping("/lecturer/topics")
@PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_BO_MON')")
public class LecturerTopicController {

    private final LecturerTopicService topicService;

    public LecturerTopicController(LecturerTopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("rows", topicService.myTopics(principal.getName()));
        model.addAttribute("openRounds", topicService.openRounds());
        return "lecturer/topics/list";
    }

    @GetMapping("/new")
    public String createForm(Principal principal, Model model) {
        TopicForm form = new TopicForm();
        form.setDepartmentId(topicService.me(principal.getName()).getDepartment().getId());
        model.addAttribute("form", form);
        return formView(principal, model);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("form", topicService.toForm(id, principal.getName()));
        return formView(principal, model);
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") TopicForm form, BindingResult result,
                       Principal principal, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(principal, model);
        }
        try {
            topicService.save(form, principal.getName());
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            return formView(principal, model);
        }
        redirect.addFlashAttribute("flashSuccess",
                "Đã gửi đề tài \"" + form.getTitle() + "\" cho Trưởng bộ môn thẩm định.");
        return "redirect:/lecturer/topics";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        topicService.delete(id, principal.getName());
        redirect.addFlashAttribute("flashSuccess", "Đã xóa đề tài.");
        return "redirect:/lecturer/topics";
    }

    private String formView(Principal principal, Model model) {
        Lecturer me = topicService.me(principal.getName());
        model.addAttribute("advisor1Name", me.getFullName());
        model.addAttribute("rounds", topicService.openRounds());
        model.addAttribute("departments", topicService.departments());
        model.addAttribute("lecturers", topicService.otherLecturers(principal.getName()));
        return "lecturer/topics/form";
    }
}
