package com.cpvt.prereq_visualizer.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;
import com.cpvt.prereq_visualizer.service.CourseConflictException;
import com.cpvt.prereq_visualizer.service.CourseService;
import com.cpvt.prereq_visualizer.service.CourseValidationException;

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

	@Test
	void createCourse_returnsCreatedCourseDetail() throws Exception {
		CourseDetailModel created = new CourseDetailModel(
				20,
				"CPS250",
				"10250",
				"Programming Languages",
				3,
				List.of("Core"),
				null);

		when(courseService.createCourse(any())).thenReturn(created);

		String requestBody = """
				{
				  "course_code": "CPS250",
				  "crn": "10250",
				  "title": "Programming Languages",
				  "credits": 3,
				  "attributes": ["Core"],
				  "prerequisiteTree": null
				}
				""";

		mockMvc.perform(post("/api/courses")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.course_id").value(20))
				.andExpect(jsonPath("$.course_code").value("CPS250"))
				.andExpect(jsonPath("$.title").value("Programming Languages"));
	}

	@Test
	void createCourse_whenValidationFails_returns400() throws Exception {
		when(courseService.createCourse(any()))
				.thenThrow(new CourseValidationException("credits must be >= 0"));

		String requestBody = """
				{
				  "course_code": "CPS250",
				  "title": "Programming Languages",
				  "credits": -1,
				  "attributes": []
				}
				""";

		mockMvc.perform(post("/api/courses/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createCourse_whenCourseCodeExists_returns409() throws Exception {
		when(courseService.createCourse(any()))
				.thenThrow(new CourseConflictException("Course code already exists: CPS250"));

		String requestBody = """
				{
				  "course_code": "CPS250",
				  "title": "Programming Languages",
				  "credits": 3,
				  "attributes": []
				}
				""";

		mockMvc.perform(post("/api/courses")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict());
	}

	@Test
	void updateCourseById_returnsUpdatedCourseSummary() throws Exception {
		CourseModel updated = new CourseModel(
				8,
				"CPS310",
				"10008",
				"Algorithms and Analysis",
				4,
				List.of("Advanced Core"));

		when(courseService.updateCourse(any(), any())).thenReturn(Optional.of(updated));

		String requestBody = """
				{
				  "title": "Algorithms and Analysis",
				  "credits": 4
				}
				""";

		mockMvc.perform(patch("/api/courses/8")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.course_id").value(8))
				.andExpect(jsonPath("$.course_code").value("CPS310"))
				.andExpect(jsonPath("$.title").value("Algorithms and Analysis"))
				.andExpect(jsonPath("$.credits").value(4));
	}

	@Test
	void updateCourseById_whenMissing_returns404() throws Exception {
		when(courseService.updateCourse(any(), any())).thenReturn(Optional.empty());

		String requestBody = """
				{
				  "title": "Algorithms and Analysis"
				}
				""";

		mockMvc.perform(patch("/api/courses/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateCourseById_whenValidationFails_returns400() throws Exception {
		when(courseService.updateCourse(any(), any()))
				.thenThrow(new CourseValidationException("At least one updatable field must be provided"));

		mockMvc.perform(patch("/api/courses/8")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateCoursePrerequisites_returnsUpdatedCourseDetail() throws Exception {
		PrerequisiteTreeNodeModel prerequisiteTree = new PrerequisiteTreeNodeModel(
				"COURSE",
				"CPS330",
				null);

		CourseDetailModel updated = new CourseDetailModel(
				12,
				"CPS410",
				"10012",
				"Advanced Topics in CS",
				3,
				List.of("Capstone Track"),
				prerequisiteTree);

		when(courseService.updateCoursePrerequisites(any(), any())).thenReturn(Optional.of(updated));

		String requestBody = """
				{
				  "prerequisiteTree": {
				    "type": "COURSE",
				    "courseCode": "CPS330"
				  }
				}
				""";

		mockMvc.perform(put("/api/courses/12/prerequisites")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.course_id").value(12))
				.andExpect(jsonPath("$.course_code").value("CPS410"))
				.andExpect(jsonPath("$.prerequisiteTree.type").value("COURSE"))
				.andExpect(jsonPath("$.prerequisiteTree.courseCode").value("CPS330"));
	}

	@Test
	void updateCoursePrerequisites_whenMissing_returns404() throws Exception {
		when(courseService.updateCoursePrerequisites(any(), any())).thenReturn(Optional.empty());

		String requestBody = """
				{
				  "prerequisiteTree": null
				}
				""";

		mockMvc.perform(put("/api/courses/999/prerequisites")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateCoursePrerequisites_whenValidationFails_returns400() throws Exception {
		when(courseService.updateCoursePrerequisites(any(), any()))
				.thenThrow(new CourseValidationException("AND nodes must contain at least 2 children"));

		String requestBody = """
				{
				  "prerequisiteTree": {
				    "type": "AND",
				    "children": [
				      {
				        "type": "COURSE",
				        "courseCode": "CPS210"
				      }
				    ]
				  }
				}
				""";

		mockMvc.perform(put("/api/courses/12/prerequisites")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest());
	}
}
