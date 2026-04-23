package com.cpvt.prereq_visualizer.model;

// Internal DTO for repository reads when building graph responses.
public class PrerequisiteGraphNodeSourceModel {

	private Integer nodeId;
	private String nodeType;
	private Integer requiredCourseId;
	private String requiredCourseCode;
	private String requiredCourseTitle;

	public PrerequisiteGraphNodeSourceModel() {
	}

	public PrerequisiteGraphNodeSourceModel(
			Integer nodeId,
			String nodeType,
			Integer requiredCourseId,
			String requiredCourseCode,
			String requiredCourseTitle) {
		this.nodeId = nodeId;
		this.nodeType = nodeType;
		this.requiredCourseId = requiredCourseId;
		this.requiredCourseCode = requiredCourseCode;
		this.requiredCourseTitle = requiredCourseTitle;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public Integer getRequiredCourseId() {
		return requiredCourseId;
	}

	public void setRequiredCourseId(Integer requiredCourseId) {
		this.requiredCourseId = requiredCourseId;
	}

	public String getRequiredCourseCode() {
		return requiredCourseCode;
	}

	public void setRequiredCourseCode(String requiredCourseCode) {
		this.requiredCourseCode = requiredCourseCode;
	}

	public String getRequiredCourseTitle() {
		return requiredCourseTitle;
	}

	public void setRequiredCourseTitle(String requiredCourseTitle) {
		this.requiredCourseTitle = requiredCourseTitle;
	}
}
