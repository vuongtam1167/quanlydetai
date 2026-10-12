package com.example.quanlydetai.service;

import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.enums.TopicStatus;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional // Đảm bảo rollback nếu có RuntimeException
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true) // Tối ưu hiệu suất cho tác vụ đọc
    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Topic findById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề tài ID: " + id)); // Kích hoạt rollback
    }

    public Topic createTopic(Topic topic) {
        topic.setStatus(TopicStatus.PENDING); // Giai đoạn 1: Khởi tạo chờ duyệt (BR-01)
        return topicRepository.save(topic);
    }

    // Nghiệp vụ: Trưởng khoa/Bộ môn duyệt đề tài
    public Topic approveTopic(Long id) {
        Topic topic = findById(id);
        topic.setStatus(TopicStatus.APPROVED);
        return topicRepository.save(topic);
    }
    
    // Nghiệp vụ: Lấy danh sách đề tài đã duyệt cho Sinh viên chọn ở Giai đoạn 2 (BR-01)
    @Transactional(readOnly = true)
    public List<Topic> getApprovedTopicsByRound(Long roundId) {
        return topicRepository.findByRoundId(roundId).stream()
                .filter(t -> t.getStatus() == TopicStatus.APPROVED)
                .toList();
    }
}