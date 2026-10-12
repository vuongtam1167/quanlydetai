package com.example.quanlydetai.model;
import jakarta.persistence.*;
import java.math.BigDecimal;

import com.example.quanlydetai.model.enums.GroupStatus;

@Entity
@Table(name = "student_groups")
public class StudentGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private RegistrationRound round;

    @OneToOne
    @JoinColumn(name = "topic_id", unique = true)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.FORMING;

    @Column(name = "final_score", precision = 4, scale = 2)
    private BigDecimal finalScore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public RegistrationRound getRound() {
        return round;
    }

    public void setRound(RegistrationRound round) {
        this.round = round;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public void setStatus(GroupStatus status) {
        this.status = status;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }
    
}