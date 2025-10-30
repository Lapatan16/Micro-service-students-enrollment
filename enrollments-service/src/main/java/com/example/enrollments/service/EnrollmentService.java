package com.example.enrollments.service;

import com.example.enrollments.dto.EnrollmentDTO;
import com.example.enrollments.dto.StudentDTO;
import com.example.enrollments.feign.StudentClient;
import com.example.enrollments.model.Enrollment;
import com.example.enrollments.repo.EnrollmentRepository;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository repo;
    private final StudentClient studentClient;

    public EnrollmentService(EnrollmentRepository repo, StudentClient studentClient) {
        this.repo = repo;
        this.studentClient = studentClient;
    }

    /** Return all enrollments */
    public List<Enrollment> all() {
        return repo.findAll();
    }

    /** Return a single enrollment or throw 404 */
    public Enrollment byId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Enrollment with ID " + id + " not found"));
    }

    /** Create a new enrollment (validates student existence) */
    public Enrollment create(EnrollmentDTO dto) {
        log.info("Creating enrollment for studentId={}, courseCode={}, semester={}",
                dto.studentId(), dto.courseCode(), dto.semester());

        StudentDTO student;
        try {
            // Only service failures reach the circuit breaker
            student = fetchStudentWithResilience(dto.studentId());
        } catch (NoSuchElementException nse) {
            log.warn("Student with ID {} does not exist", dto.studentId());
            // Student missing → 404, CB never sees it
            throw nse;
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(dto.studentId())
                .courseCode(dto.courseCode())
                .semester(dto.semester())
                .build();

        return repo.save(enrollment);
    }

    /** Update an existing enrollment */
    public Enrollment update(Long id, EnrollmentDTO dto) {
        Enrollment existing = byId(id);

        // Validate student existence only if changed
        if (!existing.getStudentId().equals(dto.studentId())) {
            try {
                StudentDTO student = fetchStudentWithResilience(dto.studentId());
                if (student == null) {
                    throw new NoSuchElementException("Student with ID " + dto.studentId() + " not found");
                }
            } catch (NoSuchElementException nse) {
                throw nse;
            }
        }

        existing.setStudentId(dto.studentId());
        existing.setCourseCode(dto.courseCode());
        existing.setSemester(dto.semester());

        return repo.save(existing);
    }

    /** Delete enrollment */
    public void delete(Long id) {
        Enrollment enrollment = byId(id);
        repo.delete(enrollment);
    }

    /** Aggregated view: enrollment + student details */
    public EnrollmentDetails details(Long id) {
        Enrollment enrollment = byId(id);
        try {
            StudentDTO student = fetchStudentWithResilience(enrollment.getStudentId());
            return new EnrollmentDetails(enrollment, student);
        } catch (NoSuchElementException nse) {
            throw nse; // student missing → 404
        }
    }

    /** Helper: fetch student with circuit breaker + retry */
    @CircuitBreaker(name = "studentsCB", fallbackMethod = "fetchStudentFallback")
    @Retry(name = "studentsRetry")
    public StudentDTO fetchStudentWithResilience(Long studentId) {
        try {
            return studentClient.getStudent(studentId);
        } catch (FeignException.NotFound nf) {
            // StudentService returned 404 → handle in create/update/details
            throw new NoSuchElementException("Student with ID " + studentId + " not found");
        } catch (RetryableException rex) {
            // Network/service down → circuit breaker fallback
            log.error("StudentService retryable exception for id={}: {}", studentId, rex.getMessage());
            throw new IllegalStateException("StudentService unreachable");
        } catch (FeignException.ServiceUnavailable su) {
            log.error("StudentService returned 503 for id={}: {}", studentId, su.getMessage());
            throw new IllegalStateException("StudentService unavailable (503)");
        } catch (FeignException fe) {
            log.error("Unexpected FeignException while fetching student {}: {}", studentId, fe.getMessage());
            throw new IllegalStateException("Failed to contact StudentService");
        }
    }

    /** Circuit breaker fallback when StudentService is down */
    private StudentDTO fetchStudentFallback(Long studentId, Throwable ex) {
        log.error("fetchStudentFallback: Student service unavailable for id={} cause={}", studentId, ex.toString());
        throw new IllegalStateException("Students service unavailable (Circuit Breaker)");
    }

    /** Combined response for details() */
    public record EnrollmentDetails(Enrollment enrollment, StudentDTO student) {}
}
