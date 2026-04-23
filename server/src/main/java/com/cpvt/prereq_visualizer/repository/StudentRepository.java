package com.cpvt.prereq_visualizer.repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cpvt.prereq_visualizer.model.StudentCourseRecordModel;
import com.cpvt.prereq_visualizer.model.StudentModel;

@Repository
public class StudentRepository {

	private final JdbcTemplate jdbcTemplate;

	public StudentRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Reads summary fields for GET /api/students/ with optional filters.
	public List<StudentModel> findAllStudents(String nameFilter, String schoolStudentIdFilter, String emailFilter) {
		StringBuilder sql = new StringBuilder("""
				SELECT
					s.student_id,
					s.user_id,
					u.name,
					u.email,
					s.school_student_id,
					s.major
				FROM students s
				JOIN users u ON u.user_id = s.user_id
				WHERE 1 = 1
				""");

		List<Object> params = new ArrayList<>();

		if (nameFilter != null) {
			sql.append(" AND LOWER(u.name) LIKE LOWER(?)");
			params.add("%" + nameFilter + "%");
		}

		if (schoolStudentIdFilter != null) {
			sql.append(" AND s.school_student_id = ?");
			params.add(schoolStudentIdFilter);
		}

		if (emailFilter != null) {
			sql.append(" AND LOWER(u.email) LIKE LOWER(?)");
			params.add("%" + emailFilter + "%");
		}

		sql.append(" ORDER BY s.student_id");

		return jdbcTemplate.query(
				sql.toString(),
				(rs, rowNum) -> new StudentModel(
						rs.getInt("student_id"),
						rs.getInt("user_id"),
						rs.getString("name"),
						rs.getString("email"),
						rs.getString("school_student_id"),
						rs.getString("major")),
				params.toArray());
	}

	public Optional<StudentModel> findStudentById(Integer studentId) {
		String sql = """
				SELECT
					s.student_id,
					s.user_id,
					u.name,
					u.email,
					s.school_student_id,
					s.major
				FROM students s
				JOIN users u ON u.user_id = s.user_id
				WHERE s.student_id = ?
				""";

		List<StudentModel> students = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> new StudentModel(
						rs.getInt("student_id"),
						rs.getInt("user_id"),
						rs.getString("name"),
						rs.getString("email"),
						rs.getString("school_student_id"),
						rs.getString("major")),
				studentId);

