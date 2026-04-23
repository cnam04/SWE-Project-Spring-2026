package com.cpvt.prereq_visualizer.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpvt.prereq_visualizer.model.StudentCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.model.StudentPatchRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.repository.StudentRepository;
import com.cpvt.prereq_visualizer.repository.UserRepository;

@Service
public class StudentService {

	private final StudentRepository studentRepository;
	private final UserRepository userRepository;

	public StudentService(StudentRepository studentRepository, UserRepository userRepository) {
		this.studentRepository = studentRepository;
		this.userRepository = userRepository;
	}

	public List<StudentModel> getAllStudents(String name, String schoolStudentId, String email) {
		String normalizedName = normalizeNullableTrimmedText(name);
		String normalizedSchoolStudentId = normalizeNullableTrimmedText(schoolStudentId);
		String normalizedEmail = normalizeNullableTrimmedText(email);

		return studentRepository.findAllStudents(normalizedName, normalizedSchoolStudentId, normalizedEmail);
	}

	public Optional<StudentModel> getStudentById(Integer studentId) {
		return studentRepository.findStudentById(studentId);
	}

	@Transactional
	public StudentModel createStudent(StudentCreateRequestModel request) {
		StudentCreateCommand command = validateAndNormalizeCreateRequest(request);

		UserDetailModel referencedUser = userRepository.findUserById(command.userId())
				.orElseThrow(() -> new StudentValidationException(
						"Referenced user not found: " + command.userId()));

		if (!"student".equals(referencedUser.getRole())) {
			throw new StudentValidationException("Referenced user role must be student");
		}

		if (referencedUser.getStudent() != null) {
			throw new StudentConflictException(
					"User already has a linked student profile: " + command.userId());
		}

		if (command.schoolStudentId() != null
				&& studentRepository.findStudentIdBySchoolStudentId(command.schoolStudentId()).isPresent()) {
			throw new StudentConflictException("school_student_id already exists: " + command.schoolStudentId());
		}

		Integer studentId = studentRepository.insertStudent(
				command.userId(),
				command.schoolStudentId(),
				command.major());

		return studentRepository.findStudentById(studentId)
				.orElseThrow(() -> new IllegalStateException("Created student could not be loaded: " + studentId));
	}

	@Transactional
	public Optional<StudentModel> updateStudent(Integer studentId, StudentPatchRequestModel request) {
		Optional<StudentModel> existingStudent = studentRepository.findStudentById(studentId);
		if (existingStudent.isEmpty()) {
			return Optional.empty();
		}

		StudentPatchCommand command = validateAndNormalizePatchRequest(request);
		StudentModel currentStudent = existingStudent.get();

		String nextSchoolStudentId = command.hasSchoolStudentId()
				? command.schoolStudentId()
				: currentStudent.getSchoolStudentId();
		String nextMajor = command.hasMajor() ? command.major() : currentStudent.getMajor();

		if (command.hasSchoolStudentId() && nextSchoolStudentId != null) {
			Optional<Integer> existingStudentIdForSchoolId = studentRepository
					.findStudentIdBySchoolStudentId(nextSchoolStudentId);
			if (existingStudentIdForSchoolId.isPresent() && !studentId.equals(existingStudentIdForSchoolId.get())) {
				throw new StudentConflictException("school_student_id already exists: " + nextSchoolStudentId);
			}
		}

		int updatedRows = studentRepository.updateStudentFields(studentId, nextSchoolStudentId, nextMajor);
		if (updatedRows == 0) {
			return Optional.empty();
		}

		return studentRepository.findStudentById(studentId);
	}

	private StudentCreateCommand validateAndNormalizeCreateRequest(StudentCreateRequestModel request) {
		if (request == null) {
			throw new StudentValidationException("Request body is required");
		}

		if (request.getUserId() == null) {
			throw new StudentValidationException("user_id is required");
		}

		if (request.getUserId() <= 0) {
			throw new StudentValidationException("user_id must be positive");
		}

		String schoolStudentId = normalizeNullableTrimmedText(request.getSchoolStudentId());
		String major = normalizeNullableTrimmedText(request.getMajor());

		return new StudentCreateCommand(request.getUserId(), schoolStudentId, major);
	}

	private StudentPatchCommand validateAndNormalizePatchRequest(StudentPatchRequestModel request) {
		if (request == null) {
			throw new StudentValidationException("Request body is required");
		}

		boolean hasSchoolStudentId = request.getSchoolStudentId() != null;
		boolean hasMajor = request.getMajor() != null;

		if (!hasSchoolStudentId && !hasMajor) {
			throw new StudentValidationException("At least one updatable field must be provided");
		}

		String schoolStudentId = null;
		if (hasSchoolStudentId) {
			schoolStudentId = normalizeNullableTrimmedText(request.getSchoolStudentId());
		}

		String major = null;
		if (hasMajor) {
			major = normalizeNullableTrimmedText(request.getMajor());
		}

		return new StudentPatchCommand(hasSchoolStudentId, schoolStudentId, hasMajor, major);
	}

	private String normalizeNullableTrimmedText(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private record StudentCreateCommand(Integer userId, String schoolStudentId, String major) {
	}

	private record StudentPatchCommand(
			boolean hasSchoolStudentId,
			String schoolStudentId,
			boolean hasMajor,
			String major) {
	}
}
