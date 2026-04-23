const USER_SEARCH_FIELDS = [
	{ value: 'name', label: 'Name' },
	{ value: 'email', label: 'Email' },
	{ value: 'role', label: 'Role' },
]

function formatCreatedAt(createdAt) {
	if (!createdAt) {
		return 'Not provided'
	}

	const parsedDate = new Date(createdAt)
	if (Number.isNaN(parsedDate.getTime())) {
		return String(createdAt)
	}

	return new Intl.DateTimeFormat(undefined, {
		dateStyle: 'medium',
		timeStyle: 'short',
	}).format(parsedDate)
}

function renderUserSubtitle(user) {
	const parts = []

	if (user.role) {
		parts.push(user.role)
	}

	if (user.schoolStudentId) {
		parts.push(`Student ${user.schoolStudentId}`)
	}

	return parts.join(' • ') || 'No additional details'
}

function AdminUsersPanel({
	users,
	allUsersCount,
	loading,
	error,
	isRefreshing,
	searchText,
	searchField,
	selectedUserId,
	selectedUser,
	selectedUserLoading,
	selectedUserError,
	onSearchTextChange,
	onSearchFieldChange,
	onRefresh,
	onSelectUser,
	onRetrySelectedUser,
}) {
	return (
		<section className="box app-surface h-full">
			<div className="is-flex is-align-items-center is-justify-content-space-between is-gap-2 mb-3 is-flex-wrap-wrap">
				<h2 className="title is-5 mb-0">Users</h2>
				<div className="buttons mb-0 are-small">
					<button
						className="button is-link is-light"
						type="button"
						onClick={onRefresh}
						disabled={isRefreshing}
					>
						{isRefreshing ? 'Refreshing...' : 'Refresh'}
					</button>
				</div>
			</div>

			<div className="columns is-variable is-4 admin-users-layout">
				<div className="column is-12-tablet is-6-desktop">
					<div className="columns is-multiline is-variable is-2 mb-1">
						<div className="column is-12">
							<label className="label is-small">Search Users</label>
							<div className="control">
								<input
									className="input"
									type="text"
									value={searchText}
									onChange={(event) => onSearchTextChange(event.target.value)}
									placeholder="Search by name, email, or role"
								/>
							</div>
						</div>
						<div className="column is-12">
							<label className="label is-small">Search By</label>
							<div className="select is-fullwidth">
								<select value={searchField} onChange={(event) => onSearchFieldChange(event.target.value)}>
									{USER_SEARCH_FIELDS.map((field) => (
										<option key={field.value} value={field.value}>{field.label}</option>
									))}
								</select>
							</div>
						</div>
					</div>

					<p className="is-size-7 has-text-grey mb-3">
						Showing {users.length} of {allUsersCount} user{allUsersCount === 1 ? '' : 's'}.
					</p>

					{loading ? <p>Loading users...</p> : null}
					{!loading && error ? <p className="error-text">Error: {error}</p> : null}

					{!loading && !error && !users.length ? (
						<p className="is-size-7 has-text-grey">No users found.</p>
					) : null}

					{!loading && !error && users.length ? (
						<div className="admin-users-list">
							{users.map((user) => {
								const selected = String(selectedUserId ?? '') === String(user.userId ?? '')

								return (
									<article
										key={String(user.userId)}
										className={`media admin-user-item ${selected ? 'is-selected' : ''}`}
									>
										<div className="media-content">
											<p className="has-text-weight-semibold mb-1">{user.name || 'Unnamed user'}</p>
											<p className="is-size-7 has-text-grey mb-1">{user.email || 'No email provided'}</p>
											<div className="tags are-small mb-0">
												<span className="tag is-light">{user.role || 'No role'}</span>
												{user.linkedStudentId ? (
													<span className="tag is-info is-light">Student ID {user.linkedStudentId}</span>
												) : null}
											</div>
										</div>

										<div className="media-right">
											<button
												className={`button is-small ${selected ? 'is-warning is-light' : 'is-link is-light'}`}
												type="button"
												onClick={() => onSelectUser(user.userId)}
											>
												{selected ? 'Selected' : 'View'}
											</button>
										</div>
									</article>
								)
							})}
						</div>
					) : null}
				</div>

				<div className="column is-12-tablet is-6-desktop">
					<section className="box admin-user-detail-panel h-full">
						<div className="is-flex is-align-items-center is-justify-content-space-between is-gap-2 mb-3 is-flex-wrap-wrap">
							<h3 className="title is-6 mb-0">User Details</h3>
							{selectedUserError ? (
								<button className="button is-link is-light is-small" type="button" onClick={onRetrySelectedUser}>
									Retry
								</button>
							) : null}
						</div>

						{selectedUserLoading ? <p>Loading selected user...</p> : null}
						{!selectedUserLoading && selectedUserError ? (
							<p className="error-text">Error: {selectedUserError}</p>
						) : null}

						{!selectedUserLoading && !selectedUserError && !selectedUser ? (
							<p className="is-size-7 has-text-grey">Select a user from the list to inspect details.</p>
						) : null}

						{!selectedUserLoading && !selectedUserError && selectedUser ? (
							<div>
								<p className="detail-row"><span>User ID</span><strong>{selectedUser.userId ?? 'Not provided'}</strong></p>
								<p className="detail-row"><span>Name</span><strong>{selectedUser.name || 'Not provided'}</strong></p>
								<p className="detail-row"><span>Email</span><strong>{selectedUser.email || 'Not provided'}</strong></p>
								<p className="detail-row"><span>Role</span><strong>{selectedUser.role || 'Not provided'}</strong></p>
								<p className="detail-row"><span>Created At</span><strong>{formatCreatedAt(selectedUser.createdAt)}</strong></p>
								<p className="detail-row"><span>Linked Student ID</span><strong>{selectedUser.linkedStudentId ?? 'Not provided'}</strong></p>
								<p className="detail-row"><span>School Student ID</span><strong>{selectedUser.schoolStudentId || 'Not provided'}</strong></p>

								<div className="mt-4">
									<p className="label is-small mb-2">Student Profile</p>
									{selectedUser.student ? (
										<div className="admin-user-student-card">
											<p className="detail-row"><span>Student ID</span><strong>{selectedUser.student.studentId ?? 'Not provided'}</strong></p>
											<p className="detail-row"><span>School Student ID</span><strong>{selectedUser.student.schoolStudentId || 'Not provided'}</strong></p>
											<p className="detail-row"><span>Major</span><strong>{selectedUser.student.major || 'Not provided'}</strong></p>
										</div>
									) : (
										<p className="is-size-7 has-text-grey mb-0">This user is not linked to a student profile.</p>
									)}
								</div>

								<p className="is-size-7 has-text-grey mt-4 mb-0">{renderUserSubtitle(selectedUser)}</p>
							</div>
						) : null}
					</section>
				</div>
			</div>
		</section>
	)
}

export default AdminUsersPanel