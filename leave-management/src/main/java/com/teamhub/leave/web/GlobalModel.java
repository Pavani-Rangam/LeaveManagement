package com.teamhub.leave.web;

import com.teamhub.leave.model.Employee;
import com.teamhub.leave.repo.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the logged-in user and unread-notification count available to every page. */
@ControllerAdvice
public class GlobalModel {
    private final Auth auth;
    private final NotificationRepository notifications;

    public GlobalModel(Auth auth, NotificationRepository notifications) {
        this.auth = auth;
        this.notifications = notifications;
    }

    @ModelAttribute("me")
    public Employee me(HttpSession session) { return auth.user(session); }

    @ModelAttribute("unread")
    public long unread(HttpSession session) {
        Employee e = auth.user(session);
        return e == null ? 0 : notifications.countByRecipientAndSeenFalse(e);
    }
}
