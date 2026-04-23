package com.cpvt.prereq_visualizer.model;

// Edge DTO for graph responses used by visualization UIs.
public class CourseGraphEdgeModel {

	private String source;
	private String target;

	public CourseGraphEdgeModel() {
	}

	public CourseGraphEdgeModel(String source, String target) {
		this.source = source;
		this.target = target;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
