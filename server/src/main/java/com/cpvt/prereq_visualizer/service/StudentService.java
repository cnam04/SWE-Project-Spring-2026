package com.cpvt.prereq_visualizer.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpvt.prereq_visualizer.model.StudentCourseRecordCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentCourseRecordModel;
import com.cpvt.prereq_visualizer.model.StudentCourseRecordPatchRequestModel;
import com.cpvt.prereq_visualizer.model.StudentCreateRequestModel;
import com.cpvt.prereq_visualizer.model.StudentModel;
import com.cpvt.prereq_visualizer.model.StudentPatchRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.repository.CourseRepository;
import com.cpvt.prereq_visualizer.repository.StudentRepository;
import com.cpvt.prereq_visualizer.repository.UserRepository;

@Service
public class StudentService {

	private static final Set<String> ALLOWED_RECORD_STATUSES = Set.of("completed", "in_progress", "planned");

	private final StudentRepository studentRepository;
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;

	public StudentService(
			StudentRepository studentRepository,
			UserRepository userRepository,
			CourseRepository courseRepository) {
		this.studentRepository = studentRepository;
		this.userRepository = userRepository;
		this.courseRepository = courseRepository;
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
		Integer normalizedStudentId = requirePositiveId(studentId, "student_id");

		Optional<StudentModel> existingStudent = studentRepository.findStudentById(normalizedStudentId);
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
			if (existingStudentIdForSchoolId.isPresent() && !normalizedStudentId.equals(existingStudentIdForSchoolId.get())) {
				throw new StudentConflictException("school_student_id already exists: " + nextSchoolStudentId);
			}
		}

		int updatedRows = studentRepository.updateStudentFields(normalizedStudentId, nextSchoolStudentId, nextMajor);
		if (updatedRows == 0) {
			return Optional.empty();
		}

