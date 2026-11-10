package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
public class Attendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Employee employee;
    private LocalDate workDate;
    private LocalTime checkIn, checkOut;

    public String getHours() {
        if (checkIn == null || checkOut == null) return "-";
        long mins = Duration.between(checkIn, checkOut).toMinutes();
        return (mins / 60) + "h " + String.format("%02d", mins % 60) + "m";
    }
    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee v) { employee = v; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate v) { workDate = v; }
    public LocalTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalTime v) { checkIn = v; }
    public LocalTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalTime v) { checkOut = v; }
}
