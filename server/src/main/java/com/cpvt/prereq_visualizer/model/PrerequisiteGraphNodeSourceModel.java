package com.cpvt.prereq_visualizer.model;

import java.util.List;

// Internal DTO for repository reads when building graph responses.
public class PrerequisiteGraphNodeSourceModel {

	private Integer nodeId;
	private String nodeType;
	private Integer requiredCourseId;
	private String requiredCourseCode;
	private String requiredCourseCrn;
	private String requiredCourseTitle;
	private Integer requiredCourseCredits;
	private List<String> requiredCourseAttributes;
	private Integer requiredCourseRootPrerequisiteNodeId;

	public PrerequisiteGraphNodeSourceModel() {
	}

	public PrerequisiteGraphNodeSourceModel(
			Integer nodeId,
			String nodeType,
			Integer requiredCourseId,
			String requiredCourseCode,
			String requiredCourseCrn,
			String requiredCourseTitle,
			Integer requiredCourseCredits,
			List<String> requiredCourseAttributes,
			Integer requiredCourseRootPrerequisiteNodeId) {
		this.nodeId = nodeId;
		this.nodeType = nodeType;
		this.requiredCourseId = requiredCourseId;
		this.requiredCourseCode = requiredCourseCode;
		this.requiredCourseCrn = requiredCourseCrn;
		this.requiredCourseTitle = requiredCourseTitle;
		this.requiredCourseCredits = requiredCourseCredits;
		this.requiredCourseAttributes = requiredCourseAttributes;
		this.requiredCourseRootPrerequisiteNodeId = requiredCourseRootPrerequisiteNodeId;
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

	public String getRequiredCourseCrn() {
		return requiredCourseCrn;
	}

	public void setRequiredCourseCrn(String requiredCourseCrn) {
		this.requiredCourseCrn = requiredCourseCrn;
	}

	public String getRequiredCourseTitle() {
		return requiredCourseTitle;
	}

	public void setRequiredCourseTitle(String requiredCourseTitle) {
		this.requiredCourseTitle = requiredCourseTitle;
	}

	public Integer getRequiredCourseCredits() {
		return requiredCourseCredits;
	}

	public void setRequiredCourseCredits(Integer requiredCourseCredits) {
		this.requiredCourseCredits = requiredCourseCredits;
	}

	public List<String> getRequiredCourseAttributes() {
		return requiredCourseAttributes;
	}

	public void setRequiredCourseAttributes(List<String> requiredCourseAttributes) {
		this.requiredCourseAttributes = requiredCourseAttributes;
	}

	public Integer getRequiredCourseRootPrerequisiteNodeId() {
		return requiredCourseRootPrerequisiteNodeId;
	}

	public void setRequiredCourseRootPrerequisiteNodeId(Integer requiredCourseRootPrerequisiteNodeId) {
		this.requiredCourseRootPrerequisiteNodeId = requiredCourseRootPrerequisiteNodeId;
	}
}
