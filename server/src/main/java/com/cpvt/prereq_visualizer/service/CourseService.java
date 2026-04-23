package com.cpvt.prereq_visualizer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cpvt.prereq_visualizer.model.CourseModel;
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
}
