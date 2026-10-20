package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.enums.RoundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Form tạo/sửa đợt đăng ký (Trưởng khoa). */
public class RoundForm {

    private Long id;

    @NotBlank(message = "Tên đợt đăng ký không được để trống")
    @Size(max = 150, message = "Tên đợt tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn loại đợt")
    private RoundType type = RoundType.MON_HOC;

    @NotNull(message = "Chọn thời gian bắt đầu GĐ1")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lecturerStartDate;

    @NotNull(message = "Chọn thời gian kết thúc GĐ1")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lecturerEndDate;

    @NotNull(message = "Chọn thời gian bắt đầu GĐ2")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime studentStartDate;

    @NotNull(message = "Chọn thời gian kết thúc GĐ2")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime studentEndDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime reviewerDeadline;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime councilReportDate;

    public static RoundForm from(RegistrationRound r) {
        RoundForm f = new RoundForm();
        f.id = r.getId();
        f.name = r.getName();
        f.type = r.getType();
        f.lecturerStartDate = r.getLecturerStartDate();
        f.lecturerEndDate = r.getLecturerEndDate();
        f.studentStartDate = r.getStudentStartDate();
        f.studentEndDate = r.getStudentEndDate();
        f.reviewerDeadline = r.getReviewerDeadline();
        f.councilReportDate = r.getCouncilReportDate();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RoundType getType() { return type; }
    public void setType(RoundType type) { this.type = type; }
    public LocalDateTime getLecturerStartDate() { return lecturerStartDate; }
    public void setLecturerStartDate(LocalDateTime v) { this.lecturerStartDate = v; }
    public LocalDateTime getLecturerEndDate() { return lecturerEndDate; }
    public void setLecturerEndDate(LocalDateTime v) { this.lecturerEndDate = v; }
    public LocalDateTime getStudentStartDate() { return studentStartDate; }
    public void setStudentStartDate(LocalDateTime v) { this.studentStartDate = v; }
    public LocalDateTime getStudentEndDate() { return studentEndDate; }
    public void setStudentEndDate(LocalDateTime v) { this.studentEndDate = v; }
    public LocalDateTime getReviewerDeadline() { return reviewerDeadline; }
    public void setReviewerDeadline(LocalDateTime v) { this.reviewerDeadline = v; }
    public LocalDateTime getCouncilReportDate() { return councilReportDate; }
    public void setCouncilReportDate(LocalDateTime v) { this.councilReportDate = v; }
}
