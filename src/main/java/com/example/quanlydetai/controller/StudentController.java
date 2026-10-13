package com.example.quanlydetai.controller;

import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentRepository;
import com.example.quanlydetai.service.SubmissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sinhvien")
@PreAuthorize("hasRole('SINH_VIEN')") // Bảo mật cấp độ class
public class StudentController {

    private final StudentRepository studentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SubmissionService submissionService;

    // Constructor Injection
    public StudentController(StudentRepository studentRepository, 
                             GroupMemberRepository groupMemberRepository,
                             SubmissionService submissionService) {
        this.studentRepository = studentRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.submissionService = submissionService;
    }

    private Student getCurrentStudent(Principal principal) {
        return studentRepository.findByUserUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Lỗi: Tài khoản chưa liên kết hồ sơ Sinh viên"));
    }

    @GetMapping("/nhom")
    public String viewMyGroup(Model model, Principal principal) {
        Student student = getCurrentStudent(principal);
        
        // Lấy danh sách nhóm của sinh viên hiện tại
        List<GroupMember> memberships = groupMemberRepository.findByStudentId(student.getId());
        if (memberships.isEmpty()) {
            model.addAttribute("message", "Bạn chưa tham gia nhóm nào trong đợt này.");
            return "sinhvien/nhom-empty";
        }

        // Giả định lấy nhóm mới nhất (có thể mở rộng chọn đợt)
        GroupMember currentMembership = memberships.get(memberships.size() - 1);
        StudentGroup group = currentMembership.getGroup();
        
        model.addAttribute("group", group);
        model.addAttribute("members", groupMemberRepository.findByGroupId(group.getId()));
        model.addAttribute("isLeader", currentMembership.getIsLeader());
        
        return "sinhvien/nhom";
    }

    @PostMapping("/nhom/{groupId}/submit")
    public String submitReport(@PathVariable Long groupId, 
                               @RequestParam("reportFile") MultipartFile file,
                               @RequestParam(value = "sourceCodeUrl", required = false) String sourceCodeUrl,
                               Principal principal) {
        Student student = getCurrentStudent(principal);
        
        // Giả lập lưu file storage và lấy đường dẫn
        String filePath = "/uploads/" + file.getOriginalFilename(); 
        
        // Gọi Service xử lý nộp bài (Service đã kiểm tra quyền Nhóm trưởng)
        submissionService.submitReport(groupId, student.getId(), filePath, sourceCodeUrl);
        
        return "redirect:/sinhvien/nhom?success";
    }

    @GetMapping("/diem")
    public String viewScores(Model model, Principal principal) {
        Student student = getCurrentStudent(principal);
        List<GroupMember> memberships = groupMemberRepository.findByStudentId(student.getId());
        
        List<StudentGroup> validGroups = new ArrayList<>();
        for (GroupMember member : memberships) {
            StudentGroup group = member.getGroup();
            if (group.getRound() != null && group.getRound().getIsPublished()) {
                validGroups.add(group);
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("groups", validGroups);
        return "sinhvien/diem"; 
    }
}