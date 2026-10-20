package com.example.quanlydetai.controller.student;

import com.example.quanlydetai.dto.RoundOption;
import com.example.quanlydetai.service.GroupWorkspaceService;
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

/** Không gian nhóm: lập nhóm, thêm/xóa thành viên, rời nhóm, giải tán nhóm. */
@Controller
@RequestMapping("/student/group")
@PreAuthorize("hasRole('SINH_VIEN')")
public class StudentGroupController {

    private final GroupWorkspaceService workspace;

    public StudentGroupController(GroupWorkspaceService workspace) {
        this.workspace = workspace;
    }

    @GetMapping
    public String view(@RequestParam(value = "roundId", required = false) Long roundId,
                       Principal principal, Model model) {
        List<RoundOption> options = workspace.roundOptions(principal.getName());
        Long selected = roundId != null ? roundId : workspace.defaultRoundId(options);
        model.addAttribute("options", options);
        model.addAttribute("roundId", selected);
        model.addAttribute("group", workspace.myGroup(principal.getName(), selected));
        model.addAttribute("minMembers", workspace.getMinMembers());
        model.addAttribute("maxMembers", workspace.getMaxMembers());
        return "student/group";
    }

    @PostMapping("/create")
    public String create(@RequestParam("roundId") Long roundId, @RequestParam("groupName") String groupName,
                         Principal principal, RedirectAttributes redirect) {
        workspace.create(principal.getName(), roundId, groupName);
        redirect.addFlashAttribute("flashSuccess", "Đã tạo nhóm. Bạn là nhóm trưởng, hãy thêm thành viên bằng MSSV.");
        return "redirect:/student/group?roundId=" + roundId;
    }

    @PostMapping("/{groupId}/members")
    public String addMember(@PathVariable Long groupId, @RequestParam("roundId") Long roundId,
                            @RequestParam("studentCode") String studentCode,
                            Principal principal, RedirectAttributes redirect) {
        workspace.addMember(principal.getName(), groupId, studentCode);
        redirect.addFlashAttribute("flashSuccess", "Đã thêm thành viên vào nhóm.");
        return "redirect:/student/group?roundId=" + roundId;
    }

    @PostMapping("/{groupId}/members/{memberId}/remove")
    public String removeMember(@PathVariable Long groupId, @PathVariable Long memberId,
                               @RequestParam("roundId") Long roundId,
                               Principal principal, RedirectAttributes redirect) {
        workspace.removeMember(principal.getName(), groupId, memberId);
        redirect.addFlashAttribute("flashSuccess", "Đã xóa thành viên khỏi nhóm.");
        return "redirect:/student/group?roundId=" + roundId;
    }

    @PostMapping("/{groupId}/leave")
    public String leave(@PathVariable Long groupId, @RequestParam("roundId") Long roundId,
                        Principal principal, RedirectAttributes redirect) {
        workspace.leave(principal.getName(), groupId);
        redirect.addFlashAttribute("flashSuccess", "Bạn đã rời nhóm.");
        return "redirect:/student/group?roundId=" + roundId;
    }

    @PostMapping("/{groupId}/disband")
    public String disband(@PathVariable Long groupId, @RequestParam("roundId") Long roundId,
                          Principal principal, RedirectAttributes redirect) {
        workspace.disband(principal.getName(), groupId);
        redirect.addFlashAttribute("flashSuccess", "Đã giải tán nhóm.");
        return "redirect:/student/group?roundId=" + roundId;
    }
}
