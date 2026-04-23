package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

// Request model for PATCH /api/students/{id}.
public class StudentPatchRequestModel {

	@JsonProperty("schoolStudentId")
	@JsonAlias("school_student_id")
	private String schoolStudentId;

	private String major;

	public StudentPatchRequestModel() {
	}

	public StudentPatchRequestModel(String schoolStudentId, String major) {
		this.schoolStudentId = schoolStudentId;
		this.major = major;
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
