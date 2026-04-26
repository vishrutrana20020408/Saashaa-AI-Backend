package backend.ai_interview.service;

import backend.ai_interview.entity.Notification;
import backend.ai_interview.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notification Service
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(String title, String subtitle, String type) {
        Notification notification = Notification.builder()
                .title(title)
                .subtitle(subtitle)
                .type(type)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadOrderByCreatedAtDesc(false);
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsRead(false);
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead() {
        List<Notification> unread = getUnreadNotifications();
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}