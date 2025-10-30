package com.example.students.repo;

import com.example.students.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository repo;

    @Test
    void shouldSaveAndFindByEmail() {
        Student s = new Student();
        s.setFullName("Marko Nikolic");
        s.setEmail("marko@example.com");
        s.setIndexNumber("2025-002");

        repo.save(s);

        Optional<Student> found = repo.findByEmail("marko@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getIndexNumber()).isEqualTo("2025-002");
    }
}
