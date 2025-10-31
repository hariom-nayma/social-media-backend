package com.socialmedia.app.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;





import com.socialmedia.app.model.RefreshToken;
import com.socialmedia.app.model.User;





public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(User user);
}

