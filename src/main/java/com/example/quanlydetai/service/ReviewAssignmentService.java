package com.example.quanlydetai.service;

import com.example.quanlydetai.model.*;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReviewAssignmentService {

    private final ReviewAssignmentRepository reviewRepository;
    private final TopicAdvisorRepository advisorRepository;
    private final TopicRepository topicRepository;

    public ReviewAssignmentService(ReviewAssignmentRepository reviewRepository, TopicAdvisorRepository advisorRepository, TopicRepository topicRepository) {
        this.reviewRepository = reviewRepository;
        this.advisorRepository = advisorRepository;
        this.topicRepository = topicRepository;
    }

    public ReviewAssignment assignReviewer(Long topicId, Lecturer reviewer) {
        // Kiểm tra BR-05: GVPB không được là GVHD của chính đề tài đó
        List<TopicAdvisor> advisors = advisorRepository.findByTopicId(topicId);
        boolean isAdvisor = advisors.stream()
                .anyMatch(a -> a.getLecturer().getId().equals(reviewer.getId()));
        
        if (isAdvisor) {
            throw new RuntimeException("Giảng viên phản biện không được chấm đề tài do mình hướng dẫn (BR-05)");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề tài"));

        ReviewAssignment assignment = new ReviewAssignment();
        assignment.setTopic(topic);
        assignment.setReviewer(reviewer);
        return reviewRepository.save(assignment);
    }

    public ReviewAssignment submitReviewScore(Long assignmentId, BigDecimal score, String comments) {
        ReviewAssignment assignment = reviewRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân công phản biện"));
        
        assignment.setScore(score);
        assignment.setComments(comments);
        assignment.setSubmissionDate(LocalDateTime.now());
        
        return reviewRepository.save(assignment);
    }
}