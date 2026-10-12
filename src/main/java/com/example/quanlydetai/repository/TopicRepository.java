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
}