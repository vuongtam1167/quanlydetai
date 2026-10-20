package com.example.quanlydetai.service;

import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.repository.LecturerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lấy hồ sơ giảng viên của người đang đăng nhập (qua username Spring Security). */
@Service
@Transactional(readOnly = true)
public class CurrentLecturerService {

    private final LecturerRepository lecturerRepository;

    public CurrentLecturerService(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    public Lecturer require(String username) {
        return lecturerRepository.findByUserUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "Tài khoản của bạn chưa có hồ sơ giảng viên. Vui lòng liên hệ quản trị viên."));
    }
}
