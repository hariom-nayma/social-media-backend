package com.socialmedia.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.Notification;
import com.socialmedia.app.model.User;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findTop50ByUserOrderByCreatedAtDesc(User user);
}

