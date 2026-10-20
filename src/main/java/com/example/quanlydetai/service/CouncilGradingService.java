package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.CouncilGroupTask;
import com.example.quanlydetai.dto.CouncilTask;
import com.example.quanlydetai.dto.MemberScore;
import com.example.quanlydetai.dto.MemberView;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Council;
import com.example.quanlydetai.model.CouncilEvaluation;
import com.example.quanlydetai.model.CouncilMember;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Submission;
import com.example.quanlydetai.model.enums.CouncilRole;
import com.example.quanlydetai.model.enums.CouncilStatus;
import com.example.quanlydetai.repository.CouncilEvaluationRepository;
import com.example.quanlydetai.repository.CouncilMemberRepository;
import com.example.quanlydetai.repository.CouncilRepository;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.SubmissionRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chấm điểm hội đồng. Mỗi thành viên chấm điểm cho từng nhóm (trừ nhóm mình hướng dẫn - BR-05).
 * Chủ tịch xem điểm của mọi thành viên, điểm trung bình, nhập nhận xét tổng hợp của hội đồng
 * (lưu ở phiếu của chủ tịch) và bấm "Hoàn tất hội đồng" khi đủ điểm.
 */
@Service
@Transactional
public class CouncilGradingService {

    private final CurrentLecturerService currentLecturer;
    private final CouncilMemberRepository memberRepository;
    private final CouncilRepository councilRepository;
    private final CouncilEvaluationRepository evaluationRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SubmissionRepository submissionRepository;
    private final TopicAdvisorRepository advisorRepository;

    public CouncilGradingService(CurrentLecturerService currentLecturer, CouncilMemberRepository memberRepository,
                                 CouncilRepository councilRepository,
                                 CouncilEvaluationRepository evaluationRepository,
                                 StudentGroupRepository groupRepository,
                                 GroupMemberRepository groupMemberRepository,
                                 SubmissionRepository submissionRepository,
                                 TopicAdvisorRepository advisorRepository) {
        this.currentLecturer = currentLecturer;
        this.memberRepository = memberRepository;
        this.councilRepository = councilRepository;
        this.evaluationRepository = evaluationRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.submissionRepository = submissionRepository;
        this.advisorRepository = advisorRepository;
    }

    @Transactional(readOnly = true)
    public List<CouncilTask> tasks(String username) {
        Lecturer me = currentLecturer.require(username);
        List<CouncilTask> result = new ArrayList<>();
        for (CouncilMember cm : memberRepository.findByLecturerId(me.getId())) {
            Council c = cm.getCouncil();
            List<CouncilMember> members = memberRepository.findByCouncilId(c.getId());
            boolean chair = cm.getRoleInCouncil() == CouncilRole.CHAIR;

            List<CouncilGroupTask> groups = new ArrayList<>();
            boolean allDone = true;
            for (StudentGroup g : groupRepository.findByCouncilId(c.getId())) {
                if (g.getTopic() == null) {
                    continue;
                }
                CouncilGroupTask task = buildGroupTask(g, cm, members, me);
                if (!task.allScored()) {
                    allDone = false;
                }
                groups.add(task);
            }
            groups.sort(Comparator.comparing(CouncilGroupTask::groupName));
            result.add(new CouncilTask(c.getId(), c.getName(), c.getRound().getName(), c.getDefenseDate(),
                    c.getLocation(), c.getStatus(), chair, roleLabel(cm.getRoleInCouncil()),
                    chair && !groups.isEmpty() && allDone && c.getStatus() != CouncilStatus.COMPLETED,
                    groups));
        }
        result.sort(Comparator.comparing(CouncilTask::defenseDate).reversed());
        return result;
    }

