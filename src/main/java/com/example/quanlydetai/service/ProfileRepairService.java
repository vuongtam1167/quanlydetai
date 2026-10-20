package com.example.quanlydetai.service;

import com.example.quanlydetai.model.Department;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.User;
import com.example.quanlydetai.model.enums.Role;
import com.example.quanlydetai.repository.DepartmentRepository;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Trưởng bộ môn phải có hồ sơ giảng viên (để biết thuộc bộ môn nào). Dữ liệu mẫu cũ chỉ có
 * tài khoản truongbomon_xxx mà chưa có hồ sơ -> tự tạo, bộ môn lấy theo hậu tố tên đăng nhập
 * (truongbomon_ktpm -> bộ môn KTPM). Chạy lặp lại nhiều lần vẫn an toàn.
 */
@Service
@Transactional
public class ProfileRepairService {

    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;

    public ProfileRepairService(UserRepository userRepository, LecturerRepository lecturerRepository,
                                DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.lecturerRepository = lecturerRepository;
        this.departmentRepository = departmentRepository;
    }

    public int repair() {
        int created = 0;
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            return 0;
        }
        for (User u : userRepository.findAll()) {
            if (u.getRole() != Role.TRUONG_BO_MON
                    || lecturerRepository.findByUserUsername(u.getUsername()).isPresent()) {
                continue;
            }
            String suffix = u.getUsername().contains("_")
                    ? u.getUsername().substring(u.getUsername().indexOf('_') + 1).toUpperCase() : "";
            Department dept = departmentRepository.findByCode(suffix).orElse(departments.get(0));

            String code = "TBM_" + (suffix.isEmpty() ? u.getUsername().toUpperCase() : suffix);
            String email = u.getUsername() + "@hcmute.edu.vn";
            if (lecturerRepository.findByLecturerCode(code).isPresent()) {
                code = code + "_" + u.getId();
            }
            if (lecturerRepository.findByEmail(email).isPresent()) {
                email = u.getUsername() + "." + u.getId() + "@hcmute.edu.vn";
            }
            Lecturer l = new Lecturer();
            l.setUser(u);
            l.setDepartment(dept);
            l.setLecturerCode(code);
            l.setFullName("Trưởng bộ môn " + dept.getName());
            l.setEmail(email);
            l.setAcademicRank("TS.");
            lecturerRepository.save(l);
            created++;
        }
        return created;
    }
}
