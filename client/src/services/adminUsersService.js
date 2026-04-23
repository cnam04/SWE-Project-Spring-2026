import { getUserById, getUsers } from '../api'

function normalizeText(value) {
	if (value === null || value === undefined) {
		return ''
	}

	return String(value).trim()
}

function normalizeUserSummary(rawUser) {
	const source = rawUser || {}

	return {
		userId: source.user_id ?? source.userId ?? source.id ?? null,
		name: normalizeText(source.name),
		email: normalizeText(source.email),
		role: normalizeText(source.role),
		createdAt: source.created_at ?? source.createdAt ?? null,
		linkedStudentId: source.linked_student_id ?? source.linkedStudentId ?? null,
		schoolStudentId: normalizeText(source.school_student_id ?? source.schoolStudentId),
	}
}

function normalizeUserDetail(rawUser) {
	const summary = normalizeUserSummary(rawUser)
	const student = rawUser?.student || null

	return {
		...summary,
		student: student
			? {
				studentId: student.student_id ?? student.studentId ?? null,
				schoolStudentId: normalizeText(student.school_student_id ?? student.schoolStudentId),
				major: normalizeText(student.major),
			}
			: null,
	}
}

export async function loadAdminUsers() {
	const rawUsers = await getUsers()
	if (!Array.isArray(rawUsers)) {
		return []
	}

	return rawUsers.map((rawUser) => normalizeUserSummary(rawUser))
}

export async function loadAdminUserDetail(userId) {
	const rawUser = await getUserById(userId)
	return normalizeUserDetail(rawUser)
}