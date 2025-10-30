package com.example.students.web;
import com.example.students.dto.StudentDTO;
import com.example.students.model.Student;
import com.example.students.service.StudentService;
import jakarta.validation.Valid;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Student> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable(name = "id") Long id) {
        try {
            Student student = service.byId(id);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            log.warn("Student not found with id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found with id " + id);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody StudentDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.toList());
            log.warn("Validation failed for POST /students: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Student student = new Student();
            student.setFullName(dto.fullName());
            student.setEmail(dto.email());
            student.setIndexNumber(dto.indexNumber());
            Student created = service.create(student);
            log.info("Created student index={} email={}", dto.indexNumber(), dto.email());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.warn("Failed to create student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable(name = "id") Long id,
                                    @Valid @RequestBody StudentDTO dto,
                                    BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.toList());
            log.warn("Validation failed for PUT /students/{}: {}", id, errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Student updated = service.update(id, dto);
            log.info("Updated student id={}", id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.warn("Failed to update student id={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") Long id) {
        try {
            service.delete(id);
            log.warn("Deleted student id={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.warn("Failed to delete student id={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}