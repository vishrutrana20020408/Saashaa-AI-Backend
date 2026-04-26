package backend.ai_interview.repository;

import backend.ai_interview.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notification Repository
 */
@Repository
@SuppressWarnings("all")
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByIsReadOrderByCreatedAtDesc(Boolean isRead);

    long countByIsRead(Boolean isRead);
}