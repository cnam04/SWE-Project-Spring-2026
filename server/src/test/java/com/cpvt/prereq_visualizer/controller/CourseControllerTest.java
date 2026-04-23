package com.cpvt.prereq_visualizer.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;
import com.cpvt.prereq_visualizer.service.CourseService;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CourseService courseService;

	@Test
	void getAllCourses_returnsCourseSummaries() throws Exception {
		CourseModel course = new CourseModel(
				1,
				"CPS101",
				"10002",
				"Intro to Programming",
				3,
				List.of("Core"));

		when(courseService.getAllCourses()).thenReturn(List.of(course));

		mockMvc.perform(get("/api/courses/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].course_id").value(1))
				.andExpect(jsonPath("$[0].course_code").value("CPS101"))
				.andExpect(jsonPath("$[0].crn").value("10002"))
				.andExpect(jsonPath("$[0].title").value("Intro to Programming"))
				.andExpect(jsonPath("$[0].credits").value(3))
				.andExpect(jsonPath("$[0].attributes[0]").value("Core"));
	}

	@Test
	void getCourseById_returnsCourseWithPrerequisiteTree() throws Exception {
		PrerequisiteTreeNodeModel cps310Node = new PrerequisiteTreeNodeModel("COURSE", "CPS310", null);
		PrerequisiteTreeNodeModel cps320Node = new PrerequisiteTreeNodeModel("COURSE", "CPS320", null);
		PrerequisiteTreeNodeModel andNode = new PrerequisiteTreeNodeModel("AND", null, List.of(cps310Node, cps320Node));
		PrerequisiteTreeNodeModel cps330Node = new PrerequisiteTreeNodeModel("COURSE", "CPS330", null);
		PrerequisiteTreeNodeModel rootNode = new PrerequisiteTreeNodeModel("OR", null, List.of(andNode, cps330Node));

		CourseDetailModel detail = new CourseDetailModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				rootNode);

		when(courseService.getCourseById(12)).thenReturn(Optional.of(detail));

		mockMvc.perform(get("/api/courses/12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.course_id").value(12))
				.andExpect(jsonPath("$.course_code").value("CPS410"))
				.andExpect(jsonPath("$.title").value("Advanced Topics in CS"))
				.andExpect(jsonPath("$.prerequisiteTree.type").value("OR"))
				.andExpect(jsonPath("$.prerequisiteTree.children[0].type").value("AND"))
				.andExpect(jsonPath("$.prerequisiteTree.children[0].children[0].courseCode").value("CPS310"))
				.andExpect(jsonPath("$.prerequisiteTree.children[0].children[1].courseCode").value("CPS320"))
				.andExpect(jsonPath("$.prerequisiteTree.children[1].courseCode").value("CPS330"));
	}

	@Test
	void getCourseById_whenMissing_returns404() throws Exception {
		when(courseService.getCourseById(999)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/courses/999"))
				.andExpect(status().isNotFound());
	}
}
