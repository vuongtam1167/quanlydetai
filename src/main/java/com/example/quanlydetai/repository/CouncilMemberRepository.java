package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.CouncilMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {
    List<CouncilMember> findByCouncilId(Long councilId);
    boolean existsByLecturerId(Long lecturerId);
    void deleteByCouncilId(Long councilId);
    List<CouncilMember> findByLecturerId(Long lecturerId);
}
