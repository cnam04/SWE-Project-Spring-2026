package com.cpvt.prereq_visualizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTO used by /api/students/{id}/records endpoints.
public class StudentCourseRecordModel {

	@JsonProperty("record_id")
	private Integer recordId;

	@JsonProperty("student_id")
	private Integer studentId;

	@JsonProperty("course_id")
	private Integer courseId;

	@JsonProperty("course_code")
	private String courseCode;

	private String crn;
	private String title;
	private Integer credits;
	private List<String> attributes;
	private String status;
	private String grade;

	@JsonProperty("semester_taken")
	private String semesterTaken;

	@JsonProperty("year_taken")
	private Integer yearTaken;

	public StudentCourseRecordModel() {
	}

	public StudentCourseRecordModel(
			Integer recordId,
			Integer studentId,
			Integer courseId,
			String courseCode,
			String crn,
			String title,
			Integer credits,
			List<String> attributes,
			String status,
			String grade,
			String semesterTaken,
			Integer yearTaken) {
		this.recordId = recordId;
		this.studentId = studentId;
		this.courseId = courseId;
		this.courseCode = courseCode;
		this.crn = crn;
		this.title = title;
		this.credits = credits;
		this.attributes = attributes;
		this.status = status;
		this.grade = grade;
		this.semesterTaken = semesterTaken;
		this.yearTaken = yearTaken;
	}

	public Integer getRecordId() {
		return recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
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
