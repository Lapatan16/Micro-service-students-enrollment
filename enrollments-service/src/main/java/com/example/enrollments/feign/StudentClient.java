package com.example.enrollments.feign;
import com.example.enrollments.dto.StudentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "students-service", path = "/students")
public interface StudentClient {
    @GetMapping("/{id}")
    StudentDTO getStudent(@PathVariable(name = "id") Long id);
}