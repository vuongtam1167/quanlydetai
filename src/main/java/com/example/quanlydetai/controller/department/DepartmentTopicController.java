package com.example.quanlydetai.controller.department;

import com.example.quanlydetai.dto.Labels;
import com.example.quanlydetai.service.DepartmentTopicService;
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

/** Trưởng bộ môn thẩm định đề tài giảng viên trong bộ môn đề xuất. */
@Controller
@RequestMapping("/department/topics")
@PreAuthorize("hasRole('TRUONG_BO_MON')")
public class DepartmentTopicController {

    private final DepartmentTopicService departmentService;

    public DepartmentTopicController(DepartmentTopicService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(@RequestParam(value = "status", defaultValue = "PENDING") String status,
                       Principal principal, Model model) {
        String user = principal.getName();
        model.addAttribute("topics", departmentService.list(user, status));
        model.addAttribute("counts", departmentService.counts(user));
        model.addAttribute("departmentName", departmentService.departmentName(user));
        model.addAttribute("statusLabels", Labels.topicStatuses());
        model.addAttribute("status", status);
        return "department/topics";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        departmentService.approve(id, principal.getName());
        redirect.addFlashAttribute("flashSuccess", "Đã thông qua đề tài và chuyển lên Trưởng khoa phê duyệt.");
        return "redirect:/department/topics";
    }

    @PostMapping("/{id}/request-edit")
    public String requestEdit(@PathVariable Long id, @RequestParam("feedback") String feedback,
                              Principal principal, RedirectAttributes redirect) {
        departmentService.requestEdit(id, principal.getName(), feedback);
        redirect.addFlashAttribute("flashSuccess", "Đã gửi yêu cầu chỉnh sửa cho giảng viên.");
        return "redirect:/department/topics";
    }
}
