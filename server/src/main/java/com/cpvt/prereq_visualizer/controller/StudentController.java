package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cpvt.prereq_visualizer.model.StudentCourseRecordCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentCourseRecordModel;
import com.cpvt.prereq_visualizer.model.StudentCourseRecordPatchRequestModel;
import com.cpvt.prereq_visualizer.model.StudentCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.model.StudentPatchRequestModel;
import com.cpvt.prereq_visualizer.service.StudentConflictException;
import com.cpvt.prereq_visualizer.service.StudentService;
import com.cpvt.prereq_visualizer.service.StudentValidationException;

@RestController
public class StudentController {

	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping({ "/api/students", "/api/students/" })
	public List<StudentModel> getAllStudents(
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "schoolStudentId", required = false) String schoolStudentId,
			@RequestParam(value = "school_student_id", required = false) String schoolStudentIdSnakeCase,
			@RequestParam(value = "email", required = false) String email) {
		String effectiveSchoolStudentId = schoolStudentId != null ? schoolStudentId : schoolStudentIdSnakeCase;
		return studentService.getAllStudents(name, effectiveSchoolStudentId, email);
	}

	@GetMapping({ "/api/students/{id}", "/api/students/{id}/" })
	public StudentModel getStudentById(@PathVariable("id") Integer id) {
		return studentService.getStudentById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
	}

	@PostMapping({ "/api/students", "/api/students/" })
	@ResponseStatus(HttpStatus.CREATED)
	public StudentModel createStudent(@RequestBody StudentCreateRequestModel request) {
		try {
			return studentService.createStudent(request);
		} catch (StudentValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (StudentConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@PatchMapping({ "/api/students/{id}", "/api/students/{id}/" })
	public StudentModel updateStudent(
			@PathVariable("id") Integer id,
			@RequestBody StudentPatchRequestModel request) {
		try {
			return studentService.updateStudent(id, request)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
		} catch (StudentValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (StudentConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@GetMapping({ "/api/students/{id}/records", "/api/students/{id}/records/" })
	public List<StudentCourseRecordModel> getStudentCourseRecords(@PathVariable("id") Integer id) {
		try {
			return studentService.getStudentCourseRecords(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
		} catch (StudentValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		}
	}

	@PostMapping({ "/api/students/{id}/records", "/api/students/{id}/records/" })
	@ResponseStatus(HttpStatus.CREATED)
	public StudentCourseRecordModel createStudentCourseRecord(
			@PathVariable("id") Integer id,
			@RequestBody StudentCourseRecordCreateRequestModel request) {
		try {
			return studentService.createStudentCourseRecord(id, request)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
		} catch (StudentValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (StudentConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@PatchMapping({ "/api/students/{id}/records/{recordId}", "/api/students/{id}/records/{recordId}/" })
	public StudentCourseRecordModel updateStudentCourseRecord(
			@PathVariable("id") Integer id,
			@PathVariable("recordId") Integer recordId,
			@RequestBody StudentCourseRecordPatchRequestModel request) {
		try {
			return studentService.updateStudentCourseRecord(id, recordId, request)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student record not found"));
		} catch (StudentValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (StudentConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@DeleteMapping({ "/api/students/{id}/records/{recordId}", "/api/students/{id}/records/{recordId}/" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteStudentCourseRecord(
			@PathVariable("id") Integer id,
			@PathVariable("recordId") Integer recordId) {
		try {
			if (!studentService.deleteStudentCourseRecord(id, recordId)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student record not found");
			}
		} catch (StudentValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		}
	}
}