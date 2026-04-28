package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserStudentModel {

	@JsonProperty("student_id")
	private Integer studentId;

	@JsonProperty("school_student_id")
	private String schoolStudentId;

	private String major;

	public UserStudentModel() {
	}

	public UserStudentModel(Integer studentId, String schoolStudentId, String major) {
		this.studentId = studentId;
		this.schoolStudentId = schoolStudentId;
		this.major = major;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
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
