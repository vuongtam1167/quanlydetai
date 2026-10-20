package com.example.quanlydetai.service;

import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lấy hồ sơ sinh viên của người đang đăng nhập. */
@Service
@Transactional(readOnly = true)
public class CurrentStudentService {

    private final StudentRepository studentRepository;

    public CurrentStudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student require(String username) {
        return studentRepository.findByUserUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "Tài khoản của bạn chưa có hồ sơ sinh viên. Vui lòng liên hệ quản trị viên."));
    }
}
