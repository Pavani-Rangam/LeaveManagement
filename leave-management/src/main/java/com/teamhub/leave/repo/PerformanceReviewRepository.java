package com.teamhub.leave.repo;

import com.teamhub.leave.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployeeOrderByReviewDateDescIdDesc(Employee e);
    List<PerformanceReview> findAllByOrderByReviewDateDescIdDesc();
}
