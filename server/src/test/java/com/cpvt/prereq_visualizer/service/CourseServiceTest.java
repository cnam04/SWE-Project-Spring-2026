package com.cpvt.prereq_visualizer.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cpvt.prereq_visualizer.model.CourseCreateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseGraphCourseNodeDataModel;
import com.cpvt.prereq_visualizer.model.CourseGraphModel;
import com.cpvt.prereq_visualizer.model.CourseGraphOperatorNodeDataModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.CoursePatchRequestModel;
import com.cpvt.prereq_visualizer.model.CoursePrerequisitesUpdateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseWithRootPrerequisiteModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteGraphNodeSourceModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;
import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.repository.CourseRepository;
import com.cpvt.prereq_visualizer.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private StudentRepository studentRepository;

	@InjectMocks
	private CourseService courseService;

	@Test
	void getCourseById_whenCourseNotFound_returnsEmpty() {
		when(courseRepository.findCourseWithRootPrerequisiteById(999)).thenReturn(Optional.empty());

		Optional<CourseDetailModel> result = courseService.getCourseById(999);

		assertTrue(result.isEmpty());
		verify(courseRepository).findCourseWithRootPrerequisiteById(999);
		verify(courseRepository, never()).findPrerequisiteNodeById(anyInt());
	}

	@Test
	void getCourseById_whenCourseHasNoPrerequisites_returnsNullTree() {
		CourseWithRootPrerequisiteModel course = new CourseWithRootPrerequisiteModel(
				1,
				"CPS101",
				"10002",
				"Intro to Programming",
				3,
				List.of("Core"),
				null);

		when(courseRepository.findCourseWithRootPrerequisiteById(1)).thenReturn(Optional.of(course));

		Optional<CourseDetailModel> result = courseService.getCourseById(1);

		assertTrue(result.isPresent());
		assertNull(result.get().getPrerequisiteTree());
		verify(courseRepository, never()).findPrerequisiteNodeById(anyInt());
	}

	@Test
	void getCourseById_buildsNestedPrerequisiteTree() {
		CourseWithRootPrerequisiteModel course = new CourseWithRootPrerequisiteModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				12);

		when(courseRepository.findCourseWithRootPrerequisiteById(12)).thenReturn(Optional.of(course));

		when(courseRepository.findPrerequisiteNodeById(12))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("OR", null, null)));
		when(courseRepository.findChildNodeIds(12)).thenReturn(List.of(13, 16));

		when(courseRepository.findPrerequisiteNodeById(13))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("AND", null, null)));
		when(courseRepository.findChildNodeIds(13)).thenReturn(List.of(14, 15));

		when(courseRepository.findPrerequisiteNodeById(14))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("COURSE", "CPS310", null)));
		when(courseRepository.findPrerequisiteNodeById(15))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("COURSE", "CPS320", null)));
		when(courseRepository.findPrerequisiteNodeById(16))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("COURSE", "CPS330", null)));

		CourseDetailModel result = courseService.getCourseById(12).orElseThrow();

		assertEquals("CPS410", result.getCourseCode());
		assertNotNull(result.getPrerequisiteTree());
		assertEquals("OR", result.getPrerequisiteTree().getType());
		assertEquals("AND", result.getPrerequisiteTree().getChildren().get(0).getType());
		assertEquals("CPS310", result.getPrerequisiteTree().getChildren().get(0).getChildren().get(0).getCourseCode());
		assertEquals("CPS320", result.getPrerequisiteTree().getChildren().get(0).getChildren().get(1).getCourseCode());
		assertEquals("CPS330", result.getPrerequisiteTree().getChildren().get(1).getCourseCode());
	}

	@Test
	void createCourse_whenCourseCodeAlreadyExists_throwsConflict() {
		CourseCreateRequestModel request = new CourseCreateRequestModel(
				"CPS250",
				"10250",
				"Programming Languages",
				3,
				List.of("Core"),
				null);

		when(courseRepository.findCourseIdByCourseCode("CPS250")).thenReturn(Optional.of(99));

		CourseConflictException exception = assertThrows(
				CourseConflictException.class,
				() -> courseService.createCourse(request));
		assertEquals("Course code already exists: CPS250", exception.getMessage());
		verify(courseRepository, never()).insertCourse(anyString(), anyString(), anyString(), anyInt(), anyList());
	}

	@Test
	void createCourse_withoutPrerequisiteTree_createsCourseAndReturnsDetail() {
		CourseCreateRequestModel request = new CourseCreateRequestModel(
				"CPS250",
				"10250",
				"Programming Languages",
				3,
				List.of("Core"),
				null);

		when(courseRepository.findCourseIdByCourseCode("CPS250")).thenReturn(Optional.empty());
		when(courseRepository.insertCourse("CPS250", "10250", "Programming Languages", 3, List.of("Core"))).thenReturn(20);
		when(courseRepository.findCourseWithRootPrerequisiteById(20))
				.thenReturn(Optional.of(new CourseWithRootPrerequisiteModel(
						20,
						"CPS250",
						"10250",
						"Programming Languages",
						3,
						List.of("Core"),
						null)));

		CourseDetailModel result = courseService.createCourse(request);

		assertEquals(20, result.getCourseId());
		assertEquals("CPS250", result.getCourseCode());
		assertNull(result.getPrerequisiteTree());
		verify(courseRepository, never()).insertPrerequisiteNode(anyInt(), anyString(), anyInt());
	}

	@Test
	void createCourse_withPrerequisiteTree_persistsNodesAndReturnsTree() {
		PrerequisiteTreeNodeModel cps210Node = new PrerequisiteTreeNodeModel("COURSE", "CPS210", null);
		PrerequisiteTreeNodeModel cps220Node = new PrerequisiteTreeNodeModel("COURSE", "CPS220", null);
		PrerequisiteTreeNodeModel rootNode = new PrerequisiteTreeNodeModel("AND", null, List.of(cps210Node, cps220Node));

		CourseCreateRequestModel request = new CourseCreateRequestModel(
				"CPS250",
				"10250",
				"Programming Languages",
				3,
				List.of("Core"),
				rootNode);

		when(courseRepository.findCourseIdByCourseCode("CPS250")).thenReturn(Optional.empty());
		when(courseRepository.findCourseIdByCourseCode("CPS210")).thenReturn(Optional.of(6));
		when(courseRepository.findCourseIdByCourseCode("CPS220")).thenReturn(Optional.of(7));

		when(courseRepository.findCourseWithRootPrerequisiteById(6))
				.thenReturn(Optional.of(new CourseWithRootPrerequisiteModel(
						6,
						"CPS210",
						"10006",
						"Data Structures",
						4,
						List.of("Core"),
						null)));
		when(courseRepository.findCourseWithRootPrerequisiteById(7))
				.thenReturn(Optional.of(new CourseWithRootPrerequisiteModel(
						7,
						"CPS220",
						"10007",
						"Discrete Structures",
						3,
						List.of("Core"),
						null)));

		when(courseRepository.insertCourse("CPS250", "10250", "Programming Languages", 3, List.of("Core"))).thenReturn(30);
		when(courseRepository.insertPrerequisiteNode(30, "AND", null)).thenReturn(100);
		when(courseRepository.insertPrerequisiteNode(30, "COURSE", 6)).thenReturn(101);
		when(courseRepository.insertPrerequisiteNode(30, "COURSE", 7)).thenReturn(102);

		when(courseRepository.findCourseWithRootPrerequisiteById(30))
				.thenReturn(Optional.of(new CourseWithRootPrerequisiteModel(
						30,
						"CPS250",
						"10250",
						"Programming Languages",
						3,
						List.of("Core"),
						100)));
		when(courseRepository.findPrerequisiteNodeById(100))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("AND", null, null)));
		when(courseRepository.findChildNodeIds(100)).thenReturn(List.of(101, 102));
		when(courseRepository.findPrerequisiteNodeById(101))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("COURSE", "CPS210", null)));
		when(courseRepository.findPrerequisiteNodeById(102))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("COURSE", "CPS220", null)));

		CourseDetailModel result = courseService.createCourse(request);

		assertNotNull(result.getPrerequisiteTree());
		assertEquals("AND", result.getPrerequisiteTree().getType());
		assertEquals("CPS210", result.getPrerequisiteTree().getChildren().get(0).getCourseCode());
		assertEquals("CPS220", result.getPrerequisiteTree().getChildren().get(1).getCourseCode());

		verify(courseRepository).updateCourseRootPrerequisiteNodeId(30, 100);
		verify(courseRepository).insertPrerequisiteEdge(100, 101, 1);
		verify(courseRepository).insertPrerequisiteEdge(100, 102, 2);
	}

	@Test
	void createCourse_whenOperatorNodeHasSingleChild_throwsValidation() {
		PrerequisiteTreeNodeModel onlyChild = new PrerequisiteTreeNodeModel("COURSE", "CPS210", null);
		PrerequisiteTreeNodeModel rootNode = new PrerequisiteTreeNodeModel("AND", null, List.of(onlyChild));

		CourseCreateRequestModel request = new CourseCreateRequestModel(
				"CPS250",
				"10250",
				"Programming Languages",
				3,
				List.of("Core"),
				rootNode);

		when(courseRepository.findCourseIdByCourseCode("CPS250")).thenReturn(Optional.empty());

		CourseValidationException exception = assertThrows(
				CourseValidationException.class,
				() -> courseService.createCourse(request));
		assertEquals("AND nodes must contain at least 2 children", exception.getMessage());
		verify(courseRepository, never()).insertCourse(anyString(), anyString(), anyString(), anyInt(), anyList());
	}

	@Test
	void updateCourse_whenCourseNotFound_returnsEmpty() {
		when(courseRepository.findCourseWithRootPrerequisiteById(999)).thenReturn(Optional.empty());

		CoursePatchRequestModel request = new CoursePatchRequestModel(null, null, "Updated title", null, null);
		Optional<CourseModel> result = courseService.updateCourse(999, request);

		assertTrue(result.isEmpty());
		verify(courseRepository, never()).updateCourseSummaryFields(anyInt(), anyString(), anyString(), anyString(), anyInt(), anyList());
	}

	@Test
	void updateCourse_withValidPatch_updatesAndReturnsSummary() {
		CourseWithRootPrerequisiteModel existing = new CourseWithRootPrerequisiteModel(
				8,
				"CPS310",
				"10008",
				"Algorithms",
				3,
				List.of("Advanced Core"),
				2);

		CourseWithRootPrerequisiteModel updated = new CourseWithRootPrerequisiteModel(
				8,
				"CPS310",
				"10008",
				"Algorithms and Analysis",
				4,
				List.of("Advanced Core"),
				2);

		when(courseRepository.findCourseWithRootPrerequisiteById(8))
				.thenReturn(Optional.of(existing))
				.thenReturn(Optional.of(updated));
		when(courseRepository.updateCourseSummaryFields(
				8,
				"CPS310",
				"10008",
				"Algorithms and Analysis",
				4,
				List.of("Advanced Core"))).thenReturn(1);

		CoursePatchRequestModel request = new CoursePatchRequestModel(
				null,
				null,
				"Algorithms and Analysis",
				4,
				null);

		CourseModel result = courseService.updateCourse(8, request).orElseThrow();

		assertEquals(8, result.getCourseId());
		assertEquals("Algorithms and Analysis", result.getTitle());
		assertEquals(4, result.getCredits());
	}

	@Test
	void updateCourse_whenCourseCodeConflicts_throwsConflict() {
		CourseWithRootPrerequisiteModel existing = new CourseWithRootPrerequisiteModel(
				8,
				"CPS310",
				"10008",
				"Algorithms",
				3,
				List.of("Advanced Core"),
				null);

		when(courseRepository.findCourseWithRootPrerequisiteById(8)).thenReturn(Optional.of(existing));
		when(courseRepository.findCourseIdByCourseCode("CPS320")).thenReturn(Optional.of(9));

		CoursePatchRequestModel request = new CoursePatchRequestModel("CPS320", null, null, null, null);

		CourseConflictException exception = assertThrows(
				CourseConflictException.class,
				() -> courseService.updateCourse(8, request));
		assertEquals("Course code already exists: CPS320", exception.getMessage());
		verify(courseRepository, never()).updateCourseSummaryFields(anyInt(), anyString(), anyString(), anyString(), anyInt(), anyList());
	}

	@Test
	void updateCoursePrerequisites_withNullTree_clearsExistingPrerequisites() {
		CourseWithRootPrerequisiteModel existing = new CourseWithRootPrerequisiteModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				12);

		CourseWithRootPrerequisiteModel updated = new CourseWithRootPrerequisiteModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				null);

		when(courseRepository.findCourseWithRootPrerequisiteById(12))
				.thenReturn(Optional.of(existing))
				.thenReturn(Optional.of(updated));

		CoursePrerequisitesUpdateRequestModel request = new CoursePrerequisitesUpdateRequestModel(null);

		CourseDetailModel result = courseService.updateCoursePrerequisites(12, request).orElseThrow();

		assertNull(result.getPrerequisiteTree());
		verify(courseRepository).updateCourseRootPrerequisiteNodeId(12, null);
		verify(courseRepository).deletePrerequisiteNodesByCourseId(12);
		verify(courseRepository, never()).insertPrerequisiteNode(anyInt(), anyString(), anyInt());
	}

	@Test
	void updateCoursePrerequisites_withTree_replacesExistingTree() {
		CourseWithRootPrerequisiteModel existing = new CourseWithRootPrerequisiteModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				12);

		CourseWithRootPrerequisiteModel referencedPrereqCourse = new CourseWithRootPrerequisiteModel(
				10,
				"CPS330",
				"10010",
				"Operating Systems",
				3,
				List.of("Advanced Core"),
				null);

		CourseWithRootPrerequisiteModel updated = new CourseWithRootPrerequisiteModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				300);

		when(courseRepository.findCourseWithRootPrerequisiteById(12))
				.thenReturn(Optional.of(existing))
				.thenReturn(Optional.of(updated));
		when(courseRepository.findCourseWithRootPrerequisiteById(10))
				.thenReturn(Optional.of(referencedPrereqCourse));
		when(courseRepository.findCourseIdByCourseCode("CPS330")).thenReturn(Optional.of(10));
		when(courseRepository.insertPrerequisiteNode(12, "COURSE", 10)).thenReturn(300);
		when(courseRepository.findPrerequisiteNodeById(300))
				.thenReturn(Optional.of(new PrerequisiteTreeNodeModel("COURSE", "CPS330", null)));

		CoursePrerequisitesUpdateRequestModel request = new CoursePrerequisitesUpdateRequestModel(
				new PrerequisiteTreeNodeModel("COURSE", "CPS330", null));

		CourseDetailModel result = courseService.updateCoursePrerequisites(12, request).orElseThrow();

		assertNotNull(result.getPrerequisiteTree());
		assertEquals("COURSE", result.getPrerequisiteTree().getType());
		assertEquals("CPS330", result.getPrerequisiteTree().getCourseCode());

		InOrder inOrder = inOrder(courseRepository);
		inOrder.verify(courseRepository).updateCourseRootPrerequisiteNodeId(12, null);
		inOrder.verify(courseRepository).deletePrerequisiteNodesByCourseId(12);
		inOrder.verify(courseRepository).insertPrerequisiteNode(12, "COURSE", 10);
		inOrder.verify(courseRepository).updateCourseRootPrerequisiteNodeId(12, 300);
	}

	@Test
	void getCourseGraph_withoutStudentId_buildsGraphNodesAndEdges() {
		when(courseRepository.findCourseWithRootPrerequisiteById(12)).thenReturn(Optional.of(
				new CourseWithRootPrerequisiteModel(
						12,
						"CPS410",
						"10012",
						"Advanced Topics in CS",
						3,
						List.of("Capstone Track"),
						12)));
		when(courseRepository.findPrerequisiteGraphNodeById(12)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(12, "OR", null, null, null, null, null, null, null)));
		when(courseRepository.findChildNodeIds(12)).thenReturn(List.of(14));
		when(courseRepository.findPrerequisiteGraphNodeById(14)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(
						14,
						"COURSE",
						6,
						"CPS210",
						"10006",
						"Data Structures",
						4,
						List.of("Core"),
						null)));

		CourseGraphModel graph = courseService.getCourseGraph(12, null).orElseThrow();

		assertEquals(12, graph.getCourseId());
		assertEquals("CPS410", graph.getCourseCode());
		assertEquals("Advanced Topics in CS", graph.getTitle());
		assertEquals("none", graph.getStatusMode());
		assertEquals("LR", graph.getLayoutDirection());
		assertEquals(3, graph.getNodes().size());
		assertEquals(2, graph.getEdges().size());
		assertEquals("courseNode", graph.getNodes().get(0).getType());
		assertEquals("operatorNode", graph.getNodes().get(1).getType());
		assertEquals("courseNode", graph.getNodes().get(2).getType());

		CourseGraphCourseNodeDataModel rootCourseData = (CourseGraphCourseNodeDataModel) graph.getNodes().get(0).getData();
		CourseGraphOperatorNodeDataModel operatorData = (CourseGraphOperatorNodeDataModel) graph.getNodes().get(1).getData();
		CourseGraphCourseNodeDataModel prereqCourseData = (CourseGraphCourseNodeDataModel) graph.getNodes().get(2).getData();

		assertEquals("course", rootCourseData.getKind());
		assertTrue(Boolean.TRUE.equals(rootCourseData.getIsTargetCourse()));
		assertNull(rootCourseData.getStatus());
		assertEquals("OR", operatorData.getOperator());
		assertEquals(12, operatorData.getPrerequisiteNodeId());
		assertNull(prereqCourseData.getStatus());
		assertTrue(Boolean.FALSE.equals(prereqCourseData.getIsTargetCourse()));

		assertEquals("course-6", graph.getEdges().get(0).getSource());
		assertEquals("op-12", graph.getEdges().get(0).getTarget());
		assertEquals("op-12", graph.getEdges().get(1).getSource());
		assertEquals("course-12", graph.getEdges().get(1).getTarget());
	}

	@Test
	void getCourseGraph_withStudentId_appliesCourseStatuses() {
		when(courseRepository.findCourseWithRootPrerequisiteById(12)).thenReturn(Optional.of(
				new CourseWithRootPrerequisiteModel(
						12,
						"CPS410",
						"10012",
						"Advanced Topics in CS",
						3,
						List.of("Capstone Track"),
						12)));
		when(studentRepository.findStudentById(1)).thenReturn(Optional.of(
				new StudentModel(1, 1, "Cole Nam", "cole@example.com", "NP100001", "Computer Science")));
		when(courseRepository.findPrerequisiteGraphNodeById(12)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(12, "OR", null, null, null, null, null, null, null)));
		when(courseRepository.findChildNodeIds(12)).thenReturn(List.of(14));
		when(courseRepository.findPrerequisiteGraphNodeById(14)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(
						14,
						"COURSE",
						6,
						"CPS210",
						"10006",
						"Data Structures",
						4,
						List.of("Core"),
						null)));
		when(courseRepository.findStudentCourseStatuses(eq(1), anyList())).thenReturn(Map.of(6, "in_progress"));

		CourseGraphModel graph = courseService.getCourseGraph(12, 1).orElseThrow();
		CourseGraphCourseNodeDataModel rootCourseData = (CourseGraphCourseNodeDataModel) graph.getNodes().get(0).getData();
		CourseGraphCourseNodeDataModel prereqCourseData = (CourseGraphCourseNodeDataModel) graph.getNodes().get(2).getData();

		assertEquals(1, graph.getStudentId());
		assertEquals("student", graph.getStatusMode());
		assertEquals("not_taken", rootCourseData.getStatus());
		assertEquals("in_progress", prereqCourseData.getStatus());
	}

	@Test
	void getCourseGraph_withExpandTrue_recursivelyExpandsPrerequisites() {
		when(courseRepository.findCourseWithRootPrerequisiteById(12)).thenReturn(Optional.of(
				new CourseWithRootPrerequisiteModel(
						12,
						"CPS410",
						"10012",
						"Advanced Topics in CS",
						3,
						List.of("Capstone Track"),
						12)));

		when(courseRepository.findPrerequisiteGraphNodeById(12)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(12, "OR", null, null, null, null, null, null, null)));
		when(courseRepository.findChildNodeIds(12)).thenReturn(List.of(14));
		when(courseRepository.findPrerequisiteGraphNodeById(14)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(
						14,
						"COURSE",
						6,
						"CPS210",
						"10006",
						"Data Structures",
						4,
						List.of("Core"),
						50)));

		when(courseRepository.findPrerequisiteGraphNodeById(50)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(50, "AND", null, null, null, null, null, null, null)));
		when(courseRepository.findChildNodeIds(50)).thenReturn(List.of(51, 52));
		when(courseRepository.findPrerequisiteGraphNodeById(51)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(
						51,
						"COURSE",
						3,
						"MAT101",
						"10001",
						"College Algebra",
						3,
						List.of("Math Foundation"),
						null)));
		when(courseRepository.findPrerequisiteGraphNodeById(52)).thenReturn(Optional.of(
				new PrerequisiteGraphNodeSourceModel(
						52,
						"COURSE",
						4,
						"CPS101",
						"10002",
						"Intro to Programming",
						3,
						List.of("Core"),
						null)));

		CourseGraphModel graph = courseService.getCourseGraph(12, null, true).orElseThrow();

		assertTrue(graph.getNodes().stream().anyMatch(node -> "op-50".equals(node.getId())));
		assertTrue(graph.getEdges().stream().anyMatch(edge ->
				"op-50".equals(edge.getSource()) && "course-6".equals(edge.getTarget())));
	}

	@Test
	void getCourseGraph_whenStudentIdIsUnknown_throwsValidation() {
		when(courseRepository.findCourseWithRootPrerequisiteById(12)).thenReturn(Optional.of(
				new CourseWithRootPrerequisiteModel(
						12,
						"CPS410",
						"10012",
						"Advanced Topics in CS",
						3,
						List.of("Capstone Track"),
						null)));
		when(studentRepository.findStudentById(999)).thenReturn(Optional.empty());

		CourseValidationException exception = assertThrows(
				CourseValidationException.class,
				() -> courseService.getCourseGraph(12, 999));

		assertEquals("Referenced student not found: 999", exception.getMessage());
	}
}
