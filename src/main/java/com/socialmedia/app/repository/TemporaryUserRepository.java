package com.socialmedia.app.repository;

import com.socialmedia.app.model.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {


    Optional<TemporaryUser> findByEmail(String email);
    Optional<TemporaryUser> findByUsername(String username);
}
