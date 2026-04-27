package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cpvt.prereq_visualizer.model.CourseCreateRequestModel;
import com.cpvt.prereq_visualizer.model.CourseDetailModel;
import com.cpvt.prereq_visualizer.model.CourseGraphModel;
import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.CoursePatchRequestModel;
import com.cpvt.prereq_visualizer.model.CoursePrerequisitesUpdateRequestModel;
import com.cpvt.prereq_visualizer.service.CourseConflictException;
import com.cpvt.prereq_visualizer.service.CourseService;
import com.cpvt.prereq_visualizer.service.CourseValidationException;

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

	@GetMapping({ "/api/courses/{id}", "/api/courses/{id}/" })
	public CourseDetailModel getCourseById(@PathVariable("id") Integer id) {
		return courseService.getCourseById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}

	@GetMapping({ "/api/courses/{id}/graph", "/api/courses/{id}/graph/" })
	public CourseGraphModel getCourseGraph(
			@PathVariable("id") Integer id,
			@RequestParam(value = "studentId", required = false) Integer studentId,
			@RequestParam(value = "expand", required = false, defaultValue = "false") Boolean expand) {
		try {
			return courseService.getCourseGraph(id, studentId, expand)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
		} catch (CourseValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		}
	}

	@PostMapping({ "/api/courses", "/api/courses/" })
	@ResponseStatus(HttpStatus.CREATED)
	public CourseDetailModel createCourse(@RequestBody CourseCreateRequestModel request) {
		try {
			return courseService.createCourse(request);
		} catch (CourseValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (CourseConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@PatchMapping({ "/api/courses/{id}", "/api/courses/{id}/" })
	public CourseModel updateCourseById(
			@PathVariable("id") Integer id,
			@RequestBody CoursePatchRequestModel request) {
		try {
			return courseService.updateCourse(id, request)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
		} catch (CourseValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (CourseConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@PutMapping({ "/api/courses/{id}/prerequisites", "/api/courses/{id}/prerequisites/" })
	public CourseDetailModel updateCoursePrerequisites(
			@PathVariable("id") Integer id,
			@RequestBody CoursePrerequisitesUpdateRequestModel request) {
		try {
			return courseService.updateCoursePrerequisites(id, request)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
		} catch (CourseValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (CourseConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}
}
