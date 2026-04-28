package com.cpvt.prereq_visualizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrerequisiteTreeNodeModel {

	private String type;
	private String courseCode;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<PrerequisiteTreeNodeModel> children;

	public PrerequisiteTreeNodeModel() {
	}

	public PrerequisiteTreeNodeModel(String type, String courseCode, List<PrerequisiteTreeNodeModel> children) {
		this.type = type;
		this.courseCode = courseCode;
		this.children = children;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public List<PrerequisiteTreeNodeModel> getChildren() {
		return children;
	}

	public void setChildren(List<PrerequisiteTreeNodeModel> children) {
		this.children = children;
	}
}
