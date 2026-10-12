package com.example.quanlydetai.service;

import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Submission;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final GroupMemberRepository memberRepository;
    private final StudentGroupRepository groupRepository;

    public SubmissionService(SubmissionRepository submissionRepository, GroupMemberRepository memberRepository, StudentGroupRepository groupRepository) {
        this.submissionRepository = submissionRepository;
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
    }

    public Submission submitReport(Long groupId, Long studentId, String filePath, String sourceCodeUrl) {
        // Kiểm tra quyền nộp bài (BR-03)
        GroupMember member = memberRepository.findByGroupIdAndStudentId(groupId, studentId)
                .orElseThrow(() -> new RuntimeException("Sinh viên không thuộc nhóm"));

        if (!member.getIsLeader()) {
            throw new RuntimeException("Quyền nộp báo cáo chỉ thuộc về Nhóm trưởng (BR-03)");
        }

        StudentGroup group = groupRepository.findById(groupId).get();

        Submission submission = new Submission();
        submission.setGroup(group);
        submission.setSubmittedBy(member.getStudent());
        submission.setFilePath(filePath);
        submission.setSourceCodeUrl(sourceCodeUrl);

        return submissionRepository.save(submission);
    }
}