package com.example.quanlydetai.model;
import jakarta.persistence.*;

@Entity
@Table(name = "topic_advisors", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"topic_id", "lecturer_id"})
})
public class TopicAdvisor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @Column(name = "is_main_advisor")
    private Boolean isMainAdvisor = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public Boolean getIsMainAdvisor() {
        return isMainAdvisor;
    }

    public void setIsMainAdvisor(Boolean isMainAdvisor) {
        this.isMainAdvisor = isMainAdvisor;
    }
    
}