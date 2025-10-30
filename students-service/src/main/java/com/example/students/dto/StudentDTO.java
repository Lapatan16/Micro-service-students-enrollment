package com.example.students.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

public record StudentDTO(
        //@NotNull(message = "Id cannot be null")
        Long id,

        @NotBlank(message = "Full name cannot be blank")
        @Pattern(
                regexp = "^[A-ZŠĐŽČĆ][a-zšđžčć]+(?:\\s[A-ZŠĐŽČĆ][a-zšđžčć]+)+$",
                message = "Full name must contain at least first and last name, starting with capital letters"
        )
        String fullName,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Index number cannot be blank")
        @Pattern(regexp = "\\d{3}/\\d{4}", message = "Index number must follow format XXX/YYYY")
        String indexNumber
) {}
