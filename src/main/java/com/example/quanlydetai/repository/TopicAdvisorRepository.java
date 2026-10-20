package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.TopicAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicAdvisorRepository extends JpaRepository<TopicAdvisor, Long> {
    List<TopicAdvisor> findByTopicId(Long topicId);
    boolean existsByLecturerId(Long lecturerId);
    List<TopicAdvisor> findByLecturerId(Long lecturerId);
    void deleteByTopicId(Long topicId);
}
