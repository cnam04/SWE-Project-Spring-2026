package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cpvt.prereq_visualizer.model.CourseModel;
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
}
