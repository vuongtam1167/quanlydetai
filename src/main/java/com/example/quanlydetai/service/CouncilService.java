package com.example.quanlydetai.service;

import com.example.quanlydetai.model.*;
import com.example.quanlydetai.model.enums.GroupStatus;
import com.example.quanlydetai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class CouncilService {

    private final CouncilEvaluationRepository evaluationRepository;
    private final StudentGroupRepository groupRepository;
    private final TopicAdvisorRepository advisorRepository;

    public CouncilService(CouncilEvaluationRepository evaluationRepository, StudentGroupRepository groupRepository, TopicAdvisorRepository advisorRepository) {
        this.evaluationRepository = evaluationRepository;
        this.groupRepository = groupRepository;
        this.advisorRepository = advisorRepository;
    }

    public CouncilEvaluation submitGrade(CouncilMember councilMember, StudentGroup group, BigDecimal score, String comments) {
        // Kiểm tra xung đột lợi ích: GVHD không được chấm đề tài mình hướng dẫn (BR-05)
        List<TopicAdvisor> advisors = advisorRepository.findByTopicId(group.getTopic().getId());
        boolean isAdvisor = advisors.stream()
                .anyMatch(a -> a.getLecturer().getId().equals(councilMember.getLecturer().getId()));
        
        if (isAdvisor) {
            throw new RuntimeException("Giảng viên hướng dẫn không được phép chấm điểm đề tài do chính mình hướng dẫn (BR-05)");
        }

        CouncilEvaluation evaluation = new CouncilEvaluation();
        evaluation.setCouncil(councilMember.getCouncil());
        evaluation.setCouncilMember(councilMember);
        evaluation.setGroup(group);
        evaluation.setScore(score);
        evaluation.setComments(comments);

        return evaluationRepository.save(evaluation);
    }

    // Nghiệp vụ: Tính điểm trung bình cộng của hội đồng (BR-06)
    public void calculateFinalScore(Long groupId) {
        List<CouncilEvaluation> evaluations = evaluationRepository.findByGroupId(groupId);
        if (evaluations.isEmpty()) return;

        BigDecimal totalScore = BigDecimal.ZERO;
        for (CouncilEvaluation eval : evaluations) {
            totalScore = totalScore.add(eval.getScore());
        }

        BigDecimal finalScore = totalScore.divide(new BigDecimal(evaluations.size()), 2, RoundingMode.HALF_UP);
        
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
        group.setFinalScore(finalScore);
        group.setStatus(GroupStatus.COMPLETED);
        
        groupRepository.save(group);
    }
}