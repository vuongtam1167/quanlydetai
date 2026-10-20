package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    Optional<GroupMember> findByGroupIdAndStudentId(Long groupId, Long studentId);

    // Kiểm tra sinh viên đã có nhóm trong đợt này chưa
    boolean existsByStudentIdAndGroupRoundId(Long studentId, Long roundId);
    
    // Lấy danh sách các nhóm mà sinh viên tham gia để tra cứu điểm
    List<GroupMember> findByStudentId(Long studentId);
    boolean existsByStudentId(Long studentId);
    void deleteByGroupId(Long groupId);
    long countByGroupId(Long groupId);
}
