package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    Optional<Lecturer> findByLecturerCode(String lecturerCode);
    Optional<Lecturer> findByEmail(String email);
    // Lấy hồ sơ giảng viên từ username đăng nhập (Spring Security)
    Optional<Lecturer> findByUserUsername(String username);
    long countByDepartmentId(Long departmentId);
    List<Lecturer> findAllByOrderByFullNameAsc();
}
