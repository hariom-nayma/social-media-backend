package com.socialmedia.app.config;

import com.socialmedia.app.dto.UserProfileWithPostsDTO;
import com.socialmedia.app.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(User.class, UserProfileWithPostsDTO.class)
                .addMappings(mapper -> {
                    mapper.skip(UserProfileWithPostsDTO::setFollowing);
                    mapper.skip(UserProfileWithPostsDTO::setRequested);
                });
        return modelMapper;
    }
}
