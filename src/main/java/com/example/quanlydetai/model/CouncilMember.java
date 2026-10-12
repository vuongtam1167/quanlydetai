package com.example.quanlydetai.model;
import com.example.quanlydetai.model.enums.CouncilRole;

import jakarta.persistence.*;

@Entity
@Table(name = "council_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"council_id", "lecturer_id"})
})
public class CouncilMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_council", nullable = false)
    private CouncilRole roleInCouncil;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Council getCouncil() {
        return council;
    }

    public void setCouncil(Council council) {
        this.council = council;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public CouncilRole getRoleInCouncil() {
        return roleInCouncil;
    }

    public void setRoleInCouncil(CouncilRole roleInCouncil) {
        this.roleInCouncil = roleInCouncil;
    }
    
}