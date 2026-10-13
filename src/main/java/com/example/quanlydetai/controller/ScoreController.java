// package com.example.quanlydetai.controller;

// import com.example.quanlydetai.model.GroupMember;
// import com.example.quanlydetai.model.Student;
// import com.example.quanlydetai.model.StudentGroup;
// import com.example.quanlydetai.repository.GroupMemberRepository;
// import com.example.quanlydetai.repository.StudentRepository;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;

// import java.security.Principal;
// import java.util.ArrayList;
// import java.util.List;

// @Controller
// @RequestMapping("/sinhvien/diem")
// public class ScoreController {

//     private final StudentRepository studentRepository;
//     private final GroupMemberRepository groupMemberRepository;

//     public ScoreController(StudentRepository studentRepository, GroupMemberRepository groupMemberRepository) {
//         this.studentRepository = studentRepository;
//         this.groupMemberRepository = groupMemberRepository;
//     }

//     // Yêu cầu phân quyền: Chỉ role SINH_VIEN mới được truy cập
//     @GetMapping
//     @PreAuthorize("hasRole('SINH_VIEN')")
//     public String viewScores(Model model, Principal principal) {
//         // Lấy username từ Principal (người dùng đang đăng nhập)
//         String username = principal.getName();

//         // Map ra thực thể Student
//         Student student = studentRepository.findByUserUsername(username)
//                 .orElseThrow(() -> new RuntimeException("Tài khoản này chưa được liên kết với hồ sơ sinh viên"));

//         // Lấy tất cả các tư cách thành viên của Sinh viên này
//         List<GroupMember> memberships = groupMemberRepository.findByStudentId(student.getId());
        
//         // Trích xuất danh sách các StudentGroup tương ứng nhưng CHỈ với các đợt đã Publish kết quả
//         List<StudentGroup> validGroups = new ArrayList<>();
//         for (GroupMember member : memberships) {
//             StudentGroup group = member.getGroup();
//             if (group.getRound() != null && group.getRound().getIsPublished()) {
//                 validGroups.add(group);
//             }
//         }

//         // Đẩy dữ liệu ra view Thymeleaf (sẽ hiển thị ở file templates/sinhvien/diem.html)
//         model.addAttribute("student", student);
//         model.addAttribute("groups", validGroups);

//         return "sinhvien/diem"; 
//     }
// }