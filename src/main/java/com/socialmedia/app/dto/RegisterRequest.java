package com.socialmedia.app.dto;


import com.socialmedia.app.validators.TrimmedNotEmpty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    
    @NotBlank
    private String email;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;

    @NotBlank
    @TrimmedNotEmpty
    private String username;

    @NotBlank
    @TrimmedNotEmpty
    private String password;
}
