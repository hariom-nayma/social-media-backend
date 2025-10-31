package com.socialmedia.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.socialmedia.app.model.User;

import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.followers LEFT JOIN FETCH u.following WHERE u.username = :username")
    Optional<User> findByUsername(String username);
	Optional<User> findUserById(String userId);
}
