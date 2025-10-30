package com.example.students.web;

import com.example.students.model.Student;
import com.example.students.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService service;

    @Test
    void shouldReturnStudentById() throws Exception {
        Student mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setFullName("Ana Petrovic");
        mockStudent.setEmail("ana@example.com");
        mockStudent.setIndexNumber("2025-001");

        given(service.byId(1L)).willReturn(mockStudent);

        mockMvc.perform(get("/students/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ana Petrovic"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.indexNumber").value("2025-001"));
    }
}
