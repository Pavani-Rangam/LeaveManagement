package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String empCode, name, jobTitle, department, role, password;
    @Column(unique = true)
    private String email;
    private LocalDate joinDate = LocalDate.now();
    private int annualBalance = 20, sickBalance = 10, casualBalance = 8, otherBalance = 5;

    public int getBalance(LeaveType t) {
        return switch (t) {
            case ANNUAL -> annualBalance;
            case SICK -> sickBalance;
            case CASUAL -> casualBalance;
            case OTHER -> otherBalance;
        };
    }
    public void setBalance(LeaveType t, int v) {
        switch (t) {
            case ANNUAL -> annualBalance = v;
            case SICK -> sickBalance = v;
            case CASUAL -> casualBalance = v;
            case OTHER -> otherBalance = v;
        }
    }
    public boolean isManager() { return "MANAGER".equals(role); }
    public String getInitials() {
        String[] p = name.trim().split("\\s+");
        return (p[0].substring(0, 1) + (p.length > 1 ? p[p.length - 1].substring(0, 1) : "")).toUpperCase();
    }

    public Long getId() { return id; }
    public String getEmpCode() { return empCode; }
    public void setEmpCode(String v) { empCode = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String v) { jobTitle = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { department = v; }
    public String getRole() { return role; }
    public void setRole(String v) { role = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate v) { joinDate = v; }
    public int getAnnualBalance() { return annualBalance; }
    public int getSickBalance() { return sickBalance; }
    public int getCasualBalance() { return casualBalance; }
    public int getOtherBalance() { return otherBalance; }
}
