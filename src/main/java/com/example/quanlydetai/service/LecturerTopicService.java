package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.MyTopicRow;
import com.example.quanlydetai.dto.TopicForm;
import com.example.quanlydetai.dto.TopicRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Department;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.TopicAdvisor;
import com.example.quanlydetai.model.enums.TopicStatus;
import com.example.quanlydetai.repository.DepartmentRepository;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Giảng viên đề xuất đề tài trong Giai đoạn 1 của đợt đăng ký.
 * Ràng buộc: 1-2 GVHD, chỉ đề xuất khi đợt đang trong thời gian GV đề xuất,
 * chỉ sửa/xóa khi đề tài còn chờ duyệt hoặc bị từ chối và chưa có nhóm đăng ký.
 */
@Service
@Transactional
public class LecturerTopicService {

    private final CurrentLecturerService currentLecturer;
    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository advisorRepository;
    private final StudentGroupRepository groupRepository;
    private final RegistrationRoundRepository roundRepository;
    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;

    public LecturerTopicService(CurrentLecturerService currentLecturer, TopicRepository topicRepository,
                                TopicAdvisorRepository advisorRepository, StudentGroupRepository groupRepository,
                                RegistrationRoundRepository roundRepository,
                                DepartmentRepository departmentRepository, LecturerRepository lecturerRepository) {
        this.currentLecturer = currentLecturer;
        this.topicRepository = topicRepository;
        this.advisorRepository = advisorRepository;
        this.groupRepository = groupRepository;
        this.roundRepository = roundRepository;
        this.departmentRepository = departmentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    @Transactional(readOnly = true)
    public Lecturer me(String username) {
        return currentLecturer.require(username);
    }

    @Transactional(readOnly = true)
    public List<MyTopicRow> myTopics(String username) {
        Lecturer me = currentLecturer.require(username);
        Map<Long, Topic> map = new LinkedHashMap<>();
        for (TopicAdvisor a : advisorRepository.findByLecturerId(me.getId())) {
            map.putIfAbsent(a.getTopic().getId(), a.getTopic());
        }
        List<Topic> topics = new ArrayList<>(map.values());
        topics.sort(Comparator.comparing(Topic::getId).reversed());

        List<MyTopicRow> rows = new ArrayList<>();
        for (Topic t : topics) {
            StudentGroup g = groupRepository.findByTopicId(t.getId()).orElse(null);
            rows.add(new MyTopicRow(TopicRow.of(t, advisorNames(t.getId())),
                    g == null ? null : g.getGroupName(), canModify(t, me, g)));
        }
        return rows;
    }

    /** Các đợt đang trong thời gian giảng viên đề xuất (Giai đoạn 1). */
    @Transactional(readOnly = true)
    public List<RegistrationRound> openRounds() {
        LocalDateTime now = LocalDateTime.now();
        return roundRepository.findAllByOrderByLecturerStartDateDesc().stream()
                .filter(r -> isProposalOpen(r, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Department> departments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Lecturer> otherLecturers(String username) {
        Lecturer me = currentLecturer.require(username);
        return lecturerRepository.findAllByOrderByFullNameAsc().stream()
                .filter(l -> !l.getId().equals(me.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TopicForm toForm(Long id, String username) {
        Lecturer me = currentLecturer.require(username);
        Topic t = get(id);
        StudentGroup g = groupRepository.findByTopicId(id).orElse(null);
        if (!canModify(t, me, g)) {
            throw new BusinessException("Đề tài này không còn được phép chỉnh sửa.");
        }
        Long advisor2 = null;
        for (TopicAdvisor a : advisorRepository.findByTopicId(id)) {
            if (!a.getLecturer().getId().equals(me.getId())) {
                advisor2 = a.getLecturer().getId();
            }
        }
        return TopicForm.from(t, advisor2);
    }

    public Topic save(TopicForm f, String username) {
        Lecturer me = currentLecturer.require(username);
        RegistrationRound round = roundRepository.findById(f.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
        if (!isProposalOpen(round, LocalDateTime.now())) {
            throw new BusinessException("Đợt \"" + round.getName() + "\" không còn trong thời gian giảng viên đề xuất đề tài.");
        }
        Department dept = departmentRepository.findById(f.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));
        Lecturer advisor2 = null;
        if (f.getAdvisor2Id() != null) {
            if (f.getAdvisor2Id().equals(me.getId())) {
                throw new BusinessException("GVHD 2 phải là giảng viên khác với GVHD 1 (là bạn).");
            }
            advisor2 = lecturerRepository.findById(f.getAdvisor2Id())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên hướng dẫn 2."));
        }

        Topic t;
        if (f.getId() == null) {
            t = new Topic();
            t.setCode(nextCode());
            t.setCreatedBy(me);
        } else {
            t = get(f.getId());
            StudentGroup g = groupRepository.findByTopicId(t.getId()).orElse(null);
            if (!canModify(t, me, g)) {
                throw new BusinessException("Đề tài này không còn được phép chỉnh sửa.");
            }
        }
        t.setRound(round);
        t.setDepartment(dept);
        t.setTitle(f.getTitle().trim());
        t.setDescription(f.getDescription().trim());
        t.setRequirements(f.getRequirements());
        t.setStatus(TopicStatus.PENDING);   // gửi (lại) cho Trưởng bộ môn thẩm định
        t.setReviewNote(null);
        t = topicRepository.save(t);

        advisorRepository.deleteByTopicId(t.getId());
        advisorRepository.flush();
        saveAdvisor(t, me, true);
        if (advisor2 != null) {
            saveAdvisor(t, advisor2, false);
        }
        return t;
    }

    public void delete(Long id, String username) {
        Lecturer me = currentLecturer.require(username);
        Topic t = get(id);
        StudentGroup g = groupRepository.findByTopicId(id).orElse(null);
        if (!canModify(t, me, g)) {
            throw new BusinessException("Chỉ xóa được đề tài do bạn tạo, còn chờ duyệt hoặc bị từ chối, và chưa có nhóm đăng ký.");
        }
        advisorRepository.deleteByTopicId(id);
        advisorRepository.flush();
        topicRepository.delete(t);
    }

    private boolean canModify(Topic t, Lecturer me, StudentGroup group) {
        boolean mine = t.getCreatedBy() != null && t.getCreatedBy().getId().equals(me.getId());
        boolean statusOk = t.getStatus() == TopicStatus.PENDING || t.getStatus() == TopicStatus.REJECTED;
        boolean windowOpen = isProposalOpen(t.getRound(), LocalDateTime.now());
        return mine && statusOk && group == null && windowOpen;
    }

    private boolean isProposalOpen(RegistrationRound r, LocalDateTime now) {
        return !now.isBefore(r.getLecturerStartDate()) && !now.isAfter(r.getLecturerEndDate());
    }

    private Topic get(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài."));
    }

    private void saveAdvisor(Topic t, Lecturer l, boolean main) {
        TopicAdvisor a = new TopicAdvisor();
        a.setTopic(t);
        a.setLecturer(l);
        a.setIsMainAdvisor(main);
        advisorRepository.save(a);
    }

    private String nextCode() {
        long n = topicRepository.count() + 1;
        String code;
        do {
            code = String.format("DT%03d", n++);
        } while (topicRepository.existsByCode(code));
        return code;
    }

    private String advisorNames(Long topicId) {
        return advisorRepository.findByTopicId(topicId).stream()
                .map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
    }
}
