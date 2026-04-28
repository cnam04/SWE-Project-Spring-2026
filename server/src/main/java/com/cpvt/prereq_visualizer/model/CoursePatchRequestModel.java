package com.cpvt.prereq_visualizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

// Request model for PATCH /api/courses/{id}.
public class CoursePatchRequestModel {

	@JsonProperty("course_code")
	private String courseCode;

	private String crn;
	private String title;
	private Integer credits;
	private List<String> attributes;

	public CoursePatchRequestModel() {
	}

	public CoursePatchRequestModel(String courseCode, String crn, String title, Integer credits, List<String> attributes) {
		this.courseCode = courseCode;
		this.crn = crn;
		this.title = title;
		this.credits = credits;
		this.attributes = attributes;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getCredits() {
		return credits;
	}

	public void setCredits(Integer credits) {
		this.credits = credits;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
}
