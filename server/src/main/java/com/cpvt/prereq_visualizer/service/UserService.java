package com.cpvt.prereq_visualizer.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpvt.prereq_visualizer.model.UserCreateRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserModel;
import com.cpvt.prereq_visualizer.model.UserPatchRequestModel;
import com.cpvt.prereq_visualizer.model.UserStudentRequestModel;
import com.cpvt.prereq_visualizer.repository.UserRepository;

@Service
public class UserService {

	private static final Set<String> ALLOWED_ROLES = Set.of("student", "advisor", "admin");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	// Kept as a service boundary so business rules can evolve without changing controllers.
	public List<UserModel> getAllUsers() {
		return userRepository.findAllUsers();
	}

	public Optional<UserDetailModel> getUserById(Integer userId) {
		return userRepository.findUserById(userId);
	}

	@Transactional
	public UserDetailModel createUser(UserCreateRequestModel request) {
		UserCreateCommand command = validateAndNormalizeCreateRequest(request);

		if (userRepository.findUserIdByEmail(command.email()).isPresent()) {
			throw new UserConflictException("Email already exists: " + command.email());
		}

		if (command.student() != null && command.student().schoolStudentId() != null
				&& userRepository.findStudentIdBySchoolStudentId(command.student().schoolStudentId()).isPresent()) {
			throw new UserConflictException(
					"school_student_id already exists: " + command.student().schoolStudentId());
		}

		String passwordHash = passwordEncoder.encode(command.password());
		Integer userId = userRepository.insertUser(
				command.name(),
				command.email(),
				passwordHash,
				command.role());

		if (command.student() != null) {
			userRepository.insertStudent(
					userId,
					command.student().schoolStudentId(),
					command.student().major());
		}

		return userRepository.findUserById(userId)
				.orElseThrow(() -> new IllegalStateException("Created user could not be loaded: " + userId));
	}

	@Transactional
	public Optional<UserDetailModel> updateUser(Integer userId, UserPatchRequestModel request) {
		Optional<UserDetailModel> existingUser = userRepository.findUserById(userId);
		if (existingUser.isEmpty()) {
			return Optional.empty();
		}

		UserPatchCommand command = validateAndNormalizePatchRequest(request);
		UserDetailModel currentUser = existingUser.get();

		String nextName = command.hasName() ? command.name() : currentUser.getName();
		String nextEmail = command.hasEmail() ? command.email() : currentUser.getEmail();
		String nextRole = command.hasRole() ? command.role() : currentUser.getRole();

		if (command.hasEmail()) {
			Optional<Integer> existingUserIdForEmail = userRepository.findUserIdByEmail(nextEmail);
			if (existingUserIdForEmail.isPresent() && !userId.equals(existingUserIdForEmail.get())) {
				throw new UserConflictException("Email already exists: " + nextEmail);
			}
		}

		if (command.hasRole() && !"student".equals(nextRole) && currentUser.getStudent() != null) {
			throw new UserValidationException(
					"Cannot change role away from student while a linked student profile exists");
		}

		int updatedRows = userRepository.updateUserBasicFields(userId, nextName, nextEmail, nextRole);
		if (updatedRows == 0) {
			return Optional.empty();
		}

		return userRepository.findUserById(userId);
	}

	@Transactional
	public boolean deleteUser(Integer userId) {
		return userRepository.deleteUserById(userId) > 0;
	}

	private UserCreateCommand validateAndNormalizeCreateRequest(UserCreateRequestModel request) {
		if (request == null) {
			throw new UserValidationException("Request body is required");
		}

		String name = requireTrimmedText(request.getName(), "name");
		String email = normalizeEmail(request.getEmail());
		String password = requireTrimmedText(request.getPassword(), "password");
		String role = normalizeRole(request.getRole());

		UserStudentCommand student = validateAndNormalizeStudentRequest(request.getStudent());
		if (student != null && !"student".equals(role)) {
			throw new UserValidationException("student payload is only allowed when role is student");
		}

		return new UserCreateCommand(name, email, password, role, student);
	}

	private UserPatchCommand validateAndNormalizePatchRequest(UserPatchRequestModel request) {
		if (request == null) {
			throw new UserValidationException("Request body is required");
		}

		boolean hasName = request.getName() != null;
		boolean hasEmail = request.getEmail() != null;
		boolean hasRole = request.getRole() != null;

		if (!hasName && !hasEmail && !hasRole) {
			throw new UserValidationException("At least one updatable field must be provided");
		}

		String name = null;
		if (hasName) {
			name = requireTrimmedText(request.getName(), "name");
		}

		String email = null;
		if (hasEmail) {
			email = normalizeEmail(request.getEmail());
		}

		String role = null;
		if (hasRole) {
			role = normalizeRole(request.getRole());
		}

		return new UserPatchCommand(hasName, name, hasEmail, email, hasRole, role);
	}

	private UserStudentCommand validateAndNormalizeStudentRequest(UserStudentRequestModel studentRequest) {
		if (studentRequest == null) {
			return null;
		}

		String schoolStudentId = normalizeNullableTrimmedText(studentRequest.getSchoolStudentId());
		String major = normalizeNullableTrimmedText(studentRequest.getMajor());

		return new UserStudentCommand(schoolStudentId, major);
	}

	private String normalizeEmail(String value) {
		String email = requireTrimmedText(value, "email").toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new UserValidationException("email format is invalid");
		}

		return email;
	}

	private String normalizeRole(String value) {
		String role = requireTrimmedText(value, "role").toLowerCase(Locale.ROOT);
		if (!ALLOWED_ROLES.contains(role)) {
			throw new UserValidationException("Invalid role: " + role);
		}

		return role;
	}

	private String normalizeNullableTrimmedText(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String requireTrimmedText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new UserValidationException(fieldName + " is required");
		}

		return value.trim();
	}

	private record UserStudentCommand(String schoolStudentId, String major) {
	}

	private record UserCreateCommand(
			String name,
			String email,
			String password,
			String role,
			UserStudentCommand student) {
	}

	private record UserPatchCommand(
			boolean hasName,
			String name,
			boolean hasEmail,
			String email,
			boolean hasRole,
			String role) {
	}
}
