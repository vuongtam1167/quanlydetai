package com.example.quanlydetai.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.quanlydetai.model.enums.CouncilStatus;

@Entity
@Table(name = "councils")
public class Council {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private RegistrationRound round;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String location;

    @Column(name = "defense_date", nullable = false)
    private LocalDateTime defenseDate;

    @Enumerated(EnumType.STRING)
    private CouncilStatus status = CouncilStatus.PLANNED;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getDefenseDate() {
        return defenseDate;
    }

    public void setDefenseDate(LocalDateTime defenseDate) {
        this.defenseDate = defenseDate;
    }

    public CouncilStatus getStatus() {
        return status;
    }

    public void setStatus(CouncilStatus status) {
        this.status = status;
    }
    
}