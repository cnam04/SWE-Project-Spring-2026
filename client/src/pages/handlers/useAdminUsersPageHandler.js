import { useCallback, useEffect, useMemo, useState } from 'react'
import { loadAdminUserDetail, loadAdminUsers, deleteAdminUserDetail } from '../../services/adminUsersService'


function asSearchText(value) {
	if (value === null || value === undefined) {
		return ''
	}

	return String(value).toLowerCase()
}

function sortUsers(users) {
	return [...users].sort((left, right) => String(left.name || '').localeCompare(String(right.name || ''), undefined, {
		sensitivity: 'base',
		numeric: true,
	}))
}

export function useAdminUsersPageHandler() {
	const [users, setUsers] = useState([])
	const [loadingUsers, setLoadingUsers] = useState(true)
	const [usersError, setUsersError] = useState('')
	const [isRefreshingUsers, setIsRefreshingUsers] = useState(false)

	const [searchText, setSearchText] = useState('')
	const [searchField, setSearchField] = useState('name')

	const [selectedUserId, setSelectedUserId] = useState(null)
	const [selectedUser, setSelectedUser] = useState(null)
	const [selectedUserLoading, setSelectedUserLoading] = useState(false)
	const [selectedUserError, setSelectedUserError] = useState('')
	const [userDetailCache, setUserDetailCache] = useState({})

	const fetchUsers = useCallback(async (showRefreshingState) => {
		if (showRefreshingState) {
			setIsRefreshingUsers(true)
		}

		try {
			const nextUsers = await loadAdminUsers()
			setUsers(nextUsers)
			setUsersError('')
		} catch (err) {
			setUsersError(err.message || 'Unable to load users')
		} finally {
			setLoadingUsers(false)
			setIsRefreshingUsers(false)
		}
	}, [])

	useEffect(() => {
		fetchUsers(false)
	}, [fetchUsers])

	const fetchSelectedUser = useCallback(async (userId, forceRefresh = false) => {
		if (!userId && userId !== 0) {
			setSelectedUser(null)
			setSelectedUserError('')
			return
		}

		const cacheKey = String(userId)
		if (!forceRefresh && userDetailCache[cacheKey]) {
			setSelectedUser(userDetailCache[cacheKey])
			setSelectedUserError('')
			return
		}

		setSelectedUserLoading(true)

		try {
			const detail = await loadAdminUserDetail(userId)
			setSelectedUser(detail)
			setSelectedUserError('')
			setUserDetailCache((previousCache) => ({
				...previousCache,
				[cacheKey]: detail,
			}))
		} catch (err) {
			setSelectedUser(null)
			setSelectedUserError(err.message || 'Unable to load selected user details')
		} finally {
			setSelectedUserLoading(false)
		}
	}, [userDetailCache])

	useEffect(() => {
		if (selectedUserId === null) {
			setSelectedUser(null)
			setSelectedUserError('')
			return
		}

		fetchSelectedUser(selectedUserId)
	}, [fetchSelectedUser, selectedUserId])

	useEffect(() => {
		if (selectedUserId === null) {
			return
		}

		const hasSelectedUser = users.some((user) => String(user.userId) === String(selectedUserId))
		if (!hasSelectedUser) {
			setSelectedUserId(null)
			setSelectedUser(null)
			setSelectedUserError('')
		}
	}, [selectedUserId, users])

	const visibleUsers = useMemo(() => {
		const query = searchText.trim().toLowerCase()
		const matchingUsers = query
			? users.filter((user) => {
				if (searchField === 'email') {
					return asSearchText(user.email).includes(query)
				}

				if (searchField === 'role') {
					return asSearchText(user.role).includes(query)
				}

				return asSearchText(user.name).includes(query)
			})
			: users

		return sortUsers(matchingUsers)
	}, [searchField, searchText, users])

	const handleRefreshUsers = useCallback(() => {
		fetchUsers(true)
	}, [fetchUsers])

	const handleSelectUser = useCallback((userId) => {
		setSelectedUserId(userId)
	}, [])

	const handleRetrySelectedUser = useCallback(() => {
		if (selectedUserId === null) {
			return
		}

		fetchSelectedUser(selectedUserId, true)
	}, [fetchSelectedUser, selectedUserId])

	const handleDeleteUser = useCallback(async (deleteId) => {
		if (!window.confirm('Are you sure you want to delete this user?')) return;

		try {
			await deleteAdminUserDetail(deleteId)
			// clear the selection if they deleted the user they are currently looking at
			if (selectedUserId === deleteId) {
				setSelectedUserId(null)
				setSelectedUser(null)
			}
			// refresh the list to remove deleted user
			fetchUsers(true)
		} catch (err) {
			alert(err.message || 'Failed to delete user')
		}
	}, [fetchUsers, selectedUserId])

	return {
		allUsersCount: users.length,
		users,
		visibleUsers,
		loadingUsers,
		usersError,
		isRefreshingUsers,
		searchText,
		searchField,
		selectedUserId,
		selectedUser,
		selectedUserLoading,
		selectedUserError,
		setSearchText,
		setSearchField,
		handleRefreshUsers,
		handleSelectUser,
		handleRetrySelectedUser,
		handleDeleteUser
	}
}