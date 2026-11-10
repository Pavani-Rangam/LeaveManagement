package com.teamhub.leave.repo;

import com.teamhub.leave.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeOrderBySubmitDateDescIdDesc(Employee employee);
    List<LeaveRequest> findAllByOrderBySubmitDateDescIdDesc();
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByEmployeeAndTypeAndStatus(Employee e, LeaveType t, LeaveStatus s);

    // Overlap with any Pending or Approved request of the same employee
    @Query("select count(l) > 0 from LeaveRequest l where l.employee = :emp "
         + "and l.status <> com.teamhub.leave.model.LeaveStatus.REJECTED "
         + "and l.startDate <= :e and l.endDate >= :s")
    boolean existsOverlap(@Param("emp") Employee emp, @Param("s") LocalDate s, @Param("e") LocalDate e);
}
