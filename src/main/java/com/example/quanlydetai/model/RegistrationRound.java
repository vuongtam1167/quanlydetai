package com.example.quanlydetai.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.quanlydetai.model.enums.RoundType;

@Entity
@Table(name = "registration_rounds")
public class RegistrationRound {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundType type;

    @Column(name = "lecturer_start_date", nullable = false)
    private LocalDateTime lecturerStartDate;

    @Column(name = "lecturer_end_date", nullable = false)
    private LocalDateTime lecturerEndDate;

    @Column(name = "student_start_date", nullable = false)
    private LocalDateTime studentStartDate;

    @Column(name = "student_end_date", nullable = false)
    private LocalDateTime studentEndDate;

    @Column(name = "reviewer_deadline")
    private LocalDateTime reviewerDeadline;

    @Column(name = "council_report_date")
    private LocalDateTime councilReportDate;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoundType getType() {
        return type;
    }

    public void setType(RoundType type) {
        this.type = type;
    }

    public LocalDateTime getLecturerStartDate() {
        return lecturerStartDate;
    }

    public void setLecturerStartDate(LocalDateTime lecturerStartDate) {
        this.lecturerStartDate = lecturerStartDate;
    }

    public LocalDateTime getLecturerEndDate() {
        return lecturerEndDate;
    }

    public void setLecturerEndDate(LocalDateTime lecturerEndDate) {
        this.lecturerEndDate = lecturerEndDate;
    }

    public LocalDateTime getStudentStartDate() {
        return studentStartDate;
    }

    public void setStudentStartDate(LocalDateTime studentStartDate) {
        this.studentStartDate = studentStartDate;
    }

    public LocalDateTime getStudentEndDate() {
        return studentEndDate;
    }

    public void setStudentEndDate(LocalDateTime studentEndDate) {
        this.studentEndDate = studentEndDate;
    }

    public LocalDateTime getReviewerDeadline() {
        return reviewerDeadline;
    }

    public void setReviewerDeadline(LocalDateTime reviewerDeadline) {
        this.reviewerDeadline = reviewerDeadline;
    }

    public LocalDateTime getCouncilReportDate() {
        return councilReportDate;
    }

    public void setCouncilReportDate(LocalDateTime councilReportDate) {
        this.councilReportDate = councilReportDate;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
    
}