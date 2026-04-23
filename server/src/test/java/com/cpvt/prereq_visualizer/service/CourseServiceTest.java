package com.cpvt.prereq_visualizer.service;

import java.util.List;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cpvt.prereq_visualizer.model.CourseCreateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseWithRootPrerequisiteModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;
import com.cpvt.prereq_visualizer.repository.CourseRepository;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

	@Mock
	private CourseRepository courseRepository;

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
}
