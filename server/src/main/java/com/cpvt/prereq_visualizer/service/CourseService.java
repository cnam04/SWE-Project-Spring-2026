package com.cpvt.prereq_visualizer.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpvt.prereq_visualizer.model.CourseCreateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.CoursePatchRequestModel;
import com.cpvt.prereq_visualizer.model.CoursePrerequisitesUpdateRequestModel;
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

	@Transactional
	public CourseDetailModel createCourse(CourseCreateRequestModel request) {
		CourseCreateCommand command = validateAndNormalizeCreateRequest(request);
		String normalizedNewCourseCode = normalizeCourseCodeForLookup(command.courseCode());

		if (courseRepository.findCourseIdByCourseCode(command.courseCode()).isPresent()) {
			throw new CourseConflictException("Course code already exists: " + command.courseCode());
		}

		Map<String, Integer> resolvedPrerequisiteCourseIds = validateAndResolvePrerequisiteTree(
				command.prerequisiteTree(),
				normalizedNewCourseCode);

		Integer courseId = courseRepository.insertCourse(
				command.courseCode(),
				command.crn(),
				command.title(),
				command.credits(),
				command.attributes());

		if (command.prerequisiteTree() != null) {
			Integer rootNodeId = persistPrerequisiteTree(courseId, command.prerequisiteTree(), resolvedPrerequisiteCourseIds);
			courseRepository.updateCourseRootPrerequisiteNodeId(courseId, rootNodeId);
		}

		return getCourseById(courseId)
				.orElseThrow(() -> new IllegalStateException("Created course could not be loaded: " + courseId));
	}

	@Transactional
	public Optional<CourseModel> updateCourse(Integer courseId, CoursePatchRequestModel request) {
		Optional<CourseWithRootPrerequisiteModel> existingCourse = courseRepository.findCourseWithRootPrerequisiteById(courseId);
		if (existingCourse.isEmpty()) {
			return Optional.empty();
		}

		CoursePatchCommand command = validateAndNormalizePatchRequest(request);
		CourseWithRootPrerequisiteModel currentCourse = existingCourse.get();

		String nextCourseCode = command.hasCourseCode() ? command.courseCode() : currentCourse.getCourseCode();
		String nextCrn = command.hasCrn() ? command.crn() : currentCourse.getCrn();
		String nextTitle = command.hasTitle() ? command.title() : currentCourse.getTitle();
		Integer nextCredits = command.hasCredits() ? command.credits() : currentCourse.getCredits();
		List<String> nextAttributes = command.hasAttributes() ? command.attributes() : currentCourse.getAttributes();

		if (command.hasCourseCode()) {
			Optional<Integer> existingCourseIdForCode = courseRepository.findCourseIdByCourseCode(nextCourseCode);
			if (existingCourseIdForCode.isPresent() && !courseId.equals(existingCourseIdForCode.get())) {
				throw new CourseConflictException("Course code already exists: " + nextCourseCode);
			}
		}

		int updatedRows = courseRepository.updateCourseSummaryFields(
				courseId,
				nextCourseCode,
				nextCrn,
				nextTitle,
				nextCredits,
				nextAttributes);

		if (updatedRows == 0) {
			return Optional.empty();
		}

		return courseRepository.findCourseWithRootPrerequisiteById(courseId)
				.map(this::toCourseSummaryModel);
	}

	@Transactional
	public Optional<CourseDetailModel> updateCoursePrerequisites(
			Integer courseId,
			CoursePrerequisitesUpdateRequestModel request) {
		Optional<CourseWithRootPrerequisiteModel> existingCourse = courseRepository.findCourseWithRootPrerequisiteById(courseId);
		if (existingCourse.isEmpty()) {
			return Optional.empty();
		}

		PrerequisiteTreeNodeModel nextPrerequisiteTree = validateAndNormalizePrerequisitesUpdateRequest(request);
		String normalizedCourseCode = normalizeCourseCodeForLookup(existingCourse.get().getCourseCode());
		Map<String, Integer> resolvedPrerequisiteCourseIds = validateAndResolvePrerequisiteTree(
				nextPrerequisiteTree,
				normalizedCourseCode);

		courseRepository.updateCourseRootPrerequisiteNodeId(courseId, null);
		courseRepository.deletePrerequisiteNodesByCourseId(courseId);

		if (nextPrerequisiteTree != null) {
			Integer rootNodeId = persistPrerequisiteTree(courseId, nextPrerequisiteTree, resolvedPrerequisiteCourseIds);
			courseRepository.updateCourseRootPrerequisiteNodeId(courseId, rootNodeId);
		}

		return getCourseById(courseId);
	}

	private Map<String, Integer> validateAndResolvePrerequisiteTree(
			PrerequisiteTreeNodeModel prerequisiteTree,
			String normalizedCourseCode) {
		Map<String, Integer> resolvedPrerequisiteCourseIds = new HashMap<>();
		if (prerequisiteTree != null) {
			validatePrerequisiteTree(prerequisiteTree, normalizedCourseCode, resolvedPrerequisiteCourseIds);
			ensureNoIndirectCycle(normalizedCourseCode, resolvedPrerequisiteCourseIds.values());
		}

		return resolvedPrerequisiteCourseIds;
	}

	private PrerequisiteTreeNodeModel validateAndNormalizePrerequisitesUpdateRequest(
			CoursePrerequisitesUpdateRequestModel request) {
		if (request == null) {
			throw new CourseValidationException("Request body is required");
		}

		return request.getPrerequisiteTree();
	}

	private Integer persistPrerequisiteTree(
			Integer courseId,
			PrerequisiteTreeNodeModel node,
			Map<String, Integer> resolvedPrerequisiteCourseIds) {
		String nodeType = normalizeNodeType(node.getType());
		Integer requiredCourseId = null;

		if ("COURSE".equals(nodeType)) {
			String normalizedRequiredCourseCode = normalizeCourseCodeForLookup(node.getCourseCode());
			requiredCourseId = resolvedPrerequisiteCourseIds.get(normalizedRequiredCourseCode);
			if (requiredCourseId == null) {
				throw new IllegalStateException("Missing resolved course id for prerequisite: " + node.getCourseCode());
			}
		}

		Integer nodeId = courseRepository.insertPrerequisiteNode(courseId, nodeType, requiredCourseId);

		if (!"COURSE".equals(nodeType)) {
			List<PrerequisiteTreeNodeModel> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				Integer childNodeId = persistPrerequisiteTree(courseId, children.get(i), resolvedPrerequisiteCourseIds);
				courseRepository.insertPrerequisiteEdge(nodeId, childNodeId, i + 1);
			}
		}

		return nodeId;
	}

	private void validatePrerequisiteTree(
			PrerequisiteTreeNodeModel node,
			String normalizedNewCourseCode,
			Map<String, Integer> resolvedPrerequisiteCourseIds) {
		if (node == null) {
			throw new CourseValidationException("prerequisiteTree must be a valid root node when provided");
		}

		String nodeType = normalizeNodeType(node.getType());

		if ("COURSE".equals(nodeType)) {
			validateCourseLeafNode(node, normalizedNewCourseCode, resolvedPrerequisiteCourseIds);
			return;
		}

		if (node.getCourseCode() != null && !node.getCourseCode().isBlank()) {
			throw new CourseValidationException(nodeType + " nodes cannot include courseCode");
		}

		List<PrerequisiteTreeNodeModel> children = node.getChildren();
		if (children == null || children.size() < 2) {
			throw new CourseValidationException(nodeType + " nodes must contain at least 2 children");
		}

		Set<String> childSignatures = new HashSet<>();
		for (PrerequisiteTreeNodeModel child : children) {
			if (child == null) {
				throw new CourseValidationException("Prerequisite tree cannot contain null child nodes");
			}

			validatePrerequisiteTree(child, normalizedNewCourseCode, resolvedPrerequisiteCourseIds);
			String signature = buildNodeSignature(child);
			if (!childSignatures.add(signature)) {
				throw new CourseValidationException(nodeType + " node contains duplicate child subtrees");
			}
		}
	}

	private void validateCourseLeafNode(
			PrerequisiteTreeNodeModel node,
			String normalizedNewCourseCode,
			Map<String, Integer> resolvedPrerequisiteCourseIds) {
		if (node.getChildren() != null && !node.getChildren().isEmpty()) {
			throw new CourseValidationException("COURSE nodes cannot contain children");
		}

		String courseCode = requireTrimmedText(node.getCourseCode(), "prerequisiteTree courseCode");
		String normalizedRequiredCode = normalizeCourseCodeForLookup(courseCode);

		if (normalizedNewCourseCode.equals(normalizedRequiredCode)) {
			throw new CourseValidationException("A course cannot require itself");
		}

		Integer requiredCourseId = resolvedPrerequisiteCourseIds.get(normalizedRequiredCode);
		if (requiredCourseId == null) {
			requiredCourseId = courseRepository.findCourseIdByCourseCode(courseCode)
					.orElseThrow(() -> new CourseValidationException("Referenced prerequisite course not found: " + courseCode));
			resolvedPrerequisiteCourseIds.put(normalizedRequiredCode, requiredCourseId);
		}
	}

	private void ensureNoIndirectCycle(String normalizedNewCourseCode, Iterable<Integer> referencedCourseIds) {
		for (Integer prerequisiteCourseId : referencedCourseIds) {
			if (courseDependsOnCourseCode(prerequisiteCourseId, normalizedNewCourseCode, new HashSet<>())) {
				throw new CourseValidationException(
						"Prerequisite tree introduces an indirect cycle involving course code " + normalizedNewCourseCode);
			}
		}
	}

	private boolean courseDependsOnCourseCode(Integer courseId, String targetCourseCode, Set<Integer> visitedCourses) {
		if (!visitedCourses.add(courseId)) {
			return false;
		}

		Optional<CourseWithRootPrerequisiteModel> course = courseRepository.findCourseWithRootPrerequisiteById(courseId);
		if (course.isEmpty() || course.get().getRootPrerequisiteNodeId() == null) {
			return false;
		}

		Set<String> requiredCourseCodes = collectRequiredCourseCodes(course.get().getRootPrerequisiteNodeId(), new HashSet<>());
		for (String requiredCode : requiredCourseCodes) {
			if (targetCourseCode.equals(normalizeCourseCodeForLookup(requiredCode))) {
				return true;
			}

			Optional<Integer> requiredCourseId = courseRepository.findCourseIdByCourseCode(requiredCode);
			if (requiredCourseId.isPresent()
					&& courseDependsOnCourseCode(requiredCourseId.get(), targetCourseCode, visitedCourses)) {
				return true;
			}
		}

		return false;
	}

	private Set<String> collectRequiredCourseCodes(Integer nodeId, Set<Integer> recursionPath) {
		if (nodeId == null) {
			return Set.of();
		}

		if (!recursionPath.add(nodeId)) {
			throw new IllegalStateException("Cycle detected in prerequisite tree at node " + nodeId);
		}

		try {
			PrerequisiteTreeNodeModel currentNode = courseRepository.findPrerequisiteNodeById(nodeId)
					.orElseThrow(() -> new IllegalStateException("Prerequisite node not found: " + nodeId));

			if ("COURSE".equals(currentNode.getType())) {
				return Set.of(currentNode.getCourseCode());
			}

			Set<String> collectedCodes = new HashSet<>();
			for (Integer childNodeId : courseRepository.findChildNodeIds(nodeId)) {
				collectedCodes.addAll(collectRequiredCourseCodes(childNodeId, recursionPath));
			}

			return collectedCodes;
		} finally {
			recursionPath.remove(nodeId);
		}
	}

	private String buildNodeSignature(PrerequisiteTreeNodeModel node) {
		String nodeType = normalizeNodeType(node.getType());
		if ("COURSE".equals(nodeType)) {
			return "COURSE:" + normalizeCourseCodeForLookup(node.getCourseCode());
		}

		StringBuilder signature = new StringBuilder(nodeType).append('[');
		List<PrerequisiteTreeNodeModel> children = node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			if (i > 0) {
				signature.append(',');
			}
			signature.append(buildNodeSignature(children.get(i)));
		}
		signature.append(']');

		return signature.toString();
	}

	private String normalizeNodeType(String type) {
		String normalizedType = requireTrimmedText(type, "prerequisite node type").toUpperCase(Locale.ROOT);
		if (!"AND".equals(normalizedType) && !"OR".equals(normalizedType) && !"COURSE".equals(normalizedType)) {
			throw new CourseValidationException("Invalid prerequisite node type: " + normalizedType);
		}

		return normalizedType;
	}

	private String normalizeCourseCodeForLookup(String courseCode) {
		return requireTrimmedText(courseCode, "course_code").toUpperCase(Locale.ROOT);
	}

	private String requireTrimmedText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new CourseValidationException(fieldName + " is required");
		}

		return value.trim();
	}

	private CoursePatchCommand validateAndNormalizePatchRequest(CoursePatchRequestModel request) {
		if (request == null) {
			throw new CourseValidationException("Request body is required");
		}

		boolean hasCourseCode = request.getCourseCode() != null;
		boolean hasCrn = request.getCrn() != null;
		boolean hasTitle = request.getTitle() != null;
		boolean hasCredits = request.getCredits() != null;
		boolean hasAttributes = request.getAttributes() != null;

		if (!hasCourseCode && !hasCrn && !hasTitle && !hasCredits && !hasAttributes) {
			throw new CourseValidationException("At least one updatable field must be provided");
		}

		String courseCode = null;
		if (hasCourseCode) {
			courseCode = requireTrimmedText(request.getCourseCode(), "course_code");
		}

		String crn = null;
		if (hasCrn) {
			crn = request.getCrn().trim();
			if (crn.isEmpty()) {
				crn = null;
			}
		}

		String title = null;
		if (hasTitle) {
			title = requireTrimmedText(request.getTitle(), "title");
		}

		Integer credits = null;
		if (hasCredits) {
			credits = request.getCredits();
			if (credits < 0) {
				throw new CourseValidationException("credits must be >= 0");
			}
		}

		List<String> attributes = null;
		if (hasAttributes) {
			attributes = validateAndNormalizeAttributes(request.getAttributes(), false);
		}

		return new CoursePatchCommand(
				hasCourseCode,
				courseCode,
				hasCrn,
				crn,
				hasTitle,
				title,
				hasCredits,
				credits,
				hasAttributes,
				attributes);
	}

	private List<String> validateAndNormalizeAttributes(List<String> attributes, boolean defaultToEmpty) {
		if (attributes == null) {
			return defaultToEmpty ? List.of() : null;
		}

		for (String attribute : attributes) {
			if (attribute == null || attribute.isBlank()) {
				throw new CourseValidationException("attributes cannot contain null or blank values");
			}
		}

		return attributes.stream().map(String::trim).toList();
	}

	private CourseModel toCourseSummaryModel(CourseWithRootPrerequisiteModel course) {
		return new CourseModel(
				course.getCourseId(),
				course.getCourseCode(),
				course.getCrn(),
				course.getTitle(),
				course.getCredits(),
				course.getAttributes());
	}

	private CourseCreateCommand validateAndNormalizeCreateRequest(CourseCreateRequestModel request) {
		if (request == null) {
			throw new CourseValidationException("Request body is required");
		}

		String courseCode = requireTrimmedText(request.getCourseCode(), "course_code");
		String title = requireTrimmedText(request.getTitle(), "title");

		if (request.getCredits() == null) {
			throw new CourseValidationException("credits is required");
		}

		if (request.getCredits() < 0) {
			throw new CourseValidationException("credits must be >= 0");
		}

		String crn = request.getCrn();
		if (crn != null) {
			crn = crn.trim();
			if (crn.isEmpty()) {
				crn = null;
			}
		}

		List<String> attributes = validateAndNormalizeAttributes(request.getAttributes(), true);

		return new CourseCreateCommand(
				courseCode,
				crn,
				title,
				request.getCredits(),
				attributes,
				request.getPrerequisiteTree());
	}

	private record CourseCreateCommand(
			String courseCode,
			String crn,
			String title,
			Integer credits,
			List<String> attributes,
			PrerequisiteTreeNodeModel prerequisiteTree) {
	}

	private record CoursePatchCommand(
			boolean hasCourseCode,
			String courseCode,
			boolean hasCrn,
			String crn,
			boolean hasTitle,
			String title,
			boolean hasCredits,
			Integer credits,
			boolean hasAttributes,
			List<String> attributes) {
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
