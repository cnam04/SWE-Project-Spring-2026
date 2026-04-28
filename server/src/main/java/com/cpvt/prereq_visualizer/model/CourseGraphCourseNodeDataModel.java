package com.cpvt.prereq_visualizer.model;

import java.util.List;

// Payload for graph nodes of type courseNode.
public class CourseGraphCourseNodeDataModel {

	private String kind;
	private Integer courseId;
	private String courseCode;
	private String crn;
	private String title;
	private Integer credits;
	private List<String> attributes;
	private String status;
	private Boolean isTargetCourse;

	public CourseGraphCourseNodeDataModel() {
	}

	public CourseGraphCourseNodeDataModel(
			String kind,
			Integer courseId,
			String courseCode,
			String crn,
			String title,
			Integer credits,
			List<String> attributes,
			String status,
			Boolean isTargetCourse) {
		this.kind = kind;
		this.courseId = courseId;
		this.courseCode = courseCode;
		this.crn = crn;
		this.title = title;
		this.credits = credits;
		this.attributes = attributes;
		this.status = status;
		this.isTargetCourse = isTargetCourse;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
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

	public Boolean getIsTargetCourse() {
		return isTargetCourse;
	}

	public void setIsTargetCourse(Boolean isTargetCourse) {
		this.isTargetCourse = isTargetCourse;
	}
}
