package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

// Request model for POST /api/students.
public class StudentCreateRequestModel {

	@JsonProperty("user_id")
	@JsonAlias("userId")
	private Integer userId;

	@JsonProperty("schoolStudentId")
	@JsonAlias("school_student_id")
	private String schoolStudentId;

	private String major;

	public StudentCreateRequestModel() {
	}

	public StudentCreateRequestModel(Integer userId, String schoolStudentId, String major) {
		this.userId = userId;
		this.schoolStudentId = schoolStudentId;
		this.major = major;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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
