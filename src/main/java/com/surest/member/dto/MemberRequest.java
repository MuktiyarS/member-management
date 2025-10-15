package com.surest.member.dto;

import com.surest.member.entity.Member;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MemberRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    public Member toEntity() {
        return Member.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .dateOfBirth(this.dateOfBirth)
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
    }
}
