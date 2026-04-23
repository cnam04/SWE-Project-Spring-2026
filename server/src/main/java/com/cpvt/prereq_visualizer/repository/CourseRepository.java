package com.cpvt.prereq_visualizer.repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cpvt.prereq_visualizer.model.CourseModel;
import com.cpvt.prereq_visualizer.model.CourseWithRootPrerequisiteModel;
import com.cpvt.prereq_visualizer.model.PrerequisiteTreeNodeModel;

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

	// Reads the base course fields plus root prerequisite node reference.
	public Optional<CourseWithRootPrerequisiteModel> findCourseWithRootPrerequisiteById(Integer courseId) {
		String sql = """
				SELECT course_id, course_code, crn, title, credits, attributes, root_prerequisite_node_id
				FROM courses
				WHERE course_id = ?
				""";

		List<CourseWithRootPrerequisiteModel> courses = jdbcTemplate.query(sql, (rs, rowNum) -> {
			Integer rootNodeId = (Integer) rs.getObject("root_prerequisite_node_id");

			return new CourseWithRootPrerequisiteModel(
					rs.getInt("course_id"),
					rs.getString("course_code"),
					rs.getString("crn"),
					rs.getString("title"),
					rs.getInt("credits"),
					parseTextArray(rs.getArray("attributes")),
					rootNodeId);
		}, courseId);

		return courses.stream().findFirst();
	}

	// Reads a single prerequisite node row by id.
	public Optional<PrerequisiteTreeNodeModel> findPrerequisiteNodeById(Integer nodeId) {
		String nodeSql = """
				SELECT pn.node_type, required_course.course_code AS required_course_code
				FROM prerequisite_nodes pn
				LEFT JOIN courses required_course ON required_course.course_id = pn.required_course_id
				WHERE pn.node_id = ?
				""";

		List<PrerequisiteTreeNodeModel> nodes = jdbcTemplate.query(nodeSql, (rs, rowNum) -> {
			String nodeType = rs.getString("node_type");
			PrerequisiteTreeNodeModel node = new PrerequisiteTreeNodeModel();
			node.setType(nodeType);

			if ("COURSE".equals(nodeType)) {
				node.setCourseCode(rs.getString("required_course_code"));
			}

			return node;
		}, nodeId);

		return nodes.stream().findFirst();
	}

	// Reads child node ids in a deterministic order.
	public List<Integer> findChildNodeIds(Integer parentNodeId) {
		String childrenSql = """
				SELECT child_node_id
				FROM prerequisite_node_edges
				WHERE parent_node_id = ?
				ORDER BY sort_order
				""";

		return jdbcTemplate.query(childrenSql,
				(rs, rowNum) -> rs.getInt("child_node_id"),
				parentNodeId);
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
