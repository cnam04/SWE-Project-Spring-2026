package com.cpvt.prereq_visualizer.model;

// Edge DTO for graph responses used by visualization UIs.
public class CourseGraphEdgeModel {

	private String id;
	private String source;
	private String target;
	private String type;
	private CourseGraphEdgeDataModel data;

	public CourseGraphEdgeModel() {
	}

	public CourseGraphEdgeModel(String id, String source, String target, String type, CourseGraphEdgeDataModel data) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.type = type;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CourseGraphEdgeDataModel getData() {
		return data;
	}

	public void setData(CourseGraphEdgeDataModel data) {
		this.data = data;
	}
}
