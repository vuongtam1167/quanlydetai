package com.example.quanlydetai.service;

import com.example.quanlydetai.model.enums.GroupStatus;
import com.example.quanlydetai.model.*;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final TopicRepository topicRepository;

    public StudentGroupService(StudentGroupRepository groupRepository, GroupMemberRepository memberRepository, TopicRepository topicRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.topicRepository = topicRepository;
    }

    // Nghiệp vụ: Thêm thành viên vào nhóm (Kiểm tra BR-02)
    public GroupMember addMemberToGroup(Long groupId, Student student, boolean isLeader) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));

        // Kiểm tra sinh viên đã tham gia nhóm nào trong cùng đợt này chưa
        if (memberRepository.existsByStudentIdAndGroupRoundId(student.getId(), group.getRound().getId())) {
            throw new RuntimeException("Sinh viên đã tham gia một nhóm khác trong đợt đăng ký này!");
        }
        
        List<GroupMember> currentMembers = memberRepository.findByGroupId(groupId);
        
        // Ràng buộc số lượng (VD: max 5) (BR-02)
        if (currentMembers.size() >= 5) {
            throw new RuntimeException("Nhóm đã đạt số lượng tối đa (5 sinh viên)");
        }
        
        // Kiểm tra nhóm trưởng duy nhất (BR-02)
        if (isLeader && currentMembers.stream().anyMatch(GroupMember::getIsLeader)) {
            throw new RuntimeException("Nhóm này đã có nhóm trưởng");
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroup(group);
        newMember.setStudent(student);
        newMember.setIsLeader(isLeader);
        
        return memberRepository.save(newMember);
    }

    // Nghiệp vụ: Đăng ký đề tài cho nhóm (BR-03)
    public StudentGroup registerTopic(Long groupId, Long topicId, Long requestStudentId) {
        GroupMember requester = memberRepository.findByGroupIdAndStudentId(groupId, requestStudentId)
                .orElseThrow(() -> new RuntimeException("Sinh viên không thuộc nhóm này"));

        if (!requester.getIsLeader()) {
            throw new RuntimeException("Chỉ Nhóm trưởng mới có quyền đăng ký đề tài (BR-03)");
        }

        StudentGroup group = groupRepository.findById(groupId).get();
        if (group.getTopic() != null) {
            throw new RuntimeException("Nhóm chỉ được đăng ký duy nhất 1 đề tài (BR-03)");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề tài"));

        group.setTopic(topic);
        group.setStatus(GroupStatus.REGISTERED);
        return groupRepository.save(group);
    }
}