package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.TopicRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.enums.TopicStatus;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Trưởng bộ môn thẩm định đề tài của bộ môn mình: thông qua (gửi Trưởng khoa) hoặc yêu cầu sửa. */
@Service
@Transactional
public class DepartmentTopicService {

    private final CurrentLecturerService currentLecturer;
    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository advisorRepository;

    public DepartmentTopicService(CurrentLecturerService currentLecturer, TopicRepository topicRepository,
                                  TopicAdvisorRepository advisorRepository) {
        this.currentLecturer = currentLecturer;
        this.topicRepository = topicRepository;
        this.advisorRepository = advisorRepository;
    }

    @Transactional(readOnly = true)
    public String departmentName(String username) {
        return currentLecturer.require(username).getDepartment().getName();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> counts(String username) {
        Long deptId = currentLecturer.require(username).getDepartment().getId();
        Map<String, Long> m = new LinkedHashMap<>();
        for (TopicStatus s : TopicStatus.values()) {
            m.put(s.name(), topicRepository.countByDepartmentIdAndStatus(deptId, s));
        }
        m.put("ALL", topicRepository.countByDepartmentId(deptId));
        return m;
    }

    @Transactional(readOnly = true)
    public List<TopicRow> list(String username, String status) {
        Long deptId = currentLecturer.require(username).getDepartment().getId();
        TopicStatus st = parse(status);
        List<Topic> topics = st == null
                ? topicRepository.findByDepartmentIdOrderByIdDesc(deptId)
                : topicRepository.findByDepartmentIdAndStatusOrderByIdDesc(deptId, st);
        return topics.stream().map(t -> TopicRow.of(t, advisorNames(t.getId()))).toList();
    }

    public void approve(Long id, String username) {
        Topic t = loadForHead(id, username);
        t.setStatus(TopicStatus.DEAN_APPROVED);
        t.setReviewNote(null);
        topicRepository.save(t);
    }

    public void requestEdit(Long id, String username, String feedback) {
        if (feedback == null || feedback.isBlank()) {
            throw new BusinessException("Vui lòng nhập nội dung cần chỉnh sửa.");
        }
        Topic t = loadForHead(id, username);
        t.setStatus(TopicStatus.REJECTED);
        t.setReviewNote("[Bộ môn yêu cầu sửa] " + feedback.trim());
        topicRepository.save(t);
    }

    private Topic loadForHead(Long id, String username) {
        Lecturer head = currentLecturer.require(username);
        Topic t = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài."));
        if (!t.getDepartment().getId().equals(head.getDepartment().getId())) {
            throw new BusinessException("Đề tài này không thuộc bộ môn của bạn.");
        }
        if (t.getStatus() != TopicStatus.PENDING) {
            throw new BusinessException("Chỉ thẩm định được đề tài đang chờ bộ môn.");
        }
        return t;
    }

    private String advisorNames(Long topicId) {
        return advisorRepository.findByTopicId(topicId).stream()
                .map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
    }

    private TopicStatus parse(String status) {
        if (status == null || status.isBlank() || "ALL".equals(status)) {
            return null;
        }
        try {
            return TopicStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
