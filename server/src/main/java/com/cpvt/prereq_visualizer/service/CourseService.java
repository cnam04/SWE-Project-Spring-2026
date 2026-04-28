package com.cpvt.prereq_visualizer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpvt.prereq_visualizer.model.CourseCreateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseGraphCourseNodeDataModel;
import com.cpvt.prereq_visualizer.model.CourseGraphEdgeDataModel;
import com.cpvt.prereq_visualizer.model.CourseGraphEdgeModel;
import com.cpvt.prereq_visualizer.model.CourseGraphModel;
import com.cpvt.prereq_visualizer.model.CourseGraphNodeModel;
import com.cpvt.prereq_visualizer.model.CourseGraphOperatorNodeDataModel;
import com.cpvt.prereq_visualizer.model.CourseGraphPositionModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.CoursePatchRequestModel;
import com.cpvt.prereq_visualizer.model.CoursePrerequisitesUpdateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseWithRootPrerequisiteModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteGraphNodeSourceModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;
import com.cpvt.prereq_visualizer.repository.CourseRepository;
import com.cpvt.prereq_visualizer.repository.StudentRepository;

@Service
public class CourseService {

	private final CourseRepository courseRepository;
	private final StudentRepository studentRepository;

	public CourseService(CourseRepository courseRepository, StudentRepository studentRepository) {
		this.courseRepository = courseRepository;
		this.studentRepository = studentRepository;
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

	public Optional<CourseGraphModel> getCourseGraph(Integer courseId, Integer studentId) {
		return getCourseGraph(courseId, studentId, false);
	}

	public Optional<CourseGraphModel> getCourseGraph(Integer courseId, Integer studentId, Boolean expand) {
		Integer normalizedCourseId = requirePositiveId(courseId, "course_id");
		boolean shouldExpand = Boolean.TRUE.equals(expand);

		Optional<CourseWithRootPrerequisiteModel> existingCourse = courseRepository
				.findCourseWithRootPrerequisiteById(normalizedCourseId);
		if (existingCourse.isEmpty()) {
			return Optional.empty();
		}

		Integer normalizedStudentId = null;
		if (studentId != null) {
			normalizedStudentId = requirePositiveId(studentId, "studentId");
			if (studentRepository.findStudentById(normalizedStudentId).isEmpty()) {
				throw new CourseValidationException("Referenced student not found: " + normalizedStudentId);
			}
		}

		CourseWithRootPrerequisiteModel course = existingCourse.get();
		GraphBuildContext context = new GraphBuildContext();

		String rootCourseNodeId = buildCourseNodeId(course.getCourseId());
		context.upsertCourseNode(
				rootCourseNodeId,
				course.getCourseId(),
				course.getCourseCode(),
				course.getCrn(),
				course.getTitle(),
				course.getCredits(),
				course.getAttributes(),
				true);

		Integer rootPrerequisiteNodeId = course.getRootPrerequisiteNodeId();
		if (rootPrerequisiteNodeId != null) {
			String prerequisiteRootGraphNodeId = buildGraphNodeFromPrerequisiteNode(
					rootPrerequisiteNodeId,
					context,
					new HashSet<>(),
					shouldExpand,
					new HashSet<>());
			context.addEdge(prerequisiteRootGraphNodeId, rootCourseNodeId);
		}

		if (normalizedStudentId != null) {
			Map<Integer, String> statusByCourseId = courseRepository.findStudentCourseStatuses(
					normalizedStudentId,
					context.getCourseIds());

			for (CourseGraphNodeModel node : context.getNodes()) {
				if (node.getData() instanceof CourseGraphCourseNodeDataModel courseData
						&& courseData.getCourseId() != null) {
					courseData.setStatus(statusByCourseId.getOrDefault(courseData.getCourseId(), "not_taken"));
				}
			}
		} else {
			for (CourseGraphNodeModel node : context.getNodes()) {
				if (node.getData() instanceof CourseGraphCourseNodeDataModel courseData) {
					courseData.setStatus(null);
				}
			}
		}

		CourseGraphModel graph = new CourseGraphModel(
				course.getCourseId(),
				course.getCourseCode(),
				course.getTitle(),
				normalizedStudentId,
				normalizedStudentId == null ? "none" : "student",
				"LR",
				context.getNodes(),
				context.getEdges());

		return Optional.of(graph);
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

	private Integer requirePositiveId(Integer value, String fieldName) {
		if (value == null) {
			throw new CourseValidationException(fieldName + " is required");
		}

		if (value <= 0) {
			throw new CourseValidationException(fieldName + " must be positive");
		}

		return value;
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

	private String buildGraphNodeFromPrerequisiteNode(
			Integer nodeId,
			GraphBuildContext context,
			Set<Integer> recursionPath,
			boolean expand,
			Set<Integer> expandedCourseIds) {
		if (!recursionPath.add(nodeId)) {
			throw new IllegalStateException("Cycle detected in prerequisite tree at node " + nodeId);
		}

		try {
			PrerequisiteGraphNodeSourceModel sourceNode = courseRepository.findPrerequisiteGraphNodeById(nodeId)
					.orElseThrow(() -> new IllegalStateException("Prerequisite node not found: " + nodeId));

			String nodeType = normalizeNodeType(sourceNode.getNodeType());
			if ("COURSE".equals(nodeType)) {
				if (sourceNode.getRequiredCourseId() == null) {
					throw new IllegalStateException("COURSE prerequisite node missing required_course_id: " + nodeId);
				}

				String courseNodeId = buildCourseNodeId(sourceNode.getRequiredCourseId());
				context.upsertCourseNode(
						courseNodeId,
						sourceNode.getRequiredCourseId(),
						sourceNode.getRequiredCourseCode(),
						sourceNode.getRequiredCourseCrn(),
						sourceNode.getRequiredCourseTitle(),
						sourceNode.getRequiredCourseCredits(),
						sourceNode.getRequiredCourseAttributes(),
						false);

				if (expand
						&& sourceNode.getRequiredCourseRootPrerequisiteNodeId() != null
						&& expandedCourseIds.add(sourceNode.getRequiredCourseId())) {
					String requiredCourseRootGraphNodeId = buildGraphNodeFromPrerequisiteNode(
							sourceNode.getRequiredCourseRootPrerequisiteNodeId(),
							context,
							recursionPath,
							true,
							expandedCourseIds);
					context.addEdge(requiredCourseRootGraphNodeId, courseNodeId);
				}

				return courseNodeId;
			}

			String operatorNodeId = context.upsertOperatorNode(sourceNode.getNodeId(), nodeType);
			for (Integer childNodeId : courseRepository.findChildNodeIds(nodeId)) {
				String childGraphNodeId = buildGraphNodeFromPrerequisiteNode(
						childNodeId,
						context,
						recursionPath,
						expand,
						expandedCourseIds);
				context.addEdge(childGraphNodeId, operatorNodeId);
			}

			return operatorNodeId;
		} finally {
			recursionPath.remove(nodeId);
		}
	}

	private String buildCourseNodeId(Integer courseId) {
		if (courseId != null) {
			return "course-" + courseId;
		}

		throw new IllegalStateException("Cannot build graph node id without course identity");
	}

	private static final class GraphBuildContext {

		private final Map<String, CourseGraphNodeModel> nodesById = new LinkedHashMap<>();
		private final Map<String, CourseGraphEdgeModel> edgesById = new LinkedHashMap<>();

		void upsertCourseNode(
				String nodeId,
				Integer courseId,
				String courseCode,
				String crn,
				String title,
				Integer credits,
				List<String> attributes,
				boolean isTargetCourse) {
			CourseGraphNodeModel existingNode = nodesById.get(nodeId);
			if (existingNode == null) {
				CourseGraphCourseNodeDataModel nodeData = new CourseGraphCourseNodeDataModel(
						"course",
						courseId,
						courseCode,
						crn,
						title,
						credits,
						attributes == null ? List.of() : List.copyOf(attributes),
						null,
						isTargetCourse);

				nodesById.put(nodeId, new CourseGraphNodeModel(
						nodeId,
						"courseNode",
						new CourseGraphPositionModel(0, 0),
						nodeData));
				return;
			}

			if (existingNode.getData() instanceof CourseGraphCourseNodeDataModel courseNodeData) {
				if (courseNodeData.getCourseCode() == null) {
					courseNodeData.setCourseCode(courseCode);
				}
				if (courseNodeData.getCrn() == null) {
					courseNodeData.setCrn(crn);
				}
				if (courseNodeData.getTitle() == null) {
					courseNodeData.setTitle(title);
				}
				if (courseNodeData.getCredits() == null) {
					courseNodeData.setCredits(credits);
				}
				if (courseNodeData.getAttributes() == null || courseNodeData.getAttributes().isEmpty()) {
					courseNodeData.setAttributes(attributes == null ? List.of() : List.copyOf(attributes));
				}
				if (isTargetCourse) {
					courseNodeData.setIsTargetCourse(true);
				}
			}
		}

		String upsertOperatorNode(Integer prerequisiteNodeId, String operator) {
			String nodeId = "op-" + prerequisiteNodeId;
			if (!nodesById.containsKey(nodeId)) {
				CourseGraphOperatorNodeDataModel nodeData = new CourseGraphOperatorNodeDataModel(
						"operator",
						prerequisiteNodeId,
						operator,
						operator);
				nodesById.put(nodeId, new CourseGraphNodeModel(
						nodeId,
						"operatorNode",
						new CourseGraphPositionModel(0, 0),
						nodeData));
			}
			return nodeId;
		}

		void addEdge(String source, String target) {
			String edgeId = "edge-" + source + "-" + target;
			edgesById.putIfAbsent(edgeId, new CourseGraphEdgeModel(
					edgeId,
					source,
					target,
					"smoothstep",
					new CourseGraphEdgeDataModel("satisfies")));
		}

		List<Integer> getCourseIds() {
			return nodesById.values().stream()
					.map(CourseGraphNodeModel::getData)
					.filter(CourseGraphCourseNodeDataModel.class::isInstance)
					.map(CourseGraphCourseNodeDataModel.class::cast)
					.map(CourseGraphCourseNodeDataModel::getCourseId)
					.filter(courseId -> courseId != null)
					.toList();
		}

		List<CourseGraphNodeModel> getNodes() {
			return new ArrayList<>(nodesById.values());
		}

		List<CourseGraphEdgeModel> getEdges() {
			return new ArrayList<>(edgesById.values());
		}
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
