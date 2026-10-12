package com.example.quanlydetai.service;

import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.repository.LecturerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LecturerService {

    private final LecturerRepository lecturerRepository;

    public LecturerService(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    public List<Lecturer> findAll() {
        return lecturerRepository.findAll();
    }

    public Lecturer findById(Long id) {
        return lecturerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));
    }

    public Lecturer save(Lecturer lecturer) {
        return lecturerRepository.save(lecturer);
    }
}