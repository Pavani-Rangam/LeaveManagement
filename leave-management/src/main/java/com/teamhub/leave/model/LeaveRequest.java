package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class LeaveRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Employee employee;
    @Enumerated(EnumType.STRING) @Column(name = "leave_type")
    private LeaveType type;
    private LocalDate startDate, endDate, submitDate;
    private int days;
    private String reason;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee v) { employee = v; }
    public LeaveType getType() { return type; }
    public void setType(LeaveType v) { type = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { endDate = v; }
    public LocalDate getSubmitDate() { return submitDate; }
    public void setSubmitDate(LocalDate v) { submitDate = v; }
    public int getDays() { return days; }
    public void setDays(int v) { days = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { reason = v; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus v) { status = v; }
}
