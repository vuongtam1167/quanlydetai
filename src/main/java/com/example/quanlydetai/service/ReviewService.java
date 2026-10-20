package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.MemberView;
import com.example.quanlydetai.dto.ReviewTaskRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.ReviewAssignment;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Submission;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.SubmissionRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Giảng viên phản biện xem báo cáo của nhóm được phân công và nhập điểm + nhận xét. */
@Service
@Transactional
public class ReviewService {

    private final CurrentLecturerService currentLecturer;
    private final ReviewAssignmentRepository reviewRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final SubmissionRepository submissionRepository;
    private final TopicAdvisorRepository advisorRepository;

    public ReviewService(CurrentLecturerService currentLecturer, ReviewAssignmentRepository reviewRepository,
                         StudentGroupRepository groupRepository, GroupMemberRepository memberRepository,
                         SubmissionRepository submissionRepository, TopicAdvisorRepository advisorRepository) {
        this.currentLecturer = currentLecturer;
        this.reviewRepository = reviewRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.submissionRepository = submissionRepository;
        this.advisorRepository = advisorRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewTaskRow> tasks(String username) {
        Lecturer me = currentLecturer.require(username);
        List<ReviewAssignment> list = new ArrayList<>(reviewRepository.findByReviewerId(me.getId()));
        list.sort(Comparator.comparing((ReviewAssignment r) -> r.getTopic().getId()).reversed());

        LocalDateTime now = LocalDateTime.now();
        List<ReviewTaskRow> rows = new ArrayList<>();
        for (ReviewAssignment ra : list) {
            Topic t = ra.getTopic();
            RegistrationRound round = t.getRound();
            StudentGroup g = groupRepository.findByTopicId(t.getId()).orElse(null);
            List<MemberView> members = new ArrayList<>();
            Optional<Submission> sub = Optional.empty();
            if (g != null) {
                for (GroupMember m : memberRepository.findByGroupId(g.getId())) {
                    members.add(new MemberView(m.getStudent().getStudentCode(), m.getStudent().getFullName(),
                            Boolean.TRUE.equals(m.getIsLeader())));
                }
                sub = submissionRepository.findFirstByGroupIdOrderByIdDesc(g.getId());
            }
            String advisors = advisorRepository.findByTopicId(t.getId()).stream()
                    .map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
            boolean deadlineClosed = round.getReviewerDeadline() != null
                    && now.isAfter(round.getReviewerDeadline());
            boolean overdue = deadlineClosed && ra.getScore() == null;
            rows.add(new ReviewTaskRow(t.getId(), t.getCode(), t.getTitle(),
                    g == null ? null : g.getGroupName(), round.getName(), advisors, members,
                    sub.isPresent(), sub.map(Submission::getFilePath).orElse(null),
                    sub.map(Submission::getSourceCodeUrl).orElse(null),
                    ra.getScore(), ra.getComments(), round.getReviewerDeadline(), overdue,
                    Boolean.TRUE.equals(round.getIsPublished()), deadlineClosed));
        }
        return rows;
    }

    public void saveScore(Long topicId, BigDecimal score, String comments, String username) {
        Lecturer me = currentLecturer.require(username);
        ReviewAssignment ra = reviewRepository.findByTopicId(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài này chưa được phân công phản biện."));
        if (!ra.getReviewer().getId().equals(me.getId())) {
            throw new BusinessException("Bạn không phải giảng viên phản biện của đề tài này.");
        }
        RegistrationRound round = ra.getTopic().getRound();
        if (Boolean.TRUE.equals(round.getIsPublished())) {
            throw new BusinessException("Kết quả đợt này đã được công bố nên không thể sửa điểm.");
        }
        if (round.getReviewerDeadline() != null
                && LocalDateTime.now().isAfter(round.getReviewerDeadline())) {
            throw new BusinessException("Đã quá hạn GVPB nộp điểm nên không thể lưu hoặc sửa điểm.");
        }
        BigDecimal valid = Scores.validate(score);
        if (comments == null || comments.isBlank()) {
            throw new BusinessException("Vui lòng nhập nhận xét chi tiết cho nhóm.");
        }
        ra.setScore(valid);
        ra.setComments(comments.trim());
        ra.setSubmissionDate(LocalDateTime.now());
        reviewRepository.save(ra);
    }
}
