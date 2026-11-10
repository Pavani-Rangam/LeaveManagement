package com.teamhub.leave.repo;

import com.teamhub.leave.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndWorkDate(Employee e, LocalDate d);
    List<Attendance> findTop30ByEmployeeOrderByWorkDateDescIdDesc(Employee e);
    List<Attendance> findTop50ByOrderByWorkDateDescIdDesc();
}
