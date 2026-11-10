package com.teamhub.leave.web;

import com.teamhub.leave.model.Employee;
import com.teamhub.leave.repo.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class Auth {
    private final EmployeeRepository employees;
    public Auth(EmployeeRepository employees) { this.employees = employees; }

    /** Returns the logged-in employee or null. */
    public Employee user(HttpSession session) {
        Object id = session.getAttribute("empId");
        return id == null ? null : employees.findById((Long) id).orElse(null);
    }
}
