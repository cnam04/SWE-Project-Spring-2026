package com.cpvt.prereq_visualizer.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
