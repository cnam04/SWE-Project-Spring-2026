package com.cpvt.prereq_visualizer.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cpvt.prereq_visualizer.model.UserCreateRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserPatchRequestModel;
import com.cpvt.prereq_visualizer.model.UserStudentModel;
import com.cpvt.prereq_visualizer.model.UserStudentRequestModel;
import com.cpvt.prereq_visualizer.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	void getUserById_whenMissing_returnsEmpty() {
		when(userRepository.findUserById(999)).thenReturn(Optional.empty());

		Optional<UserDetailModel> result = userService.getUserById(999);

		assertTrue(result.isEmpty());
		verify(userRepository).findUserById(999);
	}

	@Test
	void createUser_whenEmailAlreadyExists_throwsConflict() {
		UserCreateRequestModel request = new UserCreateRequestModel(
				"Cole Nam",
				"cole@example.com",
				"plain-password",
				"student",
				null);

		when(userRepository.findUserIdByEmail("cole@example.com")).thenReturn(Optional.of(1));

		UserConflictException exception = assertThrows(
				UserConflictException.class,
				() -> userService.createUser(request));

		assertEquals("Email already exists: cole@example.com", exception.getMessage());
		verify(userRepository, never()).insertUser(anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void createUser_whenRoleIsNotStudentAndStudentPayloadProvided_throwsValidation() {
		UserCreateRequestModel request = new UserCreateRequestModel(
				"Dr. Advisor",
				"advisor@example.com",
				"plain-password",
				"advisor",
				new UserStudentRequestModel("NP100002", "Computer Science"));

		UserValidationException exception = assertThrows(
				UserValidationException.class,
				() -> userService.createUser(request));

		assertEquals("student payload is only allowed when role is student", exception.getMessage());
		verify(userRepository, never()).insertUser(anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void createUser_whenSchoolStudentIdConflicts_throwsConflict() {
		UserCreateRequestModel request = new UserCreateRequestModel(
				"Cole Nam",
				"cole@example.com",
				"plain-password",
				"student",
				new UserStudentRequestModel("NP100001", "Computer Science"));

		when(userRepository.findUserIdByEmail("cole@example.com")).thenReturn(Optional.empty());
		when(userRepository.findStudentIdBySchoolStudentId("NP100001")).thenReturn(Optional.of(1));

		UserConflictException exception = assertThrows(
				UserConflictException.class,
				() -> userService.createUser(request));

		assertEquals("school_student_id already exists: NP100001", exception.getMessage());
		verify(userRepository, never()).insertUser(anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void createUser_withStudentPayload_insertsUserAndStudent() {
		UserCreateRequestModel request = new UserCreateRequestModel(
				"Cole Nam",
				"Cole@Example.com",
				"plain-password",
				"student",
				new UserStudentRequestModel("NP100001", "Computer Science"));

		UserDetailModel persisted = new UserDetailModel(
				7,
				"Cole Nam",
				"cole@example.com",
				"student",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				new UserStudentModel(4, "NP100001", "Computer Science"));

		when(userRepository.findUserIdByEmail("cole@example.com")).thenReturn(Optional.empty());
		when(userRepository.findStudentIdBySchoolStudentId("NP100001")).thenReturn(Optional.empty());
		when(userRepository.insertUser(anyString(), anyString(), anyString(), anyString())).thenReturn(7);
		when(userRepository.findUserById(7)).thenReturn(Optional.of(persisted));

		UserDetailModel result = userService.createUser(request);

		assertEquals(7, result.getUserId());
		assertEquals("cole@example.com", result.getEmail());
		assertEquals("NP100001", result.getStudent().getSchoolStudentId());

		verify(userRepository).insertUser(
				eq("Cole Nam"),
				eq("cole@example.com"),
				argThat(hash -> hash != null && !hash.equals("plain-password")),
				eq("student"));
		verify(userRepository).insertStudent(7, "NP100001", "Computer Science");
	}

	@Test
	void createUser_withoutStudentPayload_doesNotInsertStudent() {
		UserCreateRequestModel request = new UserCreateRequestModel(
				"Cole Nam",
				"cole@example.com",
				"plain-password",
				"student",
				null);

		UserDetailModel persisted = new UserDetailModel(
				8,
				"Cole Nam",
				"cole@example.com",
				"student",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				null);

		when(userRepository.findUserIdByEmail("cole@example.com")).thenReturn(Optional.empty());
		when(userRepository.insertUser(anyString(), anyString(), anyString(), anyString())).thenReturn(8);
		when(userRepository.findUserById(8)).thenReturn(Optional.of(persisted));

		UserDetailModel result = userService.createUser(request);

		assertEquals(8, result.getUserId());
		assertEquals("cole@example.com", result.getEmail());
		verify(userRepository, never()).insertStudent(eq(8), anyString(), anyString());
	}

	@Test
	void updateUser_whenMissing_returnsEmpty() {
		when(userRepository.findUserById(999)).thenReturn(Optional.empty());

		Optional<UserDetailModel> result = userService.updateUser(
				999,
				new UserPatchRequestModel("Updated Name", null, null));

		assertTrue(result.isEmpty());
		verify(userRepository, never()).updateUserBasicFields(999, "Updated Name", null, null);
	}

	@Test
	void updateUser_whenNoFieldsProvided_throwsValidation() {
		when(userRepository.findUserById(1)).thenReturn(Optional.of(
				new UserDetailModel(
						1,
						"Cole Nam",
						"cole@example.com",
						"student",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						null)));

		UserValidationException exception = assertThrows(
				UserValidationException.class,
				() -> userService.updateUser(1, new UserPatchRequestModel(null, null, null)));

		assertEquals("At least one updatable field must be provided", exception.getMessage());
		verify(userRepository, never()).updateUserBasicFields(anyInt(), anyString(), anyString(), anyString());
	}

	@Test
	void updateUser_whenEmailConflicts_throwsConflict() {
		when(userRepository.findUserById(1)).thenReturn(Optional.of(
				new UserDetailModel(
						1,
						"Cole Nam",
						"cole@example.com",
						"student",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						null)));
		when(userRepository.findUserIdByEmail("advisor@example.com")).thenReturn(Optional.of(2));

		UserConflictException exception = assertThrows(
				UserConflictException.class,
				() -> userService.updateUser(1, new UserPatchRequestModel(null, "advisor@example.com", null)));

		assertEquals("Email already exists: advisor@example.com", exception.getMessage());
		verify(userRepository, never()).updateUserBasicFields(eq(1), anyString(), anyString(), anyString());
	}

	@Test
	void updateUser_whenChangingRoleAwayFromStudentWithLinkedProfile_throwsValidation() {
		when(userRepository.findUserById(1)).thenReturn(Optional.of(
				new UserDetailModel(
						1,
						"Cole Nam",
						"cole@example.com",
						"student",
						LocalDateTime.of(2026, 1, 10, 9, 0),
						new UserStudentModel(1, "NP100001", "Computer Science"))));

		UserValidationException exception = assertThrows(
				UserValidationException.class,
				() -> userService.updateUser(1, new UserPatchRequestModel(null, null, "advisor")));

		assertEquals(
				"Cannot change role away from student while a linked student profile exists",
				exception.getMessage());
		verify(userRepository, never()).updateUserBasicFields(eq(1), anyString(), anyString(), anyString());
	}

	@Test
	void updateUser_withValidPatch_updatesAndReturnsUser() {
		UserDetailModel existing = new UserDetailModel(
				5,
				"Dr. Advisor",
				"advisor@example.com",
				"advisor",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				null);

		UserDetailModel updated = new UserDetailModel(
				5,
				"Dr. Advisor Updated",
				"advisor-updated@example.com",
				"advisor",
				LocalDateTime.of(2026, 1, 10, 9, 0),
				null);

		when(userRepository.findUserById(5))
				.thenReturn(Optional.of(existing))
				.thenReturn(Optional.of(updated));
		when(userRepository.findUserIdByEmail("advisor-updated@example.com")).thenReturn(Optional.empty());
		when(userRepository.updateUserBasicFields(
				5,
				"Dr. Advisor Updated",
				"advisor-updated@example.com",
				"advisor")).thenReturn(1);

		UserDetailModel result = userService.updateUser(
				5,
				new UserPatchRequestModel("Dr. Advisor Updated", "advisor-updated@example.com", null))
				.orElseThrow();

		assertEquals(5, result.getUserId());
		assertEquals("Dr. Advisor Updated", result.getName());
		assertEquals("advisor-updated@example.com", result.getEmail());
	}

	@Test
	void deleteUser_whenPresent_returnsTrue() {
		when(userRepository.deleteUserById(3)).thenReturn(1);

		boolean result = userService.deleteUser(3);

		assertTrue(result);
	}

	@Test
	void deleteUser_whenMissing_returnsFalse() {
		when(userRepository.deleteUserById(404)).thenReturn(0);

		boolean result = userService.deleteUser(404);

		assertFalse(result);
	}
}