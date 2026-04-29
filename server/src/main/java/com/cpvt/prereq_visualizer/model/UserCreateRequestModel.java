package com.cpvt.prereq_visualizer.model;

// Request model for POST /api/users.
public class UserCreateRequestModel {

	private String name;
	private String email;
	private String password;
	private String role;
	private UserStudentRequestModel student;

	public UserCreateRequestModel() {
	}

	public UserCreateRequestModel(
			String name,
			String email,
			String password,
			String role,
			UserStudentRequestModel student) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.student = student;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public UserStudentRequestModel getStudent() {
		return student;
	}

	public void setStudent(UserStudentRequestModel student) {
		this.student = student;
	}
}
