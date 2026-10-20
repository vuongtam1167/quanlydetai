package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.StudentForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.User;
import com.example.quanlydetai.model.enums.Role;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentRepository;
import com.example.quanlydetai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** CRUD sinh viên kèm tài khoản đăng nhập (username mặc định = MSSV). */
@Service
@Transactional
public class StudentAdminService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentAdminService(StudentRepository studentRepository, UserRepository userRepository,
                               GroupMemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Student> search(String q) {
        String k = q == null ? "" : q.trim().toLowerCase();
        return studentRepository.findAllByOrderByStudentCodeAsc().stream()
                .filter(s -> k.isEmpty()
                        || s.getFullName().toLowerCase().contains(k)
                        || s.getStudentCode().toLowerCase().contains(k)
                        || s.getClassName().toLowerCase().contains(k))
                .toList();
    }

    @Transactional(readOnly = true)
    public Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên."));
    }

    @Transactional(readOnly = true)
    public StudentForm toForm(Long id) {
        return StudentForm.from(get(id));
    }

    public Student save(StudentForm f) {
        String code = f.getStudentCode().trim();
        String email = f.getEmail().trim();
        Optional<Student> sameCode = studentRepository.findByStudentCode(code);
        if (sameCode.isPresent() && !sameCode.get().getId().equals(f.getId())) {
            throw new BusinessException("MSSV \"" + code + "\" đã tồn tại.");
        }
        Optional<Student> sameEmail = studentRepository.findByEmail(email);
        if (sameEmail.isPresent() && !sameEmail.get().getId().equals(f.getId())) {
            throw new BusinessException("Email \"" + email + "\" đã được dùng cho sinh viên khác.");
        }
        Student s;
        if (f.getId() == null) {
            if (userRepository.existsByUsername(code)) {
                throw new BusinessException("Tên đăng nhập \"" + code + "\" đã tồn tại.");
            }
            User u = new User();
            u.setUsername(code);
            u.setPassword(passwordEncoder.encode(
                    f.getPassword() == null || f.getPassword().isBlank()
                            ? LecturerAdminService.DEFAULT_PASSWORD : f.getPassword()));
            u.setRole(Role.SINH_VIEN);
            u.setEnabled(true);
            u = userRepository.save(u);
            s = new Student();
            s.setUser(u);
        } else {
            s = get(f.getId());
            if (f.getPassword() != null && !f.getPassword().isBlank()) {
                User u = s.getUser();
                u.setPassword(passwordEncoder.encode(f.getPassword()));
                userRepository.save(u);
            }
        }
        s.setStudentCode(code);
        s.setFullName(f.getFullName().trim());
        s.setEmail(email);
        s.setPhone(f.getPhone());
        s.setClassName(f.getClassName().trim());
        return studentRepository.save(s);
    }

    public void delete(Long id) {
        Student s = get(id);
        if (memberRepository.existsByStudentId(id)) {
            throw new BusinessException("Sinh viên đã tham gia nhóm nên không thể xóa. Hãy khóa tài khoản thay vì xóa.");
        }
        User u = s.getUser();
        studentRepository.delete(s);
        userRepository.delete(u);
    }
}
