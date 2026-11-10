package com.teamhub.leave.repo;

import com.teamhub.leave.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByEmployeeOrderByPayMonthDescIdDesc(Employee e);
    List<Payroll> findAllByOrderByPayMonthDescIdDesc();
    boolean existsByEmployeeAndPayMonth(Employee e, String payMonth);
}
