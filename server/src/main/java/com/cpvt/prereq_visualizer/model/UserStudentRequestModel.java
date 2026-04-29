package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserStudentRequestModel {

	@JsonProperty("schoolStudentId")
	private String schoolStudentId;

	private String major;

	public UserStudentRequestModel() {
	}

	public UserStudentRequestModel(String schoolStudentId, String major) {
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
