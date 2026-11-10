package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PerformanceReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Employee employee;
    @ManyToOne(optional = false)
    private Employee reviewer;
    private int rating;
    @Column(length = 1000)
    private String comments;
    private LocalDate reviewDate = LocalDate.now();

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee v) { employee = v; }
    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee v) { reviewer = v; }
    public int getRating() { return rating; }
    public void setRating(int v) { rating = v; }
    public String getComments() { return comments; }
    public void setComments(String v) { comments = v; }
    public LocalDate getReviewDate() { return reviewDate; }
}
