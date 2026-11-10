package com.teamhub.leave.repo;

import com.teamhub.leave.model.JobOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {
    List<JobOpening> findAllByOrderByIdDesc();
    List<JobOpening> findByStatusOrderByIdDesc(String status);
}
