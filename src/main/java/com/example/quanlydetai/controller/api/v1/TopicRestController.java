package com.example.quanlydetai.controller.api.v1;

import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.service.TopicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/topics")
public class TopicRestController {

    private final TopicService topicService;

    public TopicRestController(TopicService topicService) {
        this.topicService = topicService;
    }

    // Trả về danh sách JSON
    @GetMapping
    public List<Topic> getAllTopics() {
        return topicService.findAll();
    }

    @GetMapping("/{id}")
    public Topic getTopic(@PathVariable Long id) {
        return topicService.findById(id);
    }

    // Đọc JSON từ Request Body
    @PostMapping
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_BO_MON', 'ADMIN')")
    public Topic createTopic(@RequestBody Topic topic) {
        return topicService.createTopic(topic);
    }

    // Xóa đề tài (Sử dụng HTTP DELETE chuẩn REST)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Cấu hình bảo mật chặn thao tác của người không đủ quyền[cite: 3]
    public void deleteTopic(@PathVariable Long id) {
        topicService.deleteById(id);
    }

    // Xử lý ngoại lệ để trả về mã lỗi 404 thay vì trang trắng
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }
}