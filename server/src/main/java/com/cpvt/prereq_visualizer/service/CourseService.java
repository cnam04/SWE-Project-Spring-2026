package com.cpvt.prereq_visualizer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.CourseWithRootPrerequisiteModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;
import com.cpvt.prereq_visualizer.repository.CourseRepository;

@Service
public class CourseService {

	private final CourseRepository courseRepository;

	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	// Kept as a service boundary so validation/business rules can be added later.
	public List<CourseModel> getAllCourses() {
		return courseRepository.findAllCourses();
	}

	public Optional<CourseDetailModel> getCourseById(Integer courseId) {
		Optional<CourseWithRootPrerequisiteModel> course = courseRepository.findCourseWithRootPrerequisiteById(courseId);
		if (course.isEmpty()) {
			return Optional.empty();
		}

		CourseWithRootPrerequisiteModel source = course.get();
		PrerequisiteTreeNodeModel prerequisiteTree = buildPrerequisiteTree(source.getRootPrerequisiteNodeId(), new HashSet<>());

		CourseDetailModel detail = new CourseDetailModel(
				source.getCourseId(),
				source.getCourseCode(),
				source.getCrn(),
				source.getTitle(),
				source.getCredits(),
				source.getAttributes(),
				prerequisiteTree);

		return Optional.of(detail);
	}

	private PrerequisiteTreeNodeModel buildPrerequisiteTree(Integer nodeId, Set<Integer> recursionPath) {
		if (nodeId == null) {
			return null;
		}

		if (!recursionPath.add(nodeId)) {
			throw new IllegalStateException("Cycle detected in prerequisite tree at node " + nodeId);
		}

		try {
			PrerequisiteTreeNodeModel currentNode = courseRepository.findPrerequisiteNodeById(nodeId)
					.orElseThrow(() -> new IllegalStateException("Prerequisite node not found: " + nodeId));

			if (!"COURSE".equals(currentNode.getType())) {
				List<PrerequisiteTreeNodeModel> children = courseRepository.findChildNodeIds(nodeId).stream()
						.map(childId -> buildPrerequisiteTree(childId, recursionPath))
						.toList();

				currentNode.setChildren(children);
			}

			return currentNode;
		} finally {
			recursionPath.remove(nodeId);
		}
	}
}
