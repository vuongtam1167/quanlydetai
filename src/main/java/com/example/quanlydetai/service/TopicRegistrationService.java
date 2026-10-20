package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.GroupView;
import com.example.quanlydetai.dto.RegistrationPage;
import com.example.quanlydetai.dto.TopicOption;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.enums.GroupStatus;
import com.example.quanlydetai.model.enums.TopicStatus;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.SubmissionRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Giai đoạn 2: nhóm sinh viên đăng ký 1 đề tài đã được Trưởng khoa phê duyệt trong danh sách của đợt.
 * Ràng buộc: chỉ nhóm trưởng đăng ký; mỗi nhóm 1 đề tài; mỗi đề tài chỉ 1 nhóm; nhóm đủ số thành viên;
 * chỉ trong thời gian sinh viên đăng ký của đợt.
 */
@Service
@Transactional
public class TopicRegistrationService {

    private final CurrentStudentService currentStudent;
    private final GroupWorkspaceService workspace;
    private final RegistrationRoundRepository roundRepository;
    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository advisorRepository;
    private final StudentGroupRepository groupRepository;
    private final SubmissionRepository submissionRepository;

    public TopicRegistrationService(CurrentStudentService currentStudent, GroupWorkspaceService workspace,
                                    RegistrationRoundRepository roundRepository, TopicRepository topicRepository,
                                    TopicAdvisorRepository advisorRepository,
                                    StudentGroupRepository groupRepository,
                                    SubmissionRepository submissionRepository) {
        this.currentStudent = currentStudent;
        this.workspace = workspace;
        this.roundRepository = roundRepository;
        this.topicRepository = topicRepository;
        this.advisorRepository = advisorRepository;
        this.groupRepository = groupRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public RegistrationPage page(String username, Long roundId, String q, Long departmentId) {
        Student me = currentStudent.require(username);
        RegistrationRound round = loadRound(roundId);
        LocalDateTime now = LocalDateTime.now();
        String state;
        boolean open = false;
        if (now.isBefore(round.getStudentStartDate())) {
            state = "Chưa đến thời gian đăng ký";
        } else if (now.isAfter(round.getStudentEndDate())) {
            state = "Đã kết thúc thời gian đăng ký";
        } else {
            state = "Đang mở đăng ký";
            open = true;
        }

        StudentGroup myGroup = workspace.findMyGroup(me, roundId);
        GroupView groupView = myGroup == null ? null : workspace.toView(myGroup, me);

        String keyword = q == null ? "" : q.trim().toLowerCase();
        List<TopicOption> options = new ArrayList<>();
        for (Topic t : topicRepository.findByRoundIdAndStatusOrderByIdDesc(roundId, TopicStatus.APPROVED)) {
            if (departmentId != null && !t.getDepartment().getId().equals(departmentId)) {
                continue;
            }
            String advisors = advisorRepository.findByTopicId(t.getId()).stream()
                    .map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
            if (!keyword.isEmpty() && !t.getTitle().toLowerCase().contains(keyword)
                    && !t.getCode().toLowerCase().contains(keyword)
                    && !advisors.toLowerCase().contains(keyword)) {
                continue;
            }
            Optional<StudentGroup> owner = groupRepository.findByTopicId(t.getId());
            boolean mine = owner.isPresent() && myGroup != null && owner.get().getId().equals(myGroup.getId());
            options.add(new TopicOption(t.getId(), t.getCode(), t.getTitle(), t.getDepartment().getName(),
                    advisors, t.getDescription(), t.getRequirements(), owner.isPresent(), mine));
        }
        return new RegistrationPage(round.getName(), round.getStudentStartDate(), round.getStudentEndDate(),
                state, open, groupView, options);
    }

    public void register(String username, Long roundId, Long topicId) {
        Student me = currentStudent.require(username);
        RegistrationRound round = loadRound(roundId);
        StudentGroup g = workspace.findMyGroup(me, roundId);
        if (g == null) {
            throw new BusinessException("Bạn chưa có nhóm trong đợt này. Hãy lập nhóm ở mục Không gian nhóm trước.");
        }
        g = workspace.requireLeaderGroup(me, g.getId());

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(round.getStudentStartDate()) || now.isAfter(round.getStudentEndDate())) {
            throw new BusinessException("Hiện không nằm trong thời gian sinh viên đăng ký đề tài của đợt này.");
        }
        if (g.getTopic() != null) {
            throw new BusinessException("Nhóm đã đăng ký đề tài. Muốn đổi, hãy hủy đăng ký hiện tại trước.");
        }
        GroupView view = workspace.toView(g, me);
        if (!view.enoughMembers()) {
            throw new BusinessException("Nhóm cần từ " + view.minMembers() + " đến " + view.maxMembers()
                    + " thành viên mới đăng ký được đề tài (hiện có " + view.size() + ").");
        }
        Topic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài."));
        if (!t.getRound().getId().equals(roundId)) {
            throw new BusinessException("Đề tài không thuộc đợt đăng ký này.");
        }
        if (t.getStatus() != TopicStatus.APPROVED) {
            throw new BusinessException("Đề tài chưa được Trưởng khoa phê duyệt.");
        }
        if (groupRepository.findByTopicId(topicId).isPresent()) {
            throw new BusinessException("Đề tài đã có nhóm khác chọn. Vui lòng chọn đề tài khác.");
        }
        g.setTopic(t);
        g.setStatus(GroupStatus.REGISTERED);
        try {
            groupRepository.saveAndFlush(g);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Đề tài vừa được nhóm khác đăng ký. Vui lòng chọn đề tài khác.");
        }
    }

    public void cancel(String username, Long groupId) {
        Student me = currentStudent.require(username);
        StudentGroup g = workspace.requireLeaderGroup(me, groupId);
        if (g.getTopic() == null) {
            throw new BusinessException("Nhóm chưa đăng ký đề tài nào.");
        }
        if (LocalDateTime.now().isAfter(g.getRound().getStudentEndDate())) {
            throw new BusinessException("Đã hết thời gian đăng ký nên không thể hủy đăng ký đề tài.");
        }
        if (submissionRepository.findFirstByGroupIdOrderByIdDesc(groupId).isPresent()) {
            throw new BusinessException("Nhóm đã nộp báo cáo nên không thể hủy đăng ký đề tài.");
        }
        g.setTopic(null);
        g.setStatus(GroupStatus.FORMING);
        groupRepository.save(g);
    }

    private RegistrationRound loadRound(Long roundId) {
        if (roundId == null) {
            throw new BusinessException("Vui lòng chọn đợt đăng ký.");
        }
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
    }
}
