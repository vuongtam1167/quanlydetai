package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {
    Optional<ReviewAssignment> findByTopicId(Long topicId);
}