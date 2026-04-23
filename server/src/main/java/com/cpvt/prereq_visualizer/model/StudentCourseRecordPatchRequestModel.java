package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

// Request model for PATCH /api/students/{id}/records/{recordId}.
public class StudentCourseRecordPatchRequestModel {

	private String status;
	private boolean hasStatus;

	private String grade;
	private boolean hasGrade;

	private String semesterTaken;
	private boolean hasSemesterTaken;

	private Integer yearTaken;
	private boolean hasYearTaken;

	public StudentCourseRecordPatchRequestModel() {
	}

	public StudentCourseRecordPatchRequestModel(
			String status,
			String grade,
			String semesterTaken,
			Integer yearTaken) {
		this.status = status;
		this.hasStatus = true;
		this.grade = grade;
		this.hasGrade = true;
		this.semesterTaken = semesterTaken;
		this.hasSemesterTaken = true;
		this.yearTaken = yearTaken;
		this.hasYearTaken = true;
	}

	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
		this.hasStatus = true;
	}

	public boolean hasStatus() {
		return hasStatus;
	}

	public String getGrade() {
		return grade;
	}

	@JsonProperty("grade")
	public void setGrade(String grade) {
		this.grade = grade;
		this.hasGrade = true;
	}

	public boolean hasGrade() {
		return hasGrade;
	}

	public String getSemesterTaken() {
		return semesterTaken;
	}

	@JsonProperty("semester_taken")
	@JsonAlias("semesterTaken")
	public void setSemesterTaken(String semesterTaken) {
		this.semesterTaken = semesterTaken;
		this.hasSemesterTaken = true;
	}

	public boolean hasSemesterTaken() {
		return hasSemesterTaken;
	}

	public Integer getYearTaken() {
		return yearTaken;
	}

	@JsonProperty("year_taken")
	@JsonAlias("yearTaken")
	public void setYearTaken(Integer yearTaken) {
		this.yearTaken = yearTaken;
		this.hasYearTaken = true;
	}

	public boolean hasYearTaken() {
		return hasYearTaken;
	}
}
