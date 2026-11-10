package com.teamhub.leave.repo;

import com.teamhub.leave.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDescIdDesc(Employee recipient);
    List<Notification> findByRecipientAndSeenFalse(Employee recipient);
    long countByRecipientAndSeenFalse(Employee recipient);
}
