package com.cpvt.prereq_visualizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

// DTO used by GET /api/courses/{id}/graph.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseGraphModel {

	private Integer courseId;
	private String courseCode;
	private Integer studentId;
	private List<CourseGraphNodeModel> nodes;
	private List<CourseGraphEdgeModel> edges;

	public CourseGraphModel() {
	}

	public CourseGraphModel(
			Integer courseId,
			String courseCode,
			Integer studentId,
			List<CourseGraphNodeModel> nodes,
			List<CourseGraphEdgeModel> edges) {
		this.courseId = courseId;
		this.courseCode = courseCode;
		this.studentId = studentId;
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

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
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
