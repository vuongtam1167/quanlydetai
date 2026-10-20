package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.ReviewerRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.ReviewAssignment;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.TopicAdvisor;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Trưởng khoa phân công giảng viên phản biện (GVPB) cho các nhóm đã đăng ký đề tài. */
@Service
@Transactional
public class ReviewerAdminService {

    private final StudentGroupRepository groupRepository;
    private final TopicAdvisorRepository advisorRepository;
    private final ReviewAssignmentRepository reviewRepository;
    private final TopicRepository topicRepository;
    private final LecturerRepository lecturerRepository;

    public ReviewerAdminService(StudentGroupRepository groupRepository, TopicAdvisorRepository advisorRepository,
                                ReviewAssignmentRepository reviewRepository, TopicRepository topicRepository,
                                LecturerRepository lecturerRepository) {
        this.groupRepository = groupRepository;
        this.advisorRepository = advisorRepository;
        this.reviewRepository = reviewRepository;
        this.topicRepository = topicRepository;
        this.lecturerRepository = lecturerRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewerRow> rows(Long roundId) {
        List<ReviewerRow> rows = new ArrayList<>();
        for (StudentGroup g : groupRepository.findByTopicIsNotNullOrderByRoundIdDescIdAsc()) {
            if (roundId != null && !g.getRound().getId().equals(roundId)) {
                continue;
            }
            Topic t = g.getTopic();
            List<TopicAdvisor> advisors = advisorRepository.findByTopicId(t.getId());
            Set<Long> advisorIds = new HashSet<>();
            for (TopicAdvisor a : advisors) {
                advisorIds.add(a.getLecturer().getId());
            }
            String advisorNames = advisors.stream().map(a -> a.getLecturer().getFullName())
                    .collect(Collectors.joining(", "));
            ReviewAssignment ra = reviewRepository.findByTopicId(t.getId()).orElse(null);
            rows.add(new ReviewerRow(t.getId(), g.getGroupName(), t.getCode(), t.getTitle(),
                    g.getRound().getName(), advisorNames, advisorIds,
                    ra == null ? null : ra.getReviewer().getId(),
                    ra == null ? null : ra.getReviewer().getFullName(),
                    ra == null ? null : ra.getScore()));
        }
        return rows;
    }

    /** Số đề tài mỗi giảng viên đang phản biện (id GV -> số đề tài), giúp chia tải đều. */
    @Transactional(readOnly = true)
    public Map<Long, Long> workload() {
        return reviewRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getReviewer().getId(), Collectors.counting()));
    }

    public void assign(Long topicId, Long reviewerId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài."));
        Lecturer reviewer = lecturerRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên."));

        boolean isAdvisor = advisorRepository.findByTopicId(topicId).stream()
                .anyMatch(a -> a.getLecturer().getId().equals(reviewerId));
        if (isAdvisor) {
            throw new BusinessException("GV phản biện không được trùng với GV hướng dẫn của đề tài (BR-05).");
        }
        ReviewAssignment ra = reviewRepository.findByTopicId(topicId).orElse(null);
        if (ra != null && ra.getScore() != null) {
            throw new BusinessException("GV phản biện hiện tại đã chấm điểm nên không thể đổi người.");
        }
        if (ra == null) {
            ra = new ReviewAssignment();
            ra.setTopic(topic);
        }
        ra.setReviewer(reviewer);
        reviewRepository.save(ra);
    }
}
