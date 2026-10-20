package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.LecturerForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Department;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.User;
import com.example.quanlydetai.model.enums.Role;
import com.example.quanlydetai.repository.CouncilMemberRepository;
import com.example.quanlydetai.repository.DepartmentRepository;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import com.example.quanlydetai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** CRUD giảng viên kèm tài khoản đăng nhập (mật khẩu mã hóa BCrypt). */
@Service
@Transactional
public class LecturerAdminService {

    public static final String DEFAULT_PASSWORD = "123456";

    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository advisorRepository;
    private final ReviewAssignmentRepository reviewRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final PasswordEncoder passwordEncoder;

    public LecturerAdminService(LecturerRepository lecturerRepository, UserRepository userRepository,
                                DepartmentRepository departmentRepository, TopicRepository topicRepository,
                                TopicAdvisorRepository advisorRepository, ReviewAssignmentRepository reviewRepository,
                                CouncilMemberRepository councilMemberRepository, PasswordEncoder passwordEncoder) {
        this.lecturerRepository = lecturerRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.topicRepository = topicRepository;
        this.advisorRepository = advisorRepository;
        this.reviewRepository = reviewRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Lecturer> search(String q, Long departmentId) {
        String k = q == null ? "" : q.trim().toLowerCase();
        return lecturerRepository.findAllByOrderByFullNameAsc().stream()
                .filter(l -> departmentId == null || l.getDepartment().getId().equals(departmentId))
                .filter(l -> k.isEmpty()
                        || l.getFullName().toLowerCase().contains(k)
                        || l.getLecturerCode().toLowerCase().contains(k)
                        || l.getEmail().toLowerCase().contains(k))
                .toList();
    }

    @Transactional(readOnly = true)
    public Lecturer get(Long id) {
        return lecturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên."));
    }

    @Transactional(readOnly = true)
    public LecturerForm toForm(Long id) {
        return LecturerForm.from(get(id));
    }

    public Lecturer save(LecturerForm f) {
        if (f.getRole() != Role.GIANG_VIEN && f.getRole() != Role.TRUONG_BO_MON && f.getRole() != Role.TRUONG_KHOA) {
            throw new BusinessException("Vai trò của giảng viên chỉ có thể là Giảng viên, Trưởng bộ môn hoặc Trưởng khoa.");
        }
        String code = f.getLecturerCode().trim();
        String email = f.getEmail().trim();
        Optional<Lecturer> sameCode = lecturerRepository.findByLecturerCode(code);
        if (sameCode.isPresent() && !sameCode.get().getId().equals(f.getId())) {
            throw new BusinessException("Mã giảng viên \"" + code + "\" đã tồn tại.");
        }
        Optional<Lecturer> sameEmail = lecturerRepository.findByEmail(email);
        if (sameEmail.isPresent() && !sameEmail.get().getId().equals(f.getId())) {
            throw new BusinessException("Email \"" + email + "\" đã được dùng cho giảng viên khác.");
        }
        Department dept = departmentRepository.findById(f.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));

        Lecturer l;
        if (f.getId() == null) {
            String username = f.getUsername() == null || f.getUsername().isBlank()
                    ? code.toLowerCase() : f.getUsername().trim();
            if (userRepository.existsByUsername(username)) {
                throw new BusinessException("Tên đăng nhập \"" + username + "\" đã tồn tại.");
            }
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(blank(f.getPassword()) ? DEFAULT_PASSWORD : f.getPassword()));
            u.setRole(f.getRole());
            u.setEnabled(true);
            u = userRepository.save(u);
            l = new Lecturer();
            l.setUser(u);
        } else {
            l = get(f.getId());
            User u = l.getUser();
            u.setRole(f.getRole());
            if (!blank(f.getPassword())) {
                u.setPassword(passwordEncoder.encode(f.getPassword()));
            }
            userRepository.save(u);
        }
        l.setLecturerCode(code);
        l.setFullName(f.getFullName().trim());
        l.setEmail(email);
        l.setPhone(f.getPhone());
        l.setAcademicRank(f.getAcademicRank());
        l.setDepartment(dept);
        return lecturerRepository.save(l);
    }

    public void delete(Long id) {
        Lecturer l = get(id);
        if (topicRepository.existsByCreatedById(id) || advisorRepository.existsByLecturerId(id)
                || reviewRepository.existsByReviewerId(id) || councilMemberRepository.existsByLecturerId(id)) {
            throw new BusinessException("Giảng viên đã tham gia đề tài/phản biện/hội đồng nên không thể xóa. Hãy khóa tài khoản thay vì xóa.");
        }
        User u = l.getUser();
        lecturerRepository.delete(l);
        userRepository.delete(u);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
