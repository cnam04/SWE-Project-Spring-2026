/**
 * API LAYER:
 * Shared HTTP utility and endpoint wrappers.
 *
 * Why this layer exists:
 * - Centralizes request plumbing (headers, error handling, JSON parsing).
 * - Keeps fetch details out of feature service files.
 * - Returns near-raw payloads so services can apply business shaping.
 */
async function request(path, options = {}) {
	const response = await fetch(path, {
		headers: {
			'Content-Type': 'application/json',
			...(options.headers || {}),
		},
		...options,
	})

	const contentType = response.headers.get('content-type') || ''
	const isJsonResponse = contentType.includes('application/json')

	let responsePayload = null
	if (isJsonResponse) {
		responsePayload = await response.json()
	} else {
		const textPayload = await response.text()
		responsePayload = textPayload || null
	}

	if (!response.ok) {
		const backendMessage = typeof responsePayload === 'object' && responsePayload
			? responsePayload.message || responsePayload.error
			: responsePayload

		const errorMessage = backendMessage || `Request failed (${response.status}) for ${path}`
		const error = new Error(errorMessage)
		error.status = response.status
		error.path = path
		error.payload = responsePayload

		throw error
	}

	return responsePayload
}

export function getTestItems() {
	return request('/api/test-items', { method: 'GET' })
}

export function getUsers() {
	return request('/api/users', { method: 'GET' })
}

export function getUserById(userId) {
	return request(`/api/users/${userId}`, { method: 'GET' })
}

export function getCourses() {
	return request('/api/courses', { method: 'GET' })
}

export function getCourseById(courseId) {
	return request(`/api/courses/${courseId}`, { method: 'GET' })
}

export function createCourse(coursePayload) {
	return request('/api/courses', {
		method: 'POST',
		body: JSON.stringify(coursePayload),
	})
}

export function updateCourse(courseId, coursePayload) {
	return request(`/api/courses/${courseId}`, {
		method: 'PATCH',
		body: JSON.stringify(coursePayload),
	})
}

export function updatePrerequisites(courseId, prerequisitesPayload) {
	return request(`/api/courses/${courseId}/prerequisites`, {
		method: 'PUT',
		body: JSON.stringify(prerequisitesPayload),
	})
}

export function getCourseGraph(courseId, queryOptions = {}) {
	const searchParams = new URLSearchParams()

	if (queryOptions.studentId !== null && queryOptions.studentId !== undefined && queryOptions.studentId !== '') {
		searchParams.set('studentId', String(queryOptions.studentId))
	}

	if (queryOptions.expand === true) {
		searchParams.set('expand', 'true')
	}

	const queryString = searchParams.toString()
	const path = queryString
		? `/api/courses/${courseId}/graph?${queryString}`
		: `/api/courses/${courseId}/graph`

	return request(path, { method: 'GET' })
}

export function deleteUserById(userId) {
	return request(`/api/users/${userId}`, { method: 'DELETE' })
}
