package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;


@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    Optional<Student> findByEmail(String email);

    // Lấy thông tin Sinh viên từ username đăng nhập của Spring Security
    Optional<Student> findByUserUsername(String username);
    List<Student> findAllByOrderByStudentCodeAsc();
    boolean existsByStudentCode(String studentCode);
}
