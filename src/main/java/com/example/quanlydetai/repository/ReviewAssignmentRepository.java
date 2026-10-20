package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {
    Optional<ReviewAssignment> findByTopicId(Long topicId);
    long countByReviewerId(Long reviewerId);
    boolean existsByReviewerId(Long reviewerId);
    List<ReviewAssignment> findByReviewerId(Long reviewerId);
}
