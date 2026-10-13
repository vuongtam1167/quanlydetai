package com.example.quanlydetai.controller;

import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("topics", topicService.findAll());
        return "topics/list"; 
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_BO_MON', 'ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("topic", new Topic());
        model.addAttribute("topicId", null);
        return "topics/form";
    }

    // Form Binding và Validate lỗi
    @PostMapping
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_BO_MON', 'ADMIN')")
    public String create(@Valid @ModelAttribute("topic") Topic topic,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("topicId", null);
            return "topics/form";
        }
        topicService.createTopic(topic);
        return "redirect:/topics";
    }

    // Chỉ ADMIN mới có quyền xóa
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        topicService.deleteById(id);
        return "redirect:/topics";
    }
}