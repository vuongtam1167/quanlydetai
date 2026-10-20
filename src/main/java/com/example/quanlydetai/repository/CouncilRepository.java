package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.Council;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilRepository extends JpaRepository<Council, Long> {
    List<Council> findByRoundId(Long roundId);
    List<Council> findAllByOrderByDefenseDateDesc();
    boolean existsByRoundId(Long roundId);
}
