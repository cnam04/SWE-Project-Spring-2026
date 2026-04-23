package com.cpvt.prereq_visualizer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.cpvt.prereq_visualizer.model.StudentCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.model.StudentPatchRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserStudentModel;
import com.cpvt.prereq_visualizer.repository.StudentRepository;
import com.cpvt.prereq_visualizer.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private UserRepository userRepository;

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
}