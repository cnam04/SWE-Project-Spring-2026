package com.cpvt.prereq_visualizer.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTO used by GET /api/users/ summary responses.
public class UserModel {

	@JsonProperty("user_id")
	private Integer userId;

	private String name;
	private String email;
	private String role;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("linked_student_id")
	private Integer linkedStudentId;

	@JsonProperty("school_student_id")
	private String schoolStudentId;

	public UserModel() {
	}

	public UserModel(
			Integer userId,
			String name,
			String email,
			String role,
			LocalDateTime createdAt,
			Integer linkedStudentId,
			String schoolStudentId) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
		this.createdAt = createdAt;
		this.linkedStudentId = linkedStudentId;
		this.schoolStudentId = schoolStudentId;
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

	public Integer getLinkedStudentId() {
		return linkedStudentId;
	}

	public void setLinkedStudentId(Integer linkedStudentId) {
		this.linkedStudentId = linkedStudentId;
	}

	public String getSchoolStudentId() {
		return schoolStudentId;
	}

	public void setSchoolStudentId(String schoolStudentId) {
		this.schoolStudentId = schoolStudentId;
	}
}