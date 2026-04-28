import { useState } from 'react'
import AdminCourseDetail from '../components/AdminCourseDetail'
import AdminCourseList from '../components/AdminCourseList'
import AdminAddCoursePanel from '../components/AdminAddCoursePanel'
import AdminUsersPanel from '../components/AdminUsersPanel'
import { useAdminCoursesPageHandler } from './handlers/useAdminCoursesPageHandler'
import { useAdminUsersPageHandler } from './handlers/useAdminUsersPageHandler'
import { getAdminAccessState } from '../services/utils/adminAccess'
import '../styles/CoursePrereqPage.css'
import '../styles/AdminPage.css'

export default function AdminPage() {
  const { isKnown: hasKnownAccess, isAdmin } = getAdminAccessState()
  const [activeTab, setActiveTab] = useState('courses')
  const {
    allCoursesCount,
    allCourses,
    visibleCourses,
    loadingCourses,
    coursesError,
    isRefreshingCourses,
    isAddCourseMode,
    isEditingCourseId,
    addCourseDraft,
    addCoursePrerequisiteTree,
    addCoursePrerequisiteSummary,
    addCourseValidationErrors,
    addCourseSubmitError,
    isSavingCourse,
    availablePrerequisiteCourses,
    searchText,
    searchField,
    sortField,
    selectedCourseId,
    selectedCourseDetail,
    selectedCourseLoading,
    selectedCourseError,
    courseMutationsReady,
    setSearchText,
    setSearchField,
    setSortField,
    handleOpenAddCourse,
    handleCancelAddCourse,
    handleAddCourseDraftChange,
    handleSetPrerequisiteRootType,
    handleClearPrerequisiteTree,
    handleChangePrerequisiteNodeType,
    handleChangePrerequisiteNodeCourse,
    handleAddPrerequisiteChild,
    handleRemovePrerequisiteNode,
    handleSaveAddCourse,
    handleRefreshCourses,
    handleSelectCourse,
    handleClearCourseSelection,
    handleRetrySelectedCourse,
    handleEditSelectedCourse,
    handleEditSelectedPrerequisites,
  } = useAdminCoursesPageHandler()
  const {
    allUsersCount,
    visibleUsers,
    loadingUsers,
    usersError,
    isRefreshingUsers,
    searchText: userSearchText,
    searchField: userSearchField,
    selectedUserId,
    selectedUser,
    selectedUserLoading,
    selectedUserError,
    setSearchText: setUserSearchText,
    setSearchField: setUserSearchField,
    handleRefreshUsers,
    handleSelectUser,
    handleRetrySelectedUser,
    handleDeleteUser,
  } = useAdminUsersPageHandler()

  if (hasKnownAccess && !isAdmin) {
    return (
      <section className="app-page">
        <div className="container is-app">
          <header className="app-page-header">
            <h1 className="title is-4 app-page-title">Admin</h1>
            <p className="subtitle is-6 app-page-subtitle">You do not have permission to access this page.</p>
          </header>

          <section className="box app-surface admin-callout mb-4">
            <p className="is-size-7 mb-0">This admin area is restricted to users with the admin role.</p>
          </section>
        </div>
      </section>
    )
  }

  return (
    <section className="app-page">
      <div className="container is-app">
        <header className="app-page-header">
          <h1 className="title is-4 app-page-title">Admin</h1>
          <p className="subtitle is-6 app-page-subtitle">Manage users and courses from one protected workspace.</p>
        </header>

        <section className="box app-surface admin-callout mb-4">
          <div className="is-flex is-align-items-center is-justify-content-space-between is-gap-3 is-flex-wrap-wrap">
            <p className="is-size-7 mb-0">Default view: Courses. Switch to Users to inspect system accounts.</p>
            <div className="tabs is-toggle is-toggle-rounded mb-0 admin-tabs">
              <ul>
                <li className={activeTab === 'users' ? 'is-active' : ''}>
                  <button type="button" onClick={() => setActiveTab('users')}>Users</button>
                </li>
                <li className={activeTab === 'courses' ? 'is-active' : ''}>
                  <button type="button" onClick={() => setActiveTab('courses')}>Courses</button>
                </li>
              </ul>
            </div>
          </div>
        </section>

        {activeTab === 'users' ? (
          <AdminUsersPanel
            users={visibleUsers}
            allUsersCount={allUsersCount}
            loading={loadingUsers}
            error={usersError}
            isRefreshing={isRefreshingUsers}
            searchText={userSearchText}
            searchField={userSearchField}
            selectedUserId={selectedUserId}
            selectedUser={selectedUser}
            selectedUserLoading={selectedUserLoading}
            selectedUserError={selectedUserError}
            onSearchTextChange={setUserSearchText}
            onSearchFieldChange={setUserSearchField}
            onRefresh={handleRefreshUsers}
            onSelectUser={handleSelectUser}
            onRetrySelectedUser={handleRetrySelectedUser}
            onDeleteUser={handleDeleteUser}
          />
        ) : (
          <div className="columns is-variable is-4 prereq-layout">
            <aside className="column is-12-mobile is-5-desktop">
              <AdminCourseList
                allCoursesCount={allCoursesCount}
                courses={visibleCourses}
                loading={loadingCourses}
                error={coursesError}
                isRefreshing={isRefreshingCourses}
                isAddCourseMode={isAddCourseMode}
                searchText={searchText}
                searchField={searchField}
                sortField={sortField}
                selectedCourseId={selectedCourseId}
                onSearchTextChange={setSearchText}
                onSearchFieldChange={setSearchField}
                onSortFieldChange={setSortField}
                onOpenAddCourse={handleOpenAddCourse}
                onRefresh={handleRefreshCourses}
                onSelectCourse={handleSelectCourse}
              />
            </aside>

            <main className="column is-12-mobile is-7-desktop">
              {isAddCourseMode ? (
                <AdminAddCoursePanel
                  draft={addCourseDraft}
                  availableCourses={availablePrerequisiteCourses.length ? availablePrerequisiteCourses : allCourses}
                  prerequisiteTree={addCoursePrerequisiteTree}
                  prerequisiteSummary={addCoursePrerequisiteSummary}
                  validationErrors={addCourseValidationErrors}
                  submitError={addCourseSubmitError}
                  isSaving={isSavingCourse}
                  isEditMode={isEditingCourseId}
                  onCancel={handleCancelAddCourse}
                  onDraftChange={handleAddCourseDraftChange}
                  onSave={() => handleSaveAddCourse(isEditingCourseId)}
                  onSetRootType={handleSetPrerequisiteRootType}
                  onClearTree={handleClearPrerequisiteTree}
                  onChangeNodeType={handleChangePrerequisiteNodeType}
                  onChangeNodeCourse={handleChangePrerequisiteNodeCourse}
                  onAddChildNode={handleAddPrerequisiteChild}
                  onRemoveNode={handleRemovePrerequisiteNode}
                />
              ) : (
                <AdminCourseDetail
                  course={selectedCourseDetail}
                  loading={selectedCourseLoading}
                  error={selectedCourseError}
                  courseMutationsReady={courseMutationsReady}
                  onRetry={handleRetrySelectedCourse}
                  onClearSelection={handleClearCourseSelection}
                  onEditCourse={() => handleEditSelectedCourse(selectedCourseId)}
                  onEditPrerequisites={() => handleEditSelectedPrerequisites(selectedCourseId)}
                />
              )}
            </main>
          </div>
        )}
      </div>
    </section>
  )
}
