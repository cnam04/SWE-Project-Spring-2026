package com.cpvt.prereq_visualizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

// DTO used by GET /api/courses/{id}/graph.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseGraphModel {

	private Integer courseId;
	private String courseCode;
	private String title;
	private Integer studentId;
	private String statusMode;
	private String layoutDirection;
	private List<CourseGraphNodeModel> nodes;
	private List<CourseGraphEdgeModel> edges;

	public CourseGraphModel() {
	}

	public CourseGraphModel(
			Integer courseId,
			String courseCode,
			String title,
			Integer studentId,
			String statusMode,
			String layoutDirection,
			List<CourseGraphNodeModel> nodes,
			List<CourseGraphEdgeModel> edges) {
		this.courseId = courseId;
		this.courseCode = courseCode;
		this.title = title;
		this.studentId = studentId;
		this.statusMode = statusMode;
		this.layoutDirection = layoutDirection;
		this.nodes = nodes;
		this.edges = edges;
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

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public String getStatusMode() {
		return statusMode;
	}

	public void setStatusMode(String statusMode) {
		this.statusMode = statusMode;
	}

	public String getLayoutDirection() {
		return layoutDirection;
	}

	public void setLayoutDirection(String layoutDirection) {
		this.layoutDirection = layoutDirection;
	}

	public List<CourseGraphNodeModel> getNodes() {
		return nodes;
	}

	public void setNodes(List<CourseGraphNodeModel> nodes) {
		this.nodes = nodes;
	}

	public List<CourseGraphEdgeModel> getEdges() {
		return edges;
	}

	public void setEdges(List<CourseGraphEdgeModel> edges) {
		this.edges = edges;
	}
}
