package com.socialmedia.app.repository;

import com.socialmedia.app.model.FollowRequest;
import com.socialmedia.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    Optional<FollowRequest> findByFollowerAndTarget(User follower, User target);
    List<FollowRequest> findByTargetAndAccepted(User target, boolean accepted);
	void deleteByFollowerAndTarget(User follower, User target);
}
