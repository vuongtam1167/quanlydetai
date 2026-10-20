package com.example.quanlydetai.controller.student;

import com.example.quanlydetai.dto.RoundOption;
import com.example.quanlydetai.service.GroupWorkspaceService;
import com.example.quanlydetai.service.SubmissionWorkService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/** Nộp báo cáo và mã nguồn - chỉ nhóm trưởng được nộp. */
@Controller
@RequestMapping("/student/submission")
@PreAuthorize("hasRole('SINH_VIEN')")
public class StudentSubmissionController {

    private final SubmissionWorkService submissionService;
    private final GroupWorkspaceService workspace;

    public StudentSubmissionController(SubmissionWorkService submissionService, GroupWorkspaceService workspace) {
        this.submissionService = submissionService;
        this.workspace = workspace;
    }

    @GetMapping
    public String view(@RequestParam(value = "roundId", required = false) Long roundId,
                       Principal principal, Model model) {
        List<RoundOption> options = workspace.myRounds(principal.getName());
        Long selected = roundId != null ? roundId : workspace.defaultRoundId(options);
        model.addAttribute("options", options);
        model.addAttribute("roundId", selected);
        model.addAttribute("sub", submissionService.view(principal.getName(), selected));
        return "student/submission";
    }

    @PostMapping("/save")
    public String save(@RequestParam("groupId") Long groupId, @RequestParam("roundId") Long roundId,
                       @RequestParam("reportUrl") String reportUrl,
                       @RequestParam(value = "sourceUrl", required = false) String sourceUrl,
                       @RequestParam(value = "notes", required = false) String notes,
                       Principal principal, RedirectAttributes redirect) {
        submissionService.submit(principal.getName(), groupId, reportUrl, sourceUrl, notes);
        redirect.addFlashAttribute("flashSuccess", "Đã nộp báo cáo thành công. Giảng viên hướng dẫn và phản biện sẽ xem được.");
        return "redirect:/student/submission?roundId=" + roundId;
    }
}
