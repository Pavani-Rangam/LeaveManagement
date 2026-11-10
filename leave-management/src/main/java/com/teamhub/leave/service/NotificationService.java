package com.teamhub.leave.service;

import com.teamhub.leave.model.*;
import com.teamhub.leave.repo.*;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository repo;
    private final EmployeeRepository employees;

    public NotificationService(NotificationRepository repo, EmployeeRepository employees) {
        this.repo = repo;
        this.employees = employees;
    }

    public void notify(Employee to, String message, String link) {
        Notification n = new Notification();
        n.setRecipient(to);
        n.setMessage(message);
        n.setLink(link);
        repo.save(n);
    }

    public void notifyAdmins(String message, String link, Long exceptId) {
        for (Employee a : employees.findByRole("MANAGER"))
            if (!a.getId().equals(exceptId)) notify(a, message, link);
    }
}
