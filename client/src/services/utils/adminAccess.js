function getStoredJson(key) {
	if (typeof window === 'undefined') {
		return null
	}

	const rawValue = window.localStorage.getItem(key) || window.sessionStorage.getItem(key)
	if (!rawValue) {
		return null
	}

	try {
		return JSON.parse(rawValue)
	} catch {
		return rawValue
	}
}

function extractRole(candidate) {
	if (!candidate) {
		return ''
	}

	if (typeof candidate === 'string') {
		return candidate.trim().toLowerCase()
	}

	if (typeof candidate === 'object') {
		if (typeof candidate.role === 'string') {
			return candidate.role.trim().toLowerCase()
		}

		if (Array.isArray(candidate.roles) && candidate.roles.length) {
			return String(candidate.roles[0] || '').trim().toLowerCase()
		}
	}

	return ''
}

export function getCurrentAccessRole() {
	const candidates = [
		getStoredJson('currentUser'),
		getStoredJson('user'),
		getStoredJson('authUser'),
		getStoredJson('sessionUser'),
		getStoredJson('profile'),
		getStoredJson('role'),
		typeof window !== 'undefined' ? window.__USER__ : null,
	]

	for (const candidate of candidates) {
		const role = extractRole(candidate)
		if (role) {
			return role
		}
	}

	return ''
}

export function getAdminAccessState() {
	const role = getCurrentAccessRole()
	if (!role) {
		return {
			role: '',
			isKnown: false,
			isAdmin: true,
		}
	}

	return {
		role,
		isKnown: true,
		isAdmin: role === 'admin',
	}
}