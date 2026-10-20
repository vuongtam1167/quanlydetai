package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.PublishSummary;
import com.example.quanlydetai.dto.ResultRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.CouncilEvaluation;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.ReviewAssignment;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.enums.GroupStatus;
import com.example.quanlydetai.repository.CouncilEvaluationRepository;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Tổng kết điểm theo đợt: điểm cuối = trung bình cộng các điểm thành phần hiện có
 * (điểm GV phản biện và điểm trung bình hội đồng). Công bố -> sinh viên mới xem được + gửi email.
 */
@Service
@Transactional
public class ResultService {

    private final StudentGroupRepository groupRepository;
    private final ReviewAssignmentRepository reviewRepository;
    private final CouncilEvaluationRepository evaluationRepository;
    private final RegistrationRoundRepository roundRepository;
    private final GroupMemberRepository memberRepository;
    private final EmailService emailService;

    public ResultService(StudentGroupRepository groupRepository, ReviewAssignmentRepository reviewRepository,
                         CouncilEvaluationRepository evaluationRepository,
                         RegistrationRoundRepository roundRepository, GroupMemberRepository memberRepository,
                         EmailService emailService) {
        this.groupRepository = groupRepository;
        this.reviewRepository = reviewRepository;
        this.evaluationRepository = evaluationRepository;
        this.roundRepository = roundRepository;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<ResultRow> rows(Long roundId) {
        List<ResultRow> rows = new ArrayList<>();
        for (StudentGroup g : groupRepository.findByRoundId(roundId)) {
            if (g.getTopic() == null) {
                continue;
            }
            rows.add(buildRow(g));
        }
        return rows;
    }

    /** Tính lại và lưu điểm tổng kết cho mọi nhóm trong đợt. Trả về số nhóm đã có điểm. */
    public int recalculate(Long roundId) {
        int scored = 0;
        for (StudentGroup g : groupRepository.findByRoundId(roundId)) {
            if (g.getTopic() == null) {
                continue;
            }
            ResultRow row = buildRow(g);
            g.setFinalScore(row.finalScore());
            if (row.finalScore() != null) {
                g.setStatus(GroupStatus.COMPLETED);
                scored++;
            }
            groupRepository.save(g);
        }
        return scored;
    }

    public PublishSummary publish(Long roundId) {
        RegistrationRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
        int scored = recalculate(roundId);
        if (scored == 0) {
            throw new BusinessException("Chưa có nhóm nào có điểm tổng kết nên chưa thể công bố.");
        }
        long withTopic = groupRepository.findByRoundId(roundId).stream().filter(g -> g.getTopic() != null).count();
        round.setIsPublished(true);
        roundRepository.save(round);

        int sent = 0;
        int failed = 0;
        int consecutiveFailures = 0;
        for (StudentGroup g : groupRepository.findByRoundId(roundId)) {
            if (g.getTopic() == null || g.getFinalScore() == null) {
                continue;
            }
            ResultRow row = buildRow(g);
            for (GroupMember m : memberRepository.findByGroupId(g.getId())) {
                // Nếu SMTP chưa cấu hình (3 lần thất bại liên tiếp) thì bỏ qua phần còn lại để không treo
                if (consecutiveFailures >= 3) {
                    failed++;
                    continue;
                }
                try {
                    emailService.sendResultEmail(m.getStudent().getEmail(),
                            "Kết quả đề tài - " + round.getName(), body(round, m, row));
                    sent++;
                    consecutiveFailures = 0;
                } catch (Exception ex) {
                    failed++;
                    consecutiveFailures++;
                }
            }
        }
        return new PublishSummary(scored, (int) (withTopic - scored), sent, failed);
    }

    public void unpublish(Long roundId) {
        RegistrationRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
        round.setIsPublished(false);
        roundRepository.save(round);
    }

    @Transactional(readOnly = true)
    public boolean isPublished(Long roundId) {
        return roundRepository.findById(roundId).map(r -> Boolean.TRUE.equals(r.getIsPublished())).orElse(false);
    }

    /** Điểm thành phần + điểm tổng kết của một nhóm (nhóm phải đã đăng ký đề tài). */
    @Transactional(readOnly = true)
    public ResultRow rowOf(StudentGroup g) {
        return buildRow(g);
    }

    private ResultRow buildRow(StudentGroup g) {
        BigDecimal reviewer = reviewRepository.findByTopicId(g.getTopic().getId())
                .map(ReviewAssignment::getScore).orElse(null);

        List<CouncilEvaluation> evals = evaluationRepository.findByGroupId(g.getId());
        BigDecimal council = null;
        if (!evals.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (CouncilEvaluation e : evals) {
                sum = sum.add(e.getScore());
            }
            council = sum.divide(BigDecimal.valueOf(evals.size()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal finalScore = null;
        if (reviewer != null && council != null) {
            finalScore = reviewer.add(council).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else if (reviewer != null) {
            finalScore = reviewer.setScale(2, RoundingMode.HALF_UP);
        } else if (council != null) {
            finalScore = council;
        }
        return new ResultRow(g.getId(), g.getGroupName(), g.getTopic().getCode(), g.getTopic().getTitle(),
                reviewer, council, evals.size(), finalScore);
    }

    private String body(RegistrationRound round, GroupMember m, ResultRow row) {
        return String.format(
                "Chào %s,%n%nKết quả đề tài \"%s\" của nhóm bạn trong đợt \"%s\":%n"
                        + "- Điểm GV phản biện: %s%n- Điểm trung bình hội đồng: %s%n- Điểm tổng kết: %s (%s)%n%n"
                        + "Vui lòng đăng nhập hệ thống để xem chi tiết.%n%nTrân trọng,%nKhoa Công nghệ Thông tin.",
                m.getStudent().getFullName(), row.topicTitle(), round.getName(),
                row.reviewerScore() == null ? "chưa có" : row.reviewerScore().toPlainString(),
                row.councilScore() == null ? "chưa có" : row.councilScore().toPlainString(),
                row.finalScore().toPlainString(), row.passed() ? "ĐẠT" : "KHÔNG ĐẠT");
    }
}
