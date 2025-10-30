package com.example.students.repo;
import com.example.students.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByIndexNumber(String indexNumber);
}
