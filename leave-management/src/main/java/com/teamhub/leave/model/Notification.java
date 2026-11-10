package com.teamhub.leave.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Employee recipient;
    @Column(length = 500)
    private String message;
    private String link;
    private boolean seen;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Employee getRecipient() { return recipient; }
    public void setRecipient(Employee v) { recipient = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { message = v; }
    public String getLink() { return link; }
    public void setLink(String v) { link = v; }
    public boolean isSeen() { return seen; }
    public void setSeen(boolean v) { seen = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
