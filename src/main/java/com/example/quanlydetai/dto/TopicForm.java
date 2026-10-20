package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.Topic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Form giảng viên đề xuất/sửa đề tài (Giai đoạn 1). GVHD 1 luôn là người đang đăng nhập. */
public class TopicForm {

    private Long id;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long roundId;

    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long departmentId;

    @NotBlank(message = "Tên đề tài không được để trống")
    @Size(max = 255, message = "Tên đề tài tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Mục tiêu / mô tả đề tài không được để trống")
    private String description;

    private String requirements;

    /** GVHD thứ 2 (tùy chọn) - đề tài có tối đa 2 GVHD. */
    private Long advisor2Id;

    public static TopicForm from(Topic t, Long advisor2Id) {
        TopicForm f = new TopicForm();
        f.id = t.getId();
        f.roundId = t.getRound().getId();
        f.departmentId = t.getDepartment().getId();
        f.title = t.getTitle();
        f.description = t.getDescription();
        f.requirements = t.getRequirements();
        f.advisor2Id = advisor2Id;
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public Long getAdvisor2Id() { return advisor2Id; }
    public void setAdvisor2Id(Long advisor2Id) { this.advisor2Id = advisor2Id; }
}
