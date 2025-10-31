package com.socialmedia.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.app.model.NewUser;

public interface NewUserRepository extends JpaRepository<NewUser, String> {
	boolean existsByUsername(String username);

	Optional<NewUser> findByEmail(String email);

	void delete(NewUser nu);

}
