package com.example.enrollments.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long studentId;

    @NotBlank
    @Size(min = 3, max = 12)
    private String courseCode; // npr. DS101

    @NotBlank
    @Size(min = 4, max = 20)
    private String semester; // npr. 067/2022
}
