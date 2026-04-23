package com.cpvt.prereq_visualizer.repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cpvt.prereq_visualizer.model.CourseModel;

@Repository
public class CourseRepository {

	private final JdbcTemplate jdbcTemplate;

	public CourseRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Reads only the summary fields required by GET /api/courses/.
	public List<CourseModel> findAllCourses() {
		String sql = """
				SELECT course_id, course_code, crn, title, credits, attributes
				FROM courses
				ORDER BY course_id
				""";

		return jdbcTemplate.query(sql, (rs, rowNum) ->
				new CourseModel(
						rs.getInt("course_id"),
						rs.getString("course_code"),
						rs.getString("crn"),
						rs.getString("title"),
						rs.getInt("credits"),
						parseTextArray(rs.getArray("attributes"))));
	}

	// Converts Postgres TEXT[] from JDBC into a JSON-friendly List<String>.
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
