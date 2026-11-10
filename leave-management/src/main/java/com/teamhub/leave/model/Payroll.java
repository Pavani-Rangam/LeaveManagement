package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Payroll {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Employee employee;
    private String payMonth;          // e.g. 2026-11
    private double basic, allowances, deductions;
    private LocalDate generatedDate = LocalDate.now();

    public double getNet() { return basic + allowances - deductions; }
    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee v) { employee = v; }
    public String getPayMonth() { return payMonth; }
    public void setPayMonth(String v) { payMonth = v; }
    public double getBasic() { return basic; }
    public void setBasic(double v) { basic = v; }
    public double getAllowances() { return allowances; }
    public void setAllowances(double v) { allowances = v; }
    public double getDeductions() { return deductions; }
    public void setDeductions(double v) { deductions = v; }
    public LocalDate getGeneratedDate() { return generatedDate; }
}
