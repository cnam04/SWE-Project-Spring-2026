package com.cpvt.prereq_visualizer.model;

// Node DTO for graph responses used by visualization UIs.
public class CourseGraphNodeModel {

	private String id;
	private String type;
	private CourseGraphPositionModel position;
	private Object data;

	public CourseGraphNodeModel() {
	}

	public CourseGraphNodeModel(
			String id,
			String type,
			CourseGraphPositionModel position,
			Object data) {
		this.id = id;
		this.type = type;
		this.position = position;
		this.data = data;
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

	public CourseGraphPositionModel getPosition() {
		return position;
	}

	public void setPosition(CourseGraphPositionModel position) {
		this.position = position;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
