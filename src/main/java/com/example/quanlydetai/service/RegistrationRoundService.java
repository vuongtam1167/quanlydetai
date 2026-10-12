package com.example.quanlydetai.service;

import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegistrationRoundService {

    private final RegistrationRoundRepository roundRepository;

    public RegistrationRoundService(RegistrationRoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public List<RegistrationRound> findAll() {
        return roundRepository.findAll();
    }

    public RegistrationRound findById(Long id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt đăng ký: " + id));
    }

    public RegistrationRound createRound(RegistrationRound round) {
        if (round.getLecturerEndDate().isBefore(round.getLecturerStartDate())) {
            throw new RuntimeException("Ngày kết thúc GĐ1 phải sau ngày bắt đầu");
        }
        if (round.getStudentEndDate().isBefore(round.getStudentStartDate())) {
            throw new RuntimeException("Ngày kết thúc GĐ2 phải sau ngày bắt đầu");
        }
        return roundRepository.save(round);
    }
    
    public RegistrationRound publishResults(Long roundId) {
        RegistrationRound round = findById(roundId);
        round.setIsPublished(true);
        return roundRepository.save(round);
    }
}