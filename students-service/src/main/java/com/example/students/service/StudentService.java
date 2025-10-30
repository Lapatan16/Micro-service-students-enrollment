package com.example.students.service;
import com.example.students.dto.StudentDTO;
import com.example.students.model.Student;
import com.example.students.repo.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class StudentService {
    private final StudentRepository repo;
    public StudentService(StudentRepository repo) { this.repo = repo; }
    public List<Student> all() { return repo.findAll(); }
    public Student byId(Long id) { return repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Student not found")); }
    public Student create(Student s) {
        repo.findByEmail(s.getEmail()).ifPresent(x -> { throw new
                ResponseStatusException(HttpStatus.CONFLICT, "Email already exists"); });
        repo.findByIndexNumber(s.getIndexNumber()).ifPresent(x -> { throw new
                ResponseStatusException(HttpStatus.CONFLICT, "Index already exists"); });
        return repo.save(s);
    }
    public Student update(Long id, StudentDTO dto) {
        var s = byId(id);
        s.setFullName(dto.fullName());
        s.setEmail(dto.email());
        s.setIndexNumber(dto.indexNumber());
        return repo.save(s);
    }
    public void delete(Long id) { repo.delete(byId(id)); }
}