package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.CouncilEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilEvaluationRepository extends JpaRepository<CouncilEvaluation, Long> {
    List<CouncilEvaluation> findByGroupId(Long groupId);
    List<CouncilEvaluation> findByCouncilId(Long councilId);
    boolean existsByCouncilId(Long councilId);
}
