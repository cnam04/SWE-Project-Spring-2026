package com.cpvt.prereq_visualizer.model;

// Payload for graph nodes of type operatorNode.
public class CourseGraphOperatorNodeDataModel {

	private String kind;
	private Integer prerequisiteNodeId;
	private String operator;
	private String label;

	public CourseGraphOperatorNodeDataModel() {
	}

	public CourseGraphOperatorNodeDataModel(
			String kind,
			Integer prerequisiteNodeId,
			String operator,
			String label) {
		this.kind = kind;
		this.prerequisiteNodeId = prerequisiteNodeId;
		this.operator = operator;
		this.label = label;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Integer getPrerequisiteNodeId() {
		return prerequisiteNodeId;
	}

	public void setPrerequisiteNodeId(Integer prerequisiteNodeId) {
		this.prerequisiteNodeId = prerequisiteNodeId;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
