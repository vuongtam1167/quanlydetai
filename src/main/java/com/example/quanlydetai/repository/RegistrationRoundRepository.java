package com.example.quanlydetai.repository;

import com.example.quanlydetai.model.RegistrationRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRoundRepository extends JpaRepository<RegistrationRound, Long> {
    List<RegistrationRound> findAllByOrderByLecturerStartDateDesc();
}
