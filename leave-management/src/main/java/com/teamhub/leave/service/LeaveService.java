package com.teamhub.leave.service;

import com.teamhub.leave.model.*;
import com.teamhub.leave.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LeaveService {
    private final LeaveRequestRepository repo;
    private final EmployeeRepository employees;
    private final NotificationService notifier;

    public LeaveService(LeaveRequestRepository repo, EmployeeRepository employees, NotificationService notifier) {
        this.repo = repo;
        this.employees = employees;
        this.notifier = notifier;
    }

    /** Apply for leave: validates dates, overlap and balance. */
    @Transactional
    public LeaveRequest apply(Employee emp, LeaveType type, LocalDate start, LocalDate end, String reason) {
        if (emp.isManager()) throw new LeaveException("Admins cannot apply for leave.");
        if (type == null) throw new LeaveException("Please select a leave type.");
        if (start == null || end == null) throw new LeaveException("Start date and end date are required.");
        if (end.isBefore(start)) throw new LeaveException("End date cannot be before start date.");
        if (start.isBefore(LocalDate.now())) throw new LeaveException("Start date cannot be in the past.");
        if (reason == null || reason.isBlank()) throw new LeaveException("Please provide a reason for your leave.");
        if (repo.existsOverlap(emp, start, end))
            throw new LeaveException("These dates overlap with an existing pending or approved leave request.");

        int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
        int pending = repo.findByEmployeeAndTypeAndStatus(emp, type, LeaveStatus.PENDING)
                .stream().mapToInt(LeaveRequest::getDays).sum();
        int available = emp.getBalance(type) - pending;
        if (days > available)
            throw new LeaveException("Insufficient " + type.getLabel() + " balance. You requested " + days
                    + " day(s) but only " + Math.max(available, 0) + " day(s) are available.");

        LeaveRequest r = new LeaveRequest();
        r.setEmployee(emp);
        r.setType(type);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setSubmitDate(LocalDate.now());
        r.setDays(days);
        r.setReason(reason.trim());
        r.setStatus(LeaveStatus.PENDING);
        r = repo.save(r);
        notifier.notifyAdmins(emp.getName() + " applied for " + type.getLabel() + " (" + days + " day(s), "
                + start + " to " + end + ").", "/dashboard", emp.getId());
        return r;
    }

    /** Approve: deducts the days from the employee's balance. */
    @Transactional
    public void approve(Long id, Employee admin) {
        LeaveRequest r = loadPending(id, admin);
        Employee e = r.getEmployee();
        if (e.getId().equals(admin.getId())) throw new LeaveException("You cannot approve your own leave request. Another admin must do it.");
        int balance = e.getBalance(r.getType());
        if (r.getDays() > balance)
            throw new LeaveException(e.getName() + " no longer has enough " + r.getType().getLabel() + " balance.");
        e.setBalance(r.getType(), balance - r.getDays());
        employees.save(e);
        r.setStatus(LeaveStatus.APPROVED);
        repo.save(r);
        notifier.notify(e, "Your " + r.getType().getLabel() + " request (" + r.getStartDate() + " to " + r.getEndDate() + ") was approved.", "/dashboard");
    }

    @Transactional
    public void reject(Long id, Employee admin) {
        LeaveRequest r = loadPending(id, admin);
        r.setStatus(LeaveStatus.REJECTED);
        repo.save(r);
        notifier.notify(r.getEmployee(), "Your " + r.getType().getLabel() + " request (" + r.getStartDate() + " to " + r.getEndDate() + ") was rejected.", "/dashboard");
    }

    private LeaveRequest loadPending(Long id, Employee admin) {
        if (admin == null || !admin.isManager()) throw new LeaveException("Only admins can approve or reject leave.");
        LeaveRequest r = repo.findById(id).orElseThrow(() -> new LeaveException("Leave request not found."));
        if (r.getStatus() != LeaveStatus.PENDING)
            throw new LeaveException("This request has already been " + r.getStatus().getLabel().toLowerCase() + ".");
        return r;
    }
}
