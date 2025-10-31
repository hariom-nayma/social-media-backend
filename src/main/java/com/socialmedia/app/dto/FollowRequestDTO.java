package com.socialmedia.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequestDTO {
    private Long id;
    private UserDTO follower;
    private UserDTO target;
    private boolean accepted;
}
