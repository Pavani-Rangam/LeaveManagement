package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class JobOpening {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title, department, location, status = "OPEN";
    private int openings = 1;
    private LocalDate postedDate = LocalDate.now();

    public boolean isOpen() { return "OPEN".equals(status); }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { department = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { location = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public int getOpenings() { return openings; }
    public void setOpenings(int v) { openings = v; }
    public LocalDate getPostedDate() { return postedDate; }
}
