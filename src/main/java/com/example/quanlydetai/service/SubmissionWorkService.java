package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.SubmissionView;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Submission;
import com.example.quanlydetai.model.enums.GroupStatus;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.SubmissionRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Nộp báo cáo + mã nguồn (dưới dạng liên kết). Chỉ nhóm trưởng được nộp; nộp lại sẽ cập nhật bản cũ. */
@Service
@Transactional
public class SubmissionWorkService {

    private static final Pattern URL = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    private final CurrentStudentService currentStudent;
    private final GroupWorkspaceService workspace;
    private final SubmissionRepository submissionRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final TopicAdvisorRepository advisorRepository;

    public SubmissionWorkService(CurrentStudentService currentStudent, GroupWorkspaceService workspace,
                                 SubmissionRepository submissionRepository, StudentGroupRepository groupRepository,
                                 GroupMemberRepository memberRepository, TopicAdvisorRepository advisorRepository) {
        this.currentStudent = currentStudent;
        this.workspace = workspace;
        this.submissionRepository = submissionRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.advisorRepository = advisorRepository;
    }

    @Transactional(readOnly = true)
    public SubmissionView view(String username, Long roundId) {
        Student me = currentStudent.require(username);
        StudentGroup g = workspace.findMyGroup(me, roundId);
        if (g == null) {
            return null;
        }
        boolean leader = memberRepository.findByGroupIdAndStudentId(g.getId(), me.getId())
                .map(m -> Boolean.TRUE.equals(m.getIsLeader())).orElse(false);
        boolean published = Boolean.TRUE.equals(g.getRound().getIsPublished());
        if (g.getTopic() == null) {
            return new SubmissionView(g.getId(), g.getGroupName(), g.getRound().getName(), false, null, null,
                    null, leader, false, null, null, null, published);
        }
        String advisors = advisorRepository.findByTopicId(g.getTopic().getId()).stream()
                .map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
        Optional<Submission> sub = submissionRepository.findFirstByGroupIdOrderByIdDesc(g.getId());
        return new SubmissionView(g.getId(), g.getGroupName(), g.getRound().getName(), true,
                g.getTopic().getCode(), g.getTopic().getTitle(), advisors, leader, sub.isPresent(),
                sub.map(Submission::getFilePath).orElse(null),
                sub.map(Submission::getSourceCodeUrl).orElse(null),
                sub.map(Submission::getNotes).orElse(null), published);
    }

    public void submit(String username, Long groupId, String reportUrl, String sourceUrl, String notes) {
        Student me = currentStudent.require(username);
        StudentGroup g = workspace.requireLeaderGroup(me, groupId);
        if (g.getTopic() == null) {
            throw new BusinessException("Nhóm chưa đăng ký đề tài nên chưa thể nộp báo cáo.");
        }
        if (Boolean.TRUE.equals(g.getRound().getIsPublished())) {
            throw new BusinessException("Kết quả đợt này đã được công bố nên không thể nộp thêm.");
        }
        String report = reportUrl == null ? "" : reportUrl.trim();
        String source = sourceUrl == null ? "" : sourceUrl.trim();
        if (!URL.matcher(report).matches()) {
            throw new BusinessException("Liên kết báo cáo không hợp lệ (phải bắt đầu bằng http:// hoặc https://).");
        }
        if (!source.isEmpty() && !URL.matcher(source).matches()) {
            throw new BusinessException("Liên kết mã nguồn không hợp lệ (phải bắt đầu bằng http:// hoặc https://).");
        }
        Submission s = submissionRepository.findFirstByGroupIdOrderByIdDesc(groupId).orElseGet(() -> {
            Submission n = new Submission();
            n.setGroup(g);
            return n;
        });
        GroupMember leader = memberRepository.findByGroupIdAndStudentId(groupId, me.getId()).orElseThrow();
        s.setSubmittedBy(leader.getStudent());
        s.setFilePath(report);
        s.setSourceCodeUrl(source.isEmpty() ? null : source);
        s.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
        submissionRepository.save(s);

        g.setStatus(GroupStatus.SUBMITTED);
        groupRepository.save(g);
    }
}
