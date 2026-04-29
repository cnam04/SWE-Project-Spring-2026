package com.cpvt.prereq_visualizer.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserModel;
import com.cpvt.prereq_visualizer.model.UserStudentModel;

@Repository
public class UserRepository {

	private final JdbcTemplate jdbcTemplate;

	public UserRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Reads summary fields required by GET /api/users/.
	public List<UserModel> findAllUsers() {
		String sql = """
				SELECT
					u.user_id,
					u.name,
					u.email,
					u.role,
					u.created_at,
					s.student_id AS linked_student_id,
					s.school_student_id
				FROM users u
				LEFT JOIN students s ON s.user_id = u.user_id
				ORDER BY u.user_id
				""";

		return jdbcTemplate.query(sql, (rs, rowNum) -> new UserModel(
				rs.getInt("user_id"),
				rs.getString("name"),
				rs.getString("email"),
				rs.getString("role"),
				readCreatedAt(rs.getObject("created_at")),
				(Integer) rs.getObject("linked_student_id"),
				rs.getString("school_student_id")));
	}

	public Optional<UserDetailModel> findUserById(Integer userId) {
		String sql = """
				SELECT
					u.user_id,
					u.name,
					u.email,
					u.role,
					u.created_at,
					s.student_id,
					s.school_student_id,
					s.major
				FROM users u
				LEFT JOIN students s ON s.user_id = u.user_id
				WHERE u.user_id = ?
				""";

		List<UserDetailModel> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
			Integer studentId = (Integer) rs.getObject("student_id");
			UserStudentModel student = null;
			if (studentId != null) {
				student = new UserStudentModel(
						studentId,
						rs.getString("school_student_id"),
						rs.getString("major"));
			}

			return new UserDetailModel(
					rs.getInt("user_id"),
					rs.getString("name"),
					rs.getString("email"),
					rs.getString("role"),
					readCreatedAt(rs.getObject("created_at")),
					student);
		}, userId);

		return users.stream().findFirst();
	}

	public Optional<Integer> findUserIdByEmail(String email) {
		String sql = """
				SELECT user_id
				FROM users
				WHERE LOWER(email) = LOWER(?)
				""";

		List<Integer> ids = jdbcTemplate.query(sql,
				(rs, rowNum) -> rs.getInt("user_id"),
				email);

		return ids.stream().findFirst();
	}

	public Optional<Integer> findStudentIdBySchoolStudentId(String schoolStudentId) {
		String sql = """
				SELECT student_id
				FROM students
				WHERE school_student_id = ?
				""";

		List<Integer> ids = jdbcTemplate.query(sql,
				(rs, rowNum) -> rs.getInt("student_id"),
				schoolStudentId);

		return ids.stream().findFirst();
	}

	public Integer insertUser(String name, String email, String passwordHash, String role) {
		String sql = """
				INSERT INTO users (name, email, password_hash, role)
				VALUES (?, ?, ?, ?)
				RETURNING user_id
				""";

		Integer userId = jdbcTemplate.queryForObject(sql, Integer.class, name, email, passwordHash, role);
		if (userId == null) {
			throw new IllegalStateException("Insert user failed to return user_id");
		}

		return userId;
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

	public int updateUserBasicFields(Integer userId, String name, String email, String role) {
		String sql = """
				UPDATE users
				SET name = ?,
					email = ?,
					role = ?
				WHERE user_id = ?
				""";

		return jdbcTemplate.update(sql, name, email, role, userId);
	}

	public int deleteUserById(Integer userId) {
		String sql = """
				DELETE FROM users
				WHERE user_id = ?
				""";

		return jdbcTemplate.update(sql, userId);
	}

	private LocalDateTime readCreatedAt(Object value) {
		if (value == null) {
			throw new IllegalStateException("created_at was null");
		}

		if (value instanceof LocalDateTime localDateTime) {
			return localDateTime;
		}

		if (value instanceof Timestamp timestamp) {
			return timestamp.toLocalDateTime();
		}

		throw new IllegalStateException("Unexpected created_at value type: " + value);
	}
}