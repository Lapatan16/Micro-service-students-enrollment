package com.example.enrollments.repo;
import com.example.enrollments.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>
{ }