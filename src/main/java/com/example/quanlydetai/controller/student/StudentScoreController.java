package com.example.quanlydetai.controller.student;

import com.example.quanlydetai.dto.RoundOption;
import com.example.quanlydetai.service.GroupWorkspaceService;
import com.example.quanlydetai.service.StudentScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/** Tra cứu điểm: chỉ đợt mình đã đăng ký và chỉ khi Khoa đã công bố. */
@Controller
@RequestMapping("/student/scores")
@PreAuthorize("hasRole('SINH_VIEN')")
public class StudentScoreController {

    private final StudentScoreService scoreService;
    private final GroupWorkspaceService workspace;

    public StudentScoreController(StudentScoreService scoreService, GroupWorkspaceService workspace) {
        this.scoreService = scoreService;
        this.workspace = workspace;
    }

    @GetMapping
    public String view(@RequestParam(value = "roundId", required = false) Long roundId,
                       Principal principal, Model model) {
        List<RoundOption> options = workspace.myRounds(principal.getName());
        Long selected = roundId != null ? roundId : workspace.defaultRoundId(options);
        model.addAttribute("options", options);
        model.addAttribute("roundId", selected);
        if (selected != null) {
            model.addAttribute("score", scoreService.view(principal.getName(), selected));
        }
        return "student/scores";
    }
}
