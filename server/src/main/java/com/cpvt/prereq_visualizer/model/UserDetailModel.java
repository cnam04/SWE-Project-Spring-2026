package com.cpvt.prereq_visualizer.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

// Response model for GET /api/users/{id} and user mutation endpoints.
public class UserDetailModel {

	@JsonProperty("user_id")
	private Integer userId;

	private String name;
	private String email;
	private String role;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	private UserStudentModel student;

	public UserDetailModel() {
	}

	public UserDetailModel(
			Integer userId,
			String name,
			String email,
			String role,
			LocalDateTime createdAt,
			UserStudentModel student) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
		this.createdAt = createdAt;
		this.student = student;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public UserStudentModel getStudent() {
		return student;
	}

	public void setStudent(UserStudentModel student) {
		this.student = student;
	}
}
