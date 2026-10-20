package com.example.quanlydetai.controller.student;

import com.example.quanlydetai.dto.RoundOption;
import com.example.quanlydetai.service.DepartmentAdminService;
import com.example.quanlydetai.service.GroupWorkspaceService;
import com.example.quanlydetai.service.TopicRegistrationService;
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
import java.util.List;

/** Giai đoạn 2: nhóm trưởng đăng ký đề tài cho nhóm; có tìm kiếm/lọc theo bộ môn. */
@Controller
@RequestMapping("/student/topics")
@PreAuthorize("hasRole('SINH_VIEN')")
public class StudentTopicController {

    private final TopicRegistrationService registrationService;
    private final GroupWorkspaceService workspace;
    private final DepartmentAdminService departmentService;

    public StudentTopicController(TopicRegistrationService registrationService, GroupWorkspaceService workspace,
                                  DepartmentAdminService departmentService) {
        this.registrationService = registrationService;
        this.workspace = workspace;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String view(@RequestParam(value = "roundId", required = false) Long roundId,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "departmentId", required = false) Long departmentId,
                       Principal principal, Model model) {
        List<RoundOption> options = workspace.roundOptions(principal.getName());
        Long selected = roundId != null ? roundId : workspace.defaultRoundId(options);
        model.addAttribute("options", options);
        model.addAttribute("roundId", selected);
        model.addAttribute("departments", departmentService.list());
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("departmentId", departmentId);
        if (selected != null) {
            model.addAttribute("pageData", registrationService.page(principal.getName(), selected, q, departmentId));
        }
        return "student/topics";
    }

    @PostMapping("/{topicId}/register")
    public String register(@PathVariable Long topicId, @RequestParam("roundId") Long roundId,
                           Principal principal, RedirectAttributes redirect) {
        registrationService.register(principal.getName(), roundId, topicId);
        redirect.addFlashAttribute("flashSuccess", "Nhóm đã đăng ký đề tài thành công!");
        return "redirect:/student/topics?roundId=" + roundId;
    }

    @PostMapping("/cancel/{groupId}")
    public String cancel(@PathVariable Long groupId, @RequestParam("roundId") Long roundId,
                         Principal principal, RedirectAttributes redirect) {
        registrationService.cancel(principal.getName(), groupId);
        redirect.addFlashAttribute("flashSuccess", "Đã hủy đăng ký đề tài. Nhóm có thể chọn đề tài khác.");
        return "redirect:/student/topics?roundId=" + roundId;
    }
}
