package com.cpvt.prereq_visualizer.model;

// Request model for PUT /api/courses/{id}/prerequisites.
public class CoursePrerequisitesUpdateRequestModel {

	private PrerequisiteTreeNodeModel prerequisiteTree;

	public CoursePrerequisitesUpdateRequestModel() {
	}

	public CoursePrerequisitesUpdateRequestModel(PrerequisiteTreeNodeModel prerequisiteTree) {
		this.prerequisiteTree = prerequisiteTree;
	}

	public PrerequisiteTreeNodeModel getPrerequisiteTree() {
		return prerequisiteTree;
	}

	public void setPrerequisiteTree(PrerequisiteTreeNodeModel prerequisiteTree) {
		this.prerequisiteTree = prerequisiteTree;
	}
}
