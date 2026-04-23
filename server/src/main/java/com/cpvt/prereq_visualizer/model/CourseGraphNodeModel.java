package com.cpvt.prereq_visualizer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

// Node DTO for graph responses used by visualization UIs.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseGraphNodeModel {

	private String id;
	private String type;
	private Integer courseId;
	private String courseCode;
	private String title;
	private String status;
	private Integer depth;

	public CourseGraphNodeModel() {
	}

	public CourseGraphNodeModel(
			String id,
			String type,
			Integer courseId,
			String courseCode,
			String title,
			String status,
			Integer depth) {
		this.id = id;
		this.type = type;
		this.courseId = courseId;
		this.courseCode = courseCode;
		this.title = title;
		this.status = status;
		this.depth = depth;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}
}
