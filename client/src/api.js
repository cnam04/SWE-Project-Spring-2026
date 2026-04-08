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

	if (!response.ok) {
		throw new Error(`Request failed (${response.status}) for ${path}`)
	}

	return response.json()
}

export function getTestItems() {
	return request('/api/test-items', { method: 'GET' })
}
