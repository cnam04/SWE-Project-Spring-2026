package com.cpvt.prereq_visualizer.model;

import java.util.List;

// Internal data shape used between repository and service for course detail assembly.
public class CourseWithRootPrerequisiteModel extends CourseModel {

	private Integer rootPrerequisiteNodeId;

	public CourseWithRootPrerequisiteModel() {
	}

	public CourseWithRootPrerequisiteModel(
			Integer courseId,
			String courseCode,
			String crn,
			String title,
			Integer credits,
			List<String> attributes,
			Integer rootPrerequisiteNodeId) {
		super(courseId, courseCode, crn, title, credits, attributes);
		this.rootPrerequisiteNodeId = rootPrerequisiteNodeId;
	}

	public Integer getRootPrerequisiteNodeId() {
		return rootPrerequisiteNodeId;
	}

	public void setRootPrerequisiteNodeId(Integer rootPrerequisiteNodeId) {
		this.rootPrerequisiteNodeId = rootPrerequisiteNodeId;
	}
}
