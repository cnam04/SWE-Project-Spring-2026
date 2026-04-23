package com.cpvt.prereq_visualizer.model;

import java.util.List;

// Response model for GET /api/courses/{id}, including prerequisite tree data.
public class CourseDetailModel extends CourseModel {

	private PrerequisiteTreeNodeModel prerequisiteTree;

	public CourseDetailModel() {
	}

	public CourseDetailModel(
			Integer courseId,
			String courseCode,
			String crn,
			String title,
			Integer credits,
			List<String> attributes,
			PrerequisiteTreeNodeModel prerequisiteTree) {
		super(courseId, courseCode, crn, title, credits, attributes);
		this.prerequisiteTree = prerequisiteTree;
	}

	public PrerequisiteTreeNodeModel getPrerequisiteTree() {
		return prerequisiteTree;
	}

	public void setPrerequisiteTree(PrerequisiteTreeNodeModel prerequisiteTree) {
		this.prerequisiteTree = prerequisiteTree;
	}
}
