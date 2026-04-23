package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTO used by GET /api/students/ and GET /api/students/{id} responses.
public class StudentModel {

	@JsonProperty("student_id")
	private Integer studentId;

	@JsonProperty("user_id")
	private Integer userId;

	private String name;
	private String email;

	@JsonProperty("school_student_id")
	private String schoolStudentId;

	private String major;

	public StudentModel() {
	}

	public StudentModel(
			Integer studentId,
			Integer userId,
			String name,
			String email,
			String schoolStudentId,
			String major) {
		this.studentId = studentId;
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.schoolStudentId = schoolStudentId;
		this.major = major;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
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

	public String getSchoolStudentId() {
		return schoolStudentId;
	}

	public void setSchoolStudentId(String schoolStudentId) {
		this.schoolStudentId = schoolStudentId;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}
}
