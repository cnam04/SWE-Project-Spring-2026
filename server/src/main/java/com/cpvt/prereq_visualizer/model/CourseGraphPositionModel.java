package com.cpvt.prereq_visualizer.model;

// React Flow node position container; backend returns origin and frontend computes layout.
public class CourseGraphPositionModel {

	private Integer x;
	private Integer y;

	public CourseGraphPositionModel() {
	}

	public CourseGraphPositionModel(Integer x, Integer y) {
		this.x = x;
		this.y = y;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}
}
