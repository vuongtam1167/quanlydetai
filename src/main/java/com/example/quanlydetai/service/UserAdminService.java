package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.UserRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.User;
import com.example.quanlydetai.model.enums.Role;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.StudentRepository;
import com.example.quanlydetai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Quản lý tài khoản: xem, khóa/mở khóa, đặt lại mật khẩu mặc định. */
@Service
@Transactional
public class UserAdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;

    public UserAdminService(UserRepository userRepository, StudentRepository studentRepository,
                            LecturerRepository lecturerRepository, PasswordEncoder passwordEncoder,
                            MenuService menuService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.passwordEncoder = passwordEncoder;
        this.menuService = menuService;
    }

    @Transactional(readOnly = true)
    public List<UserRow> search(String q, String role) {
        String k = q == null ? "" : q.trim().toLowerCase();
        List<UserRow> rows = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (role != null && !role.isBlank() && !u.getRole().name().equals(role)) {
                continue;
            }
            String name = fullNameOf(u);
            if (!k.isEmpty() && !u.getUsername().toLowerCase().contains(k) && !name.toLowerCase().contains(k)) {
                continue;
            }
            rows.add(new UserRow(u.getId(), u.getUsername(), u.getRole(), menuService.roleLabel(u.getRole()),
                    Boolean.TRUE.equals(u.getEnabled()), name));
        }
        return rows;
    }

    public void toggleEnabled(Long id, String currentUsername) {
        User u = get(id);
        if (u.getUsername().equals(currentUsername)) {
            throw new BusinessException("Không thể tự khóa tài khoản đang đăng nhập.");
        }
        u.setEnabled(!Boolean.TRUE.equals(u.getEnabled()));
        userRepository.save(u);
    }

    public void resetPassword(Long id) {
        User u = get(id);
        u.setPassword(passwordEncoder.encode(LecturerAdminService.DEFAULT_PASSWORD));
        userRepository.save(u);
    }

    private User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản."));
    }

    private String fullNameOf(User u) {
        if (u.getRole() == Role.SINH_VIEN) {
            return studentRepository.findByUserUsername(u.getUsername()).map(s -> s.getFullName()).orElse("—");
        }
        return lecturerRepository.findByUserUsername(u.getUsername()).map(l -> l.getFullName()).orElse("—");
    }
}