		return studentRepository.findStudentById(normalizedStudentId);
	}

	public Optional<List<StudentCourseRecordModel>> getStudentCourseRecords(Integer studentId) {
		Integer normalizedStudentId = requirePositiveId(studentId, "student_id");

		if (studentRepository.findStudentById(normalizedStudentId).isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(studentRepository.findStudentCourseRecordsByStudentId(normalizedStudentId));
	}

	@Transactional
	public Optional<StudentCourseRecordModel> createStudentCourseRecord(
			Integer studentId,
			StudentCourseRecordCreateRequestModel request) {
		Integer normalizedStudentId = requirePositiveId(studentId, "student_id");

		if (studentRepository.findStudentById(normalizedStudentId).isEmpty()) {
			return Optional.empty();
		}

		StudentCourseRecordCreateCommand command = validateAndNormalizeStudentCourseRecordCreateRequest(request);
		ensureCourseExists(command.courseId());

		if (studentRepository.findRecordIdByStudentIdAndCourseId(normalizedStudentId, command.courseId()).isPresent()) {
			throw new StudentConflictException("Student already has a record for course_id: " + command.courseId());
		}

		Integer createdRecordId = studentRepository.insertStudentCourseRecord(
				normalizedStudentId,
				command.courseId(),
				command.status(),
				command.grade(),
				command.semesterTaken(),
				command.yearTaken());

		return studentRepository.findStudentCourseRecordById(normalizedStudentId, createdRecordId);
	}

	@Transactional
	public Optional<StudentCourseRecordModel> updateStudentCourseRecord(
			Integer studentId,
			Integer recordId,
			StudentCourseRecordPatchRequestModel request) {
		Integer normalizedStudentId = requirePositiveId(studentId, "student_id");
		Integer normalizedRecordId = requirePositiveId(recordId, "record_id");

		if (studentRepository.findStudentById(normalizedStudentId).isEmpty()) {
			return Optional.empty();
		}

		Optional<StudentCourseRecordModel> existingRecord = studentRepository
				.findStudentCourseRecordById(normalizedStudentId, normalizedRecordId);
		if (existingRecord.isEmpty()) {
			return Optional.empty();
		}

		StudentCourseRecordPatchCommand command = validateAndNormalizeStudentCourseRecordPatchRequest(request);
		StudentCourseRecordModel currentRecord = existingRecord.get();

		String nextStatus = command.hasStatus() ? command.status() : currentRecord.getStatus();
		String nextGrade = command.hasGrade() ? command.grade() : currentRecord.getGrade();
		String nextSemesterTaken = command.hasSemesterTaken() ? command.semesterTaken() : currentRecord.getSemesterTaken();
		Integer nextYearTaken = command.hasYearTaken() ? command.yearTaken() : currentRecord.getYearTaken();

		validateRecordStatusAndGrade(nextStatus, nextGrade);

		int updatedRows = studentRepository.updateStudentCourseRecord(
				normalizedStudentId,
				normalizedRecordId,
				nextStatus,
				nextGrade,
				nextSemesterTaken,
				nextYearTaken);

		if (updatedRows == 0) {
			return Optional.empty();
		}

		return studentRepository.findStudentCourseRecordById(normalizedStudentId, normalizedRecordId);
	}

	@Transactional
	public boolean deleteStudentCourseRecord(Integer studentId, Integer recordId) {
		Integer normalizedStudentId = requirePositiveId(studentId, "student_id");
		Integer normalizedRecordId = requirePositiveId(recordId, "record_id");

		if (studentRepository.findStudentById(normalizedStudentId).isEmpty()) {
			return false;
		}

		return studentRepository.deleteStudentCourseRecord(normalizedStudentId, normalizedRecordId) > 0;
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

	private StudentCourseRecordCreateCommand validateAndNormalizeStudentCourseRecordCreateRequest(
			StudentCourseRecordCreateRequestModel request) {
		if (request == null) {
			throw new StudentValidationException("Request body is required");
		}

		Integer courseId = requirePositiveId(request.getCourseId(), "course_id");
		String status = normalizeStudentCourseRecordStatus(request.getStatus());
		String grade = normalizeNullableTrimmedText(request.getGrade());
		String semesterTaken = normalizeNullableTrimmedText(request.getSemesterTaken());
		Integer yearTaken = normalizeNullableYear(request.getYearTaken());

		validateRecordStatusAndGrade(status, grade);

		return new StudentCourseRecordCreateCommand(courseId, status, grade, semesterTaken, yearTaken);
	}

	private StudentCourseRecordPatchCommand validateAndNormalizeStudentCourseRecordPatchRequest(
			StudentCourseRecordPatchRequestModel request) {
		if (request == null) {
			throw new StudentValidationException("Request body is required");
		}

		boolean hasStatus = request.hasStatus();
		boolean hasGrade = request.hasGrade();
		boolean hasSemesterTaken = request.hasSemesterTaken();
		boolean hasYearTaken = request.hasYearTaken();

		if (!hasStatus && !hasGrade && !hasSemesterTaken && !hasYearTaken) {
			throw new StudentValidationException("At least one updatable field must be provided");
		}

		String status = null;
		if (hasStatus) {
			status = normalizeStudentCourseRecordStatus(request.getStatus());
		}

		String grade = null;
		if (hasGrade) {
			grade = normalizeNullableTrimmedText(request.getGrade());
		}

		String semesterTaken = null;
		if (hasSemesterTaken) {
			semesterTaken = normalizeNullableTrimmedText(request.getSemesterTaken());
		}

		Integer yearTaken = null;
		if (hasYearTaken) {
			yearTaken = normalizeNullableYear(request.getYearTaken());
		}

		return new StudentCourseRecordPatchCommand(
				hasStatus,
				status,
				hasGrade,
				grade,
				hasSemesterTaken,
				semesterTaken,
				hasYearTaken,
				yearTaken);
	}

	private void ensureCourseExists(Integer courseId) {
		if (courseRepository.findCourseWithRootPrerequisiteById(courseId).isEmpty()) {
			throw new StudentValidationException("Referenced course not found: " + courseId);
		}
	}

	private void validateRecordStatusAndGrade(String status, String grade) {
		if (!"completed".equals(status) && grade != null) {
			throw new StudentValidationException("grade must be null unless status is completed");
		}
	}

	private String normalizeStudentCourseRecordStatus(String status) {
		String normalizedStatus = requireTrimmedText(status, "status").toLowerCase(Locale.ROOT);
		if (!ALLOWED_RECORD_STATUSES.contains(normalizedStatus)) {
			throw new StudentValidationException("Invalid status: " + normalizedStatus);
		}

		return normalizedStatus;
	}

	private Integer normalizeNullableYear(Integer year) {
		if (year == null) {
			return null;
		}

		if (year < 1900) {
			throw new StudentValidationException("year_taken must be null or >= 1900");
		}

		return year;
	}

	private Integer requirePositiveId(Integer value, String fieldName) {
		if (value == null) {
			throw new StudentValidationException(fieldName + " is required");
		}

		if (value <= 0) {
			throw new StudentValidationException(fieldName + " must be positive");
		}

		return value;
	}

	private String requireTrimmedText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new StudentValidationException(fieldName + " is required");
		}

		return value.trim();
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

	private record StudentCourseRecordCreateCommand(
			Integer courseId,
			String status,
			String grade,
			String semesterTaken,
			Integer yearTaken) {
	}

	private record StudentCourseRecordPatchCommand(
			boolean hasStatus,
			String status,
			boolean hasGrade,
			String grade,
			boolean hasSemesterTaken,
			String semesterTaken,
			boolean hasYearTaken,
			Integer yearTaken) {
	}
}
