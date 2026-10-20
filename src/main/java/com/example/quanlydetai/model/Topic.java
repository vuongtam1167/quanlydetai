package com.example.quanlydetai.model;
import jakarta.persistence.*;

import com.example.quanlydetai.model.enums.TopicStatus;

@Entity
@Table(name = "topics")
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private RegistrationRound round;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "max_members")
    private Integer maxMembers = 3;

    @Enumerated(EnumType.STRING)
    private TopicStatus status = TopicStatus.PENDING;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Lecturer createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistrationRound getRound() {
        return round;
    }

    public void setRound(RegistrationRound round) {
        this.round = round;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public Lecturer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Lecturer createdBy) {
        this.createdBy = createdBy;
    }

    
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}
