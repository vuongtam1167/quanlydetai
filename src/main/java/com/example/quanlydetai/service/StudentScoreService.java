package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.ResultRow;
import com.example.quanlydetai.dto.ScoreView;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.model.CouncilEvaluation;
import com.example.quanlydetai.model.ReviewAssignment;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.enums.CouncilRole;
import com.example.quanlydetai.repository.CouncilEvaluationRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sinh viên tra cứu điểm. Chỉ xem được nhóm của chính mình trong đợt mình đã đăng ký,
 * và chỉ khi Khoa đã công bố kết quả của đợt đó.
 */
@Service
@Transactional(readOnly = true)
public class StudentScoreService {

    private final CurrentStudentService currentStudent;
    private final GroupWorkspaceService workspace;
    private final ResultService resultService;
    private final ReviewAssignmentRepository reviewRepository;
    private final CouncilEvaluationRepository evaluationRepository;

    public StudentScoreService(CurrentStudentService currentStudent, GroupWorkspaceService workspace,
                               ResultService resultService, ReviewAssignmentRepository reviewRepository,
                               CouncilEvaluationRepository evaluationRepository) {
        this.currentStudent = currentStudent;
        this.workspace = workspace;
        this.resultService = resultService;
        this.reviewRepository = reviewRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public ScoreView view(String username, Long roundId) {
        Student me = currentStudent.require(username);
        StudentGroup g = workspace.findMyGroup(me, roundId);
        if (g == null) {
            throw new BusinessException("Bạn không có nhóm nào trong đợt này nên không xem được điểm.");
        }
        boolean published = Boolean.TRUE.equals(g.getRound().getIsPublished());
        String code = g.getTopic() == null ? null : g.getTopic().getCode();
        String title = g.getTopic() == null ? null : g.getTopic().getTitle();
        if (!published || g.getTopic() == null) {
            return new ScoreView(g.getRound().getName(), g.getGroupName(), code, title, published,
                    null, null, null, null, null, false);
        }
        ResultRow row = resultService.rowOf(g);
        String reviewerComments = reviewRepository.findByTopicId(g.getTopic().getId())
                .map(ReviewAssignment::getComments).orElse(null);
        String councilComments = null;
        for (CouncilEvaluation e : evaluationRepository.findByGroupId(g.getId())) {
            if (e.getCouncilMember().getRoleInCouncil() == CouncilRole.CHAIR) {
                councilComments = e.getComments();
            }
        }
        return new ScoreView(g.getRound().getName(), g.getGroupName(), code, title, true,
                row.reviewerScore(), reviewerComments, row.councilScore(), councilComments,
                row.finalScore(), row.passed());
    }
}
