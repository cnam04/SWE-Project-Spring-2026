package com.cpvt.prereq_visualizer.model;

// Payload for graph edges.
public class CourseGraphEdgeDataModel {

	private String relationship;

	public CourseGraphEdgeDataModel() {
	}

	public CourseGraphEdgeDataModel(String relationship) {
		this.relationship = relationship;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
}
