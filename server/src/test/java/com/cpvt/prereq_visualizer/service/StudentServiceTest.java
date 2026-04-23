package com.cpvt.prereq_visualizer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cpvt.prereq_visualizer.model.StudentCourseRecordCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentCourseRecordModel;
import com.cpvt.prereq_visualizer.model.StudentCourseRecordPatchRequestModel;
import com.cpvt.prereq_visualizer.model.CourseWithRootPrerequisiteModel;
import com.cpvt.prereq_visualizer.model.StudentCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.model.StudentPatchRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserStudentModel;
import com.cpvt.prereq_visualizer.repository.CourseRepository;
import com.cpvt.prereq_visualizer.repository.StudentRepository;
import com.cpvt.prereq_visualizer.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CourseRepository courseRepository;

	@InjectMocks
	private StudentService studentService;

	@Test
	void getAllStudents_normalizesFiltersAndReturnsResults() {
		StudentModel student = new StudentModel(
				1,
				1,
				"Cole Nam",
				"cole@example.com",
				"NP100001",
				"Computer Science");

		when(studentRepository.findAllStudents("Cole", "NP100001", "example.com"))
				.thenReturn(List.of(student));

		List<StudentModel> results = studentService.getAllStudents("  Cole ", " NP100001 ", " example.com ");

		assertEquals(1, results.size());
		verify(studentRepository).findAllStudents("Cole", "NP100001", "example.com");
	}

	@Test
	void getStudentById_whenMissing_returnsEmpty() {
		when(studentRepository.findStudentById(999)).thenReturn(Optional.empty());

		Optional<StudentModel> result = studentService.getStudentById(999);

		assertTrue(result.isEmpty());
	}

	@Test
	void createStudent_whenUserMissing_throwsValidation() {
		StudentCreateRequestModel request = new StudentCreateRequestModel(999, "NP100999", "Computer Science");

		when(userRepository.findUserById(999)).thenReturn(Optional.empty());

		StudentValidationException exception = assertThrows(
				StudentValidationException.class,
				() -> studentService.createStudent(request));

		assertEquals("Referenced user not found: 999", exception.getMessage());
		verify(studentRepository, never()).insertStudent(anyInt(), anyString(), anyString());
	}

	@Test
	void createStudent_whenReferencedUserRoleIsNotStudent_throwsValidation() {
		StudentCreateRequestModel request = new StudentCreateRequestModel(2, "NP100777", "Computer Science");

		when(userRepository.findUserById(2)).thenReturn(Optional.of(
				new UserDetailModel(
						2,
						"Dr. Advisor",
						"advisor@example.com",
						"advisor",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						null)));

		StudentValidationException exception = assertThrows(
				StudentValidationException.class,
				() -> studentService.createStudent(request));

		assertEquals("Referenced user role must be student", exception.getMessage());
		verify(studentRepository, never()).insertStudent(anyInt(), anyString(), anyString());
	}

	@Test
	void createStudent_whenUserAlreadyLinked_throwsConflict() {
		StudentCreateRequestModel request = new StudentCreateRequestModel(1, "NP100777", "Computer Science");

		when(userRepository.findUserById(1)).thenReturn(Optional.of(
				new UserDetailModel(
						1,
						"Cole Nam",
						"cole@example.com",
						"student",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						new UserStudentModel(1, "NP100001", "Computer Science"))));

		StudentConflictException exception = assertThrows(
				StudentConflictException.class,
				() -> studentService.createStudent(request));

		assertEquals("User already has a linked student profile: 1", exception.getMessage());
		verify(studentRepository, never()).insertStudent(anyInt(), anyString(), anyString());
	}

	@Test
	void createStudent_whenSchoolStudentIdConflicts_throwsConflict() {
		StudentCreateRequestModel request = new StudentCreateRequestModel(7, "NP100001", "Computer Science");

		when(userRepository.findUserById(7)).thenReturn(Optional.of(
				new UserDetailModel(
						7,
						"New Student",
						"new-student@example.com",
						"student",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						null)));
		when(studentRepository.findStudentIdBySchoolStudentId("NP100001")).thenReturn(Optional.of(1));

		StudentConflictException exception = assertThrows(
				StudentConflictException.class,
				() -> studentService.createStudent(request));

		assertEquals("school_student_id already exists: NP100001", exception.getMessage());
		verify(studentRepository, never()).insertStudent(anyInt(), anyString(), anyString());
	}

	@Test
	void createStudent_withValidRequest_createsAndReturnsStudent() {
		StudentCreateRequestModel request = new StudentCreateRequestModel(7, "NP100042", "Computer Science");

		StudentModel created = new StudentModel(
				4,
				7,
				"New Student",
				"new-student@example.com",
				"NP100042",
				"Computer Science");

		when(userRepository.findUserById(7)).thenReturn(Optional.of(
				new UserDetailModel(
						7,
						"New Student",
						"new-student@example.com",
						"student",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						null)));
		when(studentRepository.findStudentIdBySchoolStudentId("NP100042")).thenReturn(Optional.empty());
		when(studentRepository.insertStudent(7, "NP100042", "Computer Science")).thenReturn(4);
		when(studentRepository.findStudentById(4)).thenReturn(Optional.of(created));

		StudentModel result = studentService.createStudent(request);

		assertEquals(4, result.getStudentId());
		assertEquals(7, result.getUserId());
		assertEquals("NP100042", result.getSchoolStudentId());
	}

	@Test
	void updateStudent_whenMissing_returnsEmpty() {
		when(studentRepository.findStudentById(999)).thenReturn(Optional.empty());

		Optional<StudentModel> result = studentService.updateStudent(
				999,
				new StudentPatchRequestModel("NP100333", null));

		assertTrue(result.isEmpty());
		verify(studentRepository, never()).updateStudentFields(anyInt(), anyString(), anyString());
	}

	@Test
	void updateStudent_whenNoFieldsProvided_throwsValidation() {
		when(studentRepository.findStudentById(1)).thenReturn(Optional.of(
				new StudentModel(
						1,
						1,
						"Cole Nam",
						"cole@example.com",
						"NP100001",
						"Computer Science")));

		StudentValidationException exception = assertThrows(
				StudentValidationException.class,
				() -> studentService.updateStudent(1, new StudentPatchRequestModel(null, null)));

		assertEquals("At least one updatable field must be provided", exception.getMessage());
		verify(studentRepository, never()).updateStudentFields(anyInt(), anyString(), anyString());
	}

	@Test
	void updateStudent_whenSchoolStudentIdConflicts_throwsConflict() {
		when(studentRepository.findStudentById(2)).thenReturn(Optional.of(
				new StudentModel(
						2,
						7,
						"New Student",
						"new-student@example.com",
						"NP100042",
						"Computer Science")));
		when(studentRepository.findStudentIdBySchoolStudentId("NP100001")).thenReturn(Optional.of(1));

		StudentConflictException exception = assertThrows(
				StudentConflictException.class,
				() -> studentService.updateStudent(2, new StudentPatchRequestModel("NP100001", null)));

		assertEquals("school_student_id already exists: NP100001", exception.getMessage());
		verify(studentRepository, never()).updateStudentFields(eq(2), anyString(), anyString());
	}

	@Test
	void updateStudent_withValidPatch_updatesAndReturnsStudent() {
		StudentModel existing = new StudentModel(
				2,
				7,
				"New Student",
				"new-student@example.com",
				"NP100042",
				"Computer Science");

		StudentModel updated = new StudentModel(
				2,
				7,
				"New Student",
				"new-student@example.com",
				"NP100333",
				"Software Engineering");

		when(studentRepository.findStudentById(2))
				.thenReturn(Optional.of(existing))
				.thenReturn(Optional.of(updated));
		when(studentRepository.findStudentIdBySchoolStudentId("NP100333")).thenReturn(Optional.empty());
		when(studentRepository.updateStudentFields(2, "NP100333", "Software Engineering")).thenReturn(1);

		StudentModel result = studentService.updateStudent(
				2,
				new StudentPatchRequestModel("NP100333", "Software Engineering"))
				.orElseThrow();

		assertEquals(2, result.getStudentId());
		assertEquals("NP100333", result.getSchoolStudentId());
		assertEquals("Software Engineering", result.getMajor());
	}

	@Test
	void getStudentCourseRecords_whenStudentMissing_returnsEmpty() {
		when(studentRepository.findStudentById(999)).thenReturn(Optional.empty());

		Optional<List<StudentCourseRecordModel>> result = studentService.getStudentCourseRecords(999);

		assertTrue(result.isEmpty());
		verify(studentRepository, never()).findStudentCourseRecordsByStudentId(999);
	}

	@Test
	void createStudentCourseRecord_whenDuplicateCourseRecord_throwsConflict() {
		when(studentRepository.findStudentById(1)).thenReturn(Optional.of(
				new StudentModel(1, 1, "Cole Nam", "cole@example.com", "NP100001", "Computer Science")));
		when(courseRepository.findCourseWithRootPrerequisiteById(9)).thenReturn(Optional.of(
				new CourseWithRootPrerequisiteModel(
						9,
						"CPS320",
						"10009",
						"Database Systems",
						3,
						List.of("Advanced Core"),
						null)));
		when(studentRepository.findRecordIdByStudentIdAndCourseId(1, 9)).thenReturn(Optional.of(8));

		StudentCourseRecordCreateRequestModel request = new StudentCourseRecordCreateRequestModel(
				9,
				"planned",
				null,
				"Fall",
				2026);

		StudentConflictException exception = assertThrows(
				StudentConflictException.class,
				() -> studentService.createStudentCourseRecord(1, request));

		assertEquals("Student already has a record for course_id: 9", exception.getMessage());
		verify(studentRepository, never()).insertStudentCourseRecord(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyInt());
	}

	@Test
	void createStudentCourseRecord_withValidRequest_createsRecord() {
		StudentCourseRecordModel createdRecord = new StudentCourseRecordModel(
				9,
				1,
				10,
				"CPS330",
				"10010",
				"Operating Systems",
				3,
				List.of("Advanced Core"),
				"planned",
				null,
				"Fall",
				2026);

		when(studentRepository.findStudentById(1)).thenReturn(Optional.of(
				new StudentModel(1, 1, "Cole Nam", "cole@example.com", "NP100001", "Computer Science")));
		when(courseRepository.findCourseWithRootPrerequisiteById(10)).thenReturn(Optional.of(
				new CourseWithRootPrerequisiteModel(
						10,
						"CPS330",
						"10010",
						"Operating Systems",
						3,
						List.of("Advanced Core"),
						null)));
		when(studentRepository.findRecordIdByStudentIdAndCourseId(1, 10)).thenReturn(Optional.empty());
		when(studentRepository.insertStudentCourseRecord(1, 10, "planned", null, "Fall", 2026)).thenReturn(9);
		when(studentRepository.findStudentCourseRecordById(1, 9)).thenReturn(Optional.of(createdRecord));

		StudentCourseRecordCreateRequestModel request = new StudentCourseRecordCreateRequestModel(
				10,
				"planned",
				null,
				"Fall",
				2026);

		StudentCourseRecordModel result = studentService.createStudentCourseRecord(1, request).orElseThrow();

		assertEquals(9, result.getRecordId());
		assertEquals(10, result.getCourseId());
		assertEquals("planned", result.getStatus());
	}

	@Test
	void updateStudentCourseRecord_whenStatusChangesWithoutClearingGrade_throwsValidation() {
		StudentCourseRecordModel existingRecord = new StudentCourseRecordModel(
				7,
				1,
				6,
				"CPS210",
				"10006",
				"Data Structures",
				4,
				List.of("Core"),
				"completed",
				"A",
				"Fall",
				2025);

		when(studentRepository.findStudentById(1)).thenReturn(Optional.of(
				new StudentModel(1, 1, "Cole Nam", "cole@example.com", "NP100001", "Computer Science")));
		when(studentRepository.findStudentCourseRecordById(1, 7)).thenReturn(Optional.of(existingRecord));

		StudentCourseRecordPatchRequestModel patchRequest = new StudentCourseRecordPatchRequestModel();
		patchRequest.setStatus("planned");

		StudentValidationException exception = assertThrows(
				StudentValidationException.class,
				() -> studentService.updateStudentCourseRecord(1, 7, patchRequest));

		assertEquals("grade must be null unless status is completed", exception.getMessage());
		verify(studentRepository, never()).updateStudentCourseRecord(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyInt());
	}

	@Test
	void deleteStudentCourseRecord_whenStudentMissing_returnsFalse() {
		when(studentRepository.findStudentById(999)).thenReturn(Optional.empty());

		boolean deleted = studentService.deleteStudentCourseRecord(999, 42);

		assertFalse(deleted);
		verify(studentRepository, never()).deleteStudentCourseRecord(anyInt(), anyInt());
	}
}