package com.socialmedia.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
	Optional<User> findUserById(String userId);
}
