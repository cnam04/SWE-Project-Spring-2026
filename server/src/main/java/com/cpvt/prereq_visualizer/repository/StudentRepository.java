package com.cpvt.prereq_visualizer.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
}
