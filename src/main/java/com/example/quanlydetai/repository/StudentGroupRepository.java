package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    List<StudentGroup> findByRoundId(Long roundId);
    Optional<StudentGroup> findByTopicId(Long topicId);
    List<StudentGroup> findByTopicIsNotNullOrderByRoundIdDescIdAsc();
    List<StudentGroup> findByCouncilId(Long councilId);
    long countByRoundId(Long roundId);
    long countByCouncilId(Long councilId);
    boolean existsByRoundIdAndGroupNameIgnoreCase(Long roundId, String groupName);
}
