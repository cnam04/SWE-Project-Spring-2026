package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

// Request model for POST /api/students/{id}/records.
public class StudentCourseRecordCreateRequestModel {

	@JsonProperty("course_id")
	@JsonAlias("courseId")
	private Integer courseId;

	private String status;
	private String grade;

	@JsonProperty("semester_taken")
	@JsonAlias("semesterTaken")
	private String semesterTaken;

	@JsonProperty("year_taken")
	@JsonAlias("yearTaken")
	private Integer yearTaken;

	public StudentCourseRecordCreateRequestModel() {
	}

	public StudentCourseRecordCreateRequestModel(
			Integer courseId,
			String status,
			String grade,
			String semesterTaken,
			Integer yearTaken) {
		this.courseId = courseId;
		this.status = status;
		this.grade = grade;
		this.semesterTaken = semesterTaken;
		this.yearTaken = yearTaken;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getSemesterTaken() {
		return semesterTaken;
	}

	public void setSemesterTaken(String semesterTaken) {
		this.semesterTaken = semesterTaken;
	}

	public Integer getYearTaken() {
		return yearTaken;
	}

	public void setYearTaken(Integer yearTaken) {
		this.yearTaken = yearTaken;
	}
}
