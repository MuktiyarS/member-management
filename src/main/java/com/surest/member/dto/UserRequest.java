package com.surest.member.dto;

import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotBlank(message = "User name is required")
    @Size(max = 50, message = "User name must be less than 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    public User toEntity(String passwordHash, Set<Role> roles) {
        return User.builder()
                .username(this.username)
                .email(this.email)
                .passwordHash(passwordHash)
                .dateOfBirth(this.dateOfBirth)
                .roles(roles)
                .build();
    }
}
