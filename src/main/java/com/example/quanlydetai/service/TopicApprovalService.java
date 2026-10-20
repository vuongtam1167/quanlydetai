package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.TopicRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.TopicAdvisor;
import com.example.quanlydetai.model.enums.TopicStatus;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Trưởng khoa phê duyệt/từ chối đề tài đã được Trưởng bộ môn thông qua (Giai đoạn 1). */
@Service
@Transactional
public class TopicApprovalService {

    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository advisorRepository;

    public TopicApprovalService(TopicRepository topicRepository, TopicAdvisorRepository advisorRepository) {
        this.topicRepository = topicRepository;
        this.advisorRepository = advisorRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> counts() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (TopicStatus s : TopicStatus.values()) {
            m.put(s.name(), topicRepository.countByStatus(s));
        }
        m.put("ALL", topicRepository.count());
        return m;
    }

    @Transactional(readOnly = true)
    public List<TopicRow> list(String status, Long roundId) {
        TopicStatus st = parse(status);
        List<Topic> topics;
        if (roundId != null && st != null) {
            topics = topicRepository.findByRoundIdAndStatusOrderByIdDesc(roundId, st);
        } else if (roundId != null) {
            topics = topicRepository.findByRoundIdOrderByIdDesc(roundId);
        } else if (st != null) {
            topics = topicRepository.findByStatusOrderByIdDesc(st);
        } else {
            topics = topicRepository.findAllByOrderByIdDesc();
        }
        return topics.stream().map(t -> TopicRow.of(t, advisorNames(t.getId()))).toList();
    }

    public void approve(Long id) {
        Topic t = get(id);
        if (t.getStatus() != TopicStatus.DEAN_APPROVED) {
            throw new BusinessException("Chỉ phê duyệt được đề tài đã được Trưởng bộ môn thông qua.");
        }
        t.setStatus(TopicStatus.APPROVED);
        t.setReviewNote(null);
        topicRepository.save(t);
    }

    public void reject(Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do từ chối.");
        }
        Topic t = get(id);
        if (t.getStatus() != TopicStatus.DEAN_APPROVED) {
            throw new BusinessException("Chỉ từ chối được đề tài đang chờ Trưởng khoa duyệt.");
        }
        t.setStatus(TopicStatus.REJECTED);
        t.setReviewNote("[Trưởng khoa] " + reason.trim());
        topicRepository.save(t);
    }

    private Topic get(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài."));
    }

    private String advisorNames(Long topicId) {
        List<TopicAdvisor> advisors = advisorRepository.findByTopicId(topicId);
        return advisors.stream().map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
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
