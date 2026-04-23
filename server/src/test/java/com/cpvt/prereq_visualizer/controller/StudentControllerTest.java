package com.cpvt.prereq_visualizer.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.service.StudentConflictException;
import com.cpvt.prereq_visualizer.service.StudentService;
import com.cpvt.prereq_visualizer.service.StudentValidationException;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StudentService studentService;

	@Test
	void getAllStudents_returnsStudents() throws Exception {
		StudentModel student = new StudentModel(
				1,
				1,
				"Cole Nam",
				"cole@example.com",
				"NP100001",
				"Computer Science");

		when(studentService.getAllStudents(null, null, null)).thenReturn(List.of(student));

		mockMvc.perform(get("/api/students"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].student_id").value(1))
				.andExpect(jsonPath("$[0].user_id").value(1))
				.andExpect(jsonPath("$[0].name").value("Cole Nam"))
				.andExpect(jsonPath("$[0].email").value("cole@example.com"))
				.andExpect(jsonPath("$[0].school_student_id").value("NP100001"))
				.andExpect(jsonPath("$[0].major").value("Computer Science"));
	}

	@Test
	void getAllStudents_withFilters_passesFiltersToService() throws Exception {
		when(studentService.getAllStudents("cole", "NP100001", "example.com")).thenReturn(List.of());

		mockMvc.perform(get("/api/students")
						.param("name", "cole")
						.param("schoolStudentId", "NP100001")
						.param("email", "example.com"))
				.andExpect(status().isOk());

		verify(studentService).getAllStudents(eq("cole"), eq("NP100001"), eq("example.com"));
	}

	@Test
	void getStudentById_returnsStudent() throws Exception {
		StudentModel student = new StudentModel(
				1,
				1,
				"Cole Nam",
				"cole@example.com",
				"NP100001",
				"Computer Science");

		when(studentService.getStudentById(1)).thenReturn(Optional.of(student));

		mockMvc.perform(get("/api/students/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.student_id").value(1))
				.andExpect(jsonPath("$.user_id").value(1))
				.andExpect(jsonPath("$.name").value("Cole Nam"));
	}

	@Test
	void getStudentById_whenMissing_returns404() throws Exception {
		when(studentService.getStudentById(999)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/students/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createStudent_returnsCreatedStudent() throws Exception {
		StudentModel created = new StudentModel(
				2,
				4,
				"Student User",
				"student@example.com",
				"NP100042",
				"Computer Science");

		when(studentService.createStudent(any())).thenReturn(created);

		String requestBody = """
				{
				  "user_id": 4,
				  "schoolStudentId": "NP100042",
				  "major": "Computer Science"
				}
				""";

		mockMvc.perform(post("/api/students")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.student_id").value(2))
				.andExpect(jsonPath("$.user_id").value(4))
				.andExpect(jsonPath("$.school_student_id").value("NP100042"));
	}

	@Test
	void createStudent_whenValidationFails_returns400() throws Exception {
		when(studentService.createStudent(any()))
				.thenThrow(new StudentValidationException("Referenced user not found: 999"));

		String requestBody = """
				{
				  "user_id": 999,
				  "schoolStudentId": "NP100042"
				}
				""";

		mockMvc.perform(post("/api/students")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createStudent_whenConflictOccurs_returns409() throws Exception {
		when(studentService.createStudent(any()))
				.thenThrow(new StudentConflictException("school_student_id already exists: NP100001"));

		String requestBody = """
				{
				  "user_id": 4,
				  "schoolStudentId": "NP100001"
				}
				""";

		mockMvc.perform(post("/api/students")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict());
	}

	@Test
	void updateStudent_returnsUpdatedStudent() throws Exception {
		StudentModel updated = new StudentModel(
				1,
				1,
				"Cole Nam",
				"cole@example.com",
				"NP100001",
				"Software Engineering");

		when(studentService.updateStudent(any(), any())).thenReturn(Optional.of(updated));

		String requestBody = """
				{
				  "major": "Software Engineering"
				}
				""";

		mockMvc.perform(patch("/api/students/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.student_id").value(1))
				.andExpect(jsonPath("$.major").value("Software Engineering"));
	}

	@Test
	void updateStudent_whenMissing_returns404() throws Exception {
		when(studentService.updateStudent(any(), any())).thenReturn(Optional.empty());

		mockMvc.perform(patch("/api/students/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateStudent_whenValidationFails_returns400() throws Exception {
		when(studentService.updateStudent(any(), any()))
				.thenThrow(new StudentValidationException("At least one updatable field must be provided"));

		mockMvc.perform(patch("/api/students/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateStudent_whenConflictOccurs_returns409() throws Exception {
		when(studentService.updateStudent(any(), any()))
				.thenThrow(new StudentConflictException("school_student_id already exists: NP100001"));

		String requestBody = """
				{
				  "schoolStudentId": "NP100001"
				}
				""";

		mockMvc.perform(patch("/api/students/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict());
	}
}
