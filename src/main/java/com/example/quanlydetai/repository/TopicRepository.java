package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByCode(String code);
    List<Topic> findByRoundId(Long roundId);
    List<Topic> findAllByOrderByIdDesc();
    List<Topic> findByStatusOrderByIdDesc(com.example.quanlydetai.model.enums.TopicStatus status);
    List<Topic> findByRoundIdOrderByIdDesc(Long roundId);
    List<Topic> findByRoundIdAndStatusOrderByIdDesc(Long roundId, com.example.quanlydetai.model.enums.TopicStatus status);
    long countByRoundId(Long roundId);
    long countByStatus(com.example.quanlydetai.model.enums.TopicStatus status);
    boolean existsByCreatedById(Long lecturerId);
    boolean existsByDepartmentId(Long departmentId);
    boolean existsByCode(String code);
    List<Topic> findByDepartmentIdOrderByIdDesc(Long departmentId);
    List<Topic> findByDepartmentIdAndStatusOrderByIdDesc(Long departmentId, com.example.quanlydetai.model.enums.TopicStatus status);
    long countByDepartmentIdAndStatus(Long departmentId, com.example.quanlydetai.model.enums.TopicStatus status);
    long countByDepartmentId(Long departmentId);
}
