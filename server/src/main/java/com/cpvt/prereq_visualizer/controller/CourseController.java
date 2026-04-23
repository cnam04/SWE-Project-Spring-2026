package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.service.CourseService;

@RestController
public class CourseController {

	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	// Support both variants so callers are not sensitive to trailing slash behavior.
	@GetMapping({ "/api/courses", "/api/courses/" })
	public List<CourseModel> getAllCourses() {
		return courseService.getAllCourses();
	}
}
