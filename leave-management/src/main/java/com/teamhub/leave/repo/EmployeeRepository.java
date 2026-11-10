package com.teamhub.leave.repo;

import com.teamhub.leave.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findByRole(String role);

    @Query("select e from Employee e where lower(e.name) like lower(concat('%', :q, '%')) "
         + "or lower(e.email) like lower(concat('%', :q, '%')) "
         + "or lower(e.empCode) like lower(concat('%', :q, '%')) "
         + "or lower(e.department) like lower(concat('%', :q, '%')) order by e.name")
    List<Employee> search(@Param("q") String q);
}
