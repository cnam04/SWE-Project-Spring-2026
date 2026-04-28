package com.cpvt.prereq_visualizer.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserModel;
import com.cpvt.prereq_visualizer.model.UserStudentModel;
import com.cpvt.prereq_visualizer.service.UserConflictException;
import com.cpvt.prereq_visualizer.service.UserService;
import com.cpvt.prereq_visualizer.service.UserValidationException;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void getAllUsers_returnsUserSummaries() throws Exception {
		UserModel user = new UserModel(
				1,
				"Cole Nam",
				"cole@example.com",
				"student",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				1,
				"NP100001");

		when(userService.getAllUsers()).thenReturn(List.of(user));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].user_id").value(1))
				.andExpect(jsonPath("$[0].name").value("Cole Nam"))
				.andExpect(jsonPath("$[0].email").value("cole@example.com"))
				.andExpect(jsonPath("$[0].role").value("student"))
				.andExpect(jsonPath("$[0].linked_student_id").value(1))
				.andExpect(jsonPath("$[0].school_student_id").value("NP100001"));
	}

	@Test
	void getUserById_returnsUserDetail() throws Exception {
		UserDetailModel detail = new UserDetailModel(
				1,
				"Cole Nam",
				"cole@example.com",
				"student",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				new UserStudentModel(1, "NP100001", "Computer Science"));

		when(userService.getUserById(1)).thenReturn(Optional.of(detail));

		mockMvc.perform(get("/api/users/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user_id").value(1))
				.andExpect(jsonPath("$.name").value("Cole Nam"))
				.andExpect(jsonPath("$.email").value("cole@example.com"))
				.andExpect(jsonPath("$.role").value("student"))
				.andExpect(jsonPath("$.created_at").exists())
				.andExpect(jsonPath("$.student.student_id").value(1))
				.andExpect(jsonPath("$.student.school_student_id").value("NP100001"))
				.andExpect(jsonPath("$.student.major").value("Computer Science"));
	}

	@Test
	void getUserById_whenMissing_returns404() throws Exception {
		when(userService.getUserById(999)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/users/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createUser_returnsCreatedUser() throws Exception {
		UserDetailModel created = new UserDetailModel(
				7,
				"Cole Nam",
				"cole@example.com",
				"student",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				new UserStudentModel(4, "NP100001", "Computer Science"));

		when(userService.createUser(any())).thenReturn(created);

		String requestBody = """
				{
				  "name": "Cole Nam",
				  "email": "cole@example.com",
				  "password": "plain-password",
				  "role": "student",
				  "student": {
				    "schoolStudentId": "NP100001",
				    "major": "Computer Science"
				  }
				}
				""";

		mockMvc.perform(post("/api/users/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user_id").value(7))
				.andExpect(jsonPath("$.email").value("cole@example.com"))
				.andExpect(jsonPath("$.student.student_id").value(4));
	}

	@Test
	void createUser_whenValidationFails_returns400() throws Exception {
		when(userService.createUser(any()))
				.thenThrow(new UserValidationException("email format is invalid"));

		String requestBody = """
				{
				  "name": "Cole Nam",
				  "email": "bad-email",
				  "password": "plain-password",
				  "role": "student"
				}
				""";

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createUser_whenConflictOccurs_returns409() throws Exception {
		when(userService.createUser(any()))
				.thenThrow(new UserConflictException("Email already exists: cole@example.com"));

		String requestBody = """
				{
				  "name": "Cole Nam",
				  "email": "cole@example.com",
				  "password": "plain-password",
				  "role": "student"
				}
				""";

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict());
	}

	@Test
	void updateUser_returnsUpdatedUser() throws Exception {
		UserDetailModel updated = new UserDetailModel(
				2,
				"Dr. Advisor",
				"advisor-updated@example.com",
				"advisor",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				null);

		when(userService.updateUser(any(), any())).thenReturn(Optional.of(updated));

		String requestBody = """
				{
				  "email": "advisor-updated@example.com"
				}
				""";

		mockMvc.perform(patch("/api/users/2")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user_id").value(2))
				.andExpect(jsonPath("$.email").value("advisor-updated@example.com"));
	}

	@Test
	void updateUser_whenMissing_returns404() throws Exception {
		when(userService.updateUser(any(), any())).thenReturn(Optional.empty());

		mockMvc.perform(patch("/api/users/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateUser_whenValidationFails_returns400() throws Exception {
		when(userService.updateUser(any(), any()))
				.thenThrow(new UserValidationException("At least one updatable field must be provided"));

		mockMvc.perform(patch("/api/users/2")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateUser_whenConflictOccurs_returns409() throws Exception {
		when(userService.updateUser(any(), any()))
				.thenThrow(new UserConflictException("Email already exists: advisor@example.com"));

		String requestBody = """
				{
				  "email": "advisor@example.com"
				}
				""";

		mockMvc.perform(patch("/api/users/2")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict());
	}

	@Test
	void deleteUser_whenExists_returns204() throws Exception {
		when(userService.deleteUser(3)).thenReturn(true);

		mockMvc.perform(delete("/api/users/3"))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteUser_whenMissing_returns404() throws Exception {
		when(userService.deleteUser(999)).thenReturn(false);

		mockMvc.perform(delete("/api/users/999"))
				.andExpect(status().isNotFound());
	}
}