    public void saveScore(Long groupId, BigDecimal score, String comments, String username) {
        Lecturer me = currentLecturer.require(username);
        StudentGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm."));
        Council c = g.getCouncil();
        if (c == null) {
            throw new BusinessException("Nhóm này chưa được phân vào hội đồng nào.");
        }
        CouncilMember cm = memberRepository.findByCouncilId(c.getId()).stream()
                .filter(m -> m.getLecturer().getId().equals(me.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Bạn không thuộc hội đồng của nhóm này."));
        if (c.getStatus() == CouncilStatus.COMPLETED) {
            throw new BusinessException("Hội đồng đã hoàn tất nên không thể sửa điểm.");
        }
        if (Boolean.TRUE.equals(g.getRound().getIsPublished())) {
            throw new BusinessException("Kết quả đợt này đã được công bố nên không thể sửa điểm.");
        }
        if (isAdvisor(g, me)) {
            throw new BusinessException("Bạn là GV hướng dẫn của đề tài này nên không được chấm điểm (BR-05).");
        }
        BigDecimal valid = Scores.validate(score);

        CouncilEvaluation eval = evaluationRepository.findByGroupId(groupId).stream()
                .filter(e -> e.getCouncilMember().getId().equals(cm.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CouncilEvaluation n = new CouncilEvaluation();
                    n.setCouncil(c);
                    n.setGroup(g);
                    n.setCouncilMember(cm);
                    return n;
                });
        eval.setScore(valid);
        eval.setComments(comments == null || comments.isBlank() ? null : comments.trim());
        evaluationRepository.save(eval);

        if (c.getStatus() == CouncilStatus.PLANNED) {
            c.setStatus(CouncilStatus.IN_SESSION);
            councilRepository.save(c);
        }
    }

    /** Chủ tịch hoàn tất hội đồng: khi mọi nhóm đã có đủ điểm của mọi thành viên. */
    public void complete(Long councilId, String username) {
        Lecturer me = currentLecturer.require(username);
        Council c = councilRepository.findById(councilId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng."));
        List<CouncilMember> members = memberRepository.findByCouncilId(councilId);
        boolean isChair = members.stream().anyMatch(m -> m.getLecturer().getId().equals(me.getId())
                && m.getRoleInCouncil() == CouncilRole.CHAIR);
        if (!isChair) {
            throw new BusinessException("Chỉ Chủ tịch hội đồng mới được hoàn tất hội đồng.");
        }
        if (c.getStatus() == CouncilStatus.COMPLETED) {
            throw new BusinessException("Hội đồng này đã được hoàn tất.");
        }
        List<StudentGroup> groups = groupRepository.findByCouncilId(councilId);
        if (groups.isEmpty()) {
            throw new BusinessException("Hội đồng chưa có nhóm nào để chấm.");
        }
        for (StudentGroup g : groups) {
            int scored = evaluationRepository.findByGroupId(g.getId()).size();
            if (scored < members.size()) {
                throw new BusinessException(g.getGroupName() + " mới có " + scored + "/" + members.size()
                        + " phiếu điểm. Cần đủ điểm của tất cả thành viên trước khi hoàn tất.");
            }
        }
        c.setStatus(CouncilStatus.COMPLETED);
        councilRepository.save(c);
    }

    private CouncilGroupTask buildGroupTask(StudentGroup g, CouncilMember myMembership,
                                            List<CouncilMember> members, Lecturer me) {
        List<CouncilEvaluation> evals = evaluationRepository.findByGroupId(g.getId());

        List<MemberView> students = new ArrayList<>();
        for (GroupMember m : groupMemberRepository.findByGroupId(g.getId())) {
            students.add(new MemberView(m.getStudent().getStudentCode(), m.getStudent().getFullName(),
                    Boolean.TRUE.equals(m.getIsLeader())));
        }
        Optional<Submission> sub = submissionRepository.findFirstByGroupIdOrderByIdDesc(g.getId());

        BigDecimal myScore = null;
        String myComments = null;
        List<MemberScore> scores = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (CouncilMember m : members) {
            CouncilEvaluation e = null;
            for (CouncilEvaluation x : evals) {
                if (x.getCouncilMember().getId().equals(m.getId())) {
                    e = x;
                    break;
                }
            }
            scores.add(new MemberScore(m.getLecturer().getFullName(), roleLabel(m.getRoleInCouncil()),
                    e == null ? null : e.getScore(), e == null ? null : e.getComments()));
            if (e != null) {
                sum = sum.add(e.getScore());
                count++;
                if (m.getId().equals(myMembership.getId())) {
                    myScore = e.getScore();
                    myComments = e.getComments();
                }
            }
        }
        BigDecimal avg = count == 0 ? null : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return new CouncilGroupTask(g.getId(), g.getGroupName(), g.getTopic().getCode(), g.getTopic().getTitle(),
                students, sub.isPresent(), sub.map(Submission::getFilePath).orElse(null),
                sub.map(Submission::getSourceCodeUrl).orElse(null), isAdvisor(g, me), myScore, myComments,
                scores, avg, count, members.size());
    }

    private boolean isAdvisor(StudentGroup g, Lecturer me) {
        return advisorRepository.findByTopicId(g.getTopic().getId()).stream()
                .anyMatch(a -> a.getLecturer().getId().equals(me.getId()));
    }

    private String roleLabel(CouncilRole role) {
        return switch (role) {
            case CHAIR -> "Chủ tịch";
            case SECRETARY -> "Thư ký";
            case MEMBER -> "Ủy viên";
        };
    }
}