		return students.stream().findFirst();
	}

	public Optional<Integer> findStudentIdBySchoolStudentId(String schoolStudentId) {
		String sql = """
				SELECT student_id
				FROM students
				WHERE school_student_id = ?
				""";

		List<Integer> ids = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> rs.getInt("student_id"),
				schoolStudentId);

		return ids.stream().findFirst();
	}

	public List<StudentCourseRecordModel> findStudentCourseRecordsByStudentId(Integer studentId) {
		String sql = """
				SELECT
					scr.record_id,
					scr.student_id,
					scr.course_id,
					c.course_code,
					c.crn,
					c.title,
					c.credits,
					c.attributes,
					scr.status,
					scr.grade,
					scr.semester_taken,
					scr.year_taken
				FROM student_course_records scr
				JOIN courses c ON c.course_id = scr.course_id
				WHERE scr.student_id = ?
				ORDER BY scr.record_id
				""";

		return jdbcTemplate.query(
				sql,
				(rs, rowNum) -> new StudentCourseRecordModel(
						rs.getInt("record_id"),
						rs.getInt("student_id"),
						rs.getInt("course_id"),
						rs.getString("course_code"),
						rs.getString("crn"),
						rs.getString("title"),
						rs.getInt("credits"),
						parseTextArray(rs.getArray("attributes")),
						rs.getString("status"),
						rs.getString("grade"),
						rs.getString("semester_taken"),
						(Integer) rs.getObject("year_taken")),
				studentId);
	}

	public Optional<StudentCourseRecordModel> findStudentCourseRecordById(Integer studentId, Integer recordId) {
		String sql = """
				SELECT
					scr.record_id,
					scr.student_id,
					scr.course_id,
					c.course_code,
					c.crn,
					c.title,
					c.credits,
					c.attributes,
					scr.status,
					scr.grade,
					scr.semester_taken,
					scr.year_taken
				FROM student_course_records scr
				JOIN courses c ON c.course_id = scr.course_id
				WHERE scr.student_id = ?
				  AND scr.record_id = ?
				""";

		List<StudentCourseRecordModel> records = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> new StudentCourseRecordModel(
						rs.getInt("record_id"),
						rs.getInt("student_id"),
						rs.getInt("course_id"),
						rs.getString("course_code"),
						rs.getString("crn"),
						rs.getString("title"),
						rs.getInt("credits"),
						parseTextArray(rs.getArray("attributes")),
						rs.getString("status"),
						rs.getString("grade"),
						rs.getString("semester_taken"),
						(Integer) rs.getObject("year_taken")),
				studentId,
				recordId);

		return records.stream().findFirst();
	}

	public Optional<Integer> findRecordIdByStudentIdAndCourseId(Integer studentId, Integer courseId) {
		String sql = """
				SELECT record_id
				FROM student_course_records
				WHERE student_id = ?
				  AND course_id = ?
				""";

		List<Integer> ids = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> rs.getInt("record_id"),
				studentId,
				courseId);

		return ids.stream().findFirst();
	}

	public Integer insertStudentCourseRecord(
			Integer studentId,
			Integer courseId,
			String status,
			String grade,
			String semesterTaken,
			Integer yearTaken) {
		String sql = """
				INSERT INTO student_course_records (student_id, course_id, status, grade, semester_taken, year_taken)
				VALUES (?, ?, ?, ?, ?, ?)
				RETURNING record_id
				""";

		Integer recordId = jdbcTemplate.queryForObject(
				sql,
				Integer.class,
				studentId,
				courseId,
				status,
				grade,
				semesterTaken,
				yearTaken);

		if (recordId == null) {
			throw new IllegalStateException("Insert student course record failed to return record_id");
		}

		return recordId;
	}

	public int updateStudentCourseRecord(
			Integer studentId,
			Integer recordId,
			String status,
			String grade,
			String semesterTaken,
			Integer yearTaken) {
		String sql = """
				UPDATE student_course_records
				SET status = ?,
					grade = ?,
					semester_taken = ?,
					year_taken = ?
				WHERE student_id = ?
				  AND record_id = ?
				""";

		return jdbcTemplate.update(
				sql,
				status,
				grade,
				semesterTaken,
				yearTaken,
				studentId,
				recordId);
	}

	public int deleteStudentCourseRecord(Integer studentId, Integer recordId) {
		String sql = """
				DELETE FROM student_course_records
				WHERE student_id = ?
				  AND record_id = ?
				""";

		return jdbcTemplate.update(sql, studentId, recordId);
	}

	public Integer insertStudent(Integer userId, String schoolStudentId, String major) {
		String sql = """
				INSERT INTO students (user_id, school_student_id, major)
				VALUES (?, ?, ?)
				RETURNING student_id
				""";

		Integer studentId = jdbcTemplate.queryForObject(sql, Integer.class, userId, schoolStudentId, major);
		if (studentId == null) {
			throw new IllegalStateException("Insert student failed to return student_id");
		}

		return studentId;
	}

	public int updateStudentFields(Integer studentId, String schoolStudentId, String major) {
		String sql = """
				UPDATE students
				SET school_student_id = ?,
					major = ?
				WHERE student_id = ?
				""";

		return jdbcTemplate.update(sql, schoolStudentId, major, studentId);
	}

	private List<String> parseTextArray(Array sqlArray) throws SQLException {
		if (sqlArray == null) {
			return List.of();
		}

		try {
			Object rawArray = sqlArray.getArray();
			if (rawArray instanceof String[] values) {
				return Arrays.asList(values);
			}

			if (rawArray instanceof Object[] values) {
				return Arrays.stream(values)
						.map(String::valueOf)
						.toList();
			}

			return List.of();
		} finally {
			sqlArray.free();
		}
	}
}
