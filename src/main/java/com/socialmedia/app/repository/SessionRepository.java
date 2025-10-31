package com.socialmedia.app.repository;

import com.socialmedia.app.model.Session;
import com.socialmedia.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByUserAndActiveTrue(User user);
    Optional<Session> findByJwtTokenAndActiveTrue(String jwtToken);
    void deleteAllByUserAndActiveTrue(User user);
}
