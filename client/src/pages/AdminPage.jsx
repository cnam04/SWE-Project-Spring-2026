import AdminCourseDetail from '../components/AdminCourseDetail'
import AdminCourseList from '../components/AdminCourseList'
import { useAdminCoursesPageHandler } from './handlers/useAdminCoursesPageHandler'
import '../styles/CoursePrereqPage.css'
import '../styles/AdminPage.css'

export default function AdminPage() {
  const {
    allCoursesCount,
    visibleCourses,
    loadingCourses,
    coursesError,
    isRefreshingCourses,
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
    handleRefreshCourses,
    handleSelectCourse,
    handleClearCourseSelection,
    handleRetrySelectedCourse,
  } = useAdminCoursesPageHandler()

  return (
    <section className="app-page">
      <div className="container is-app">
        <header className="app-page-header">
          <h1 className="title is-4 app-page-title">Admin</h1>
          <p className="subtitle is-6 app-page-subtitle">
            Course administration is active. User management is intentionally hidden until the user API is ready.
          </p>
        </header>

        <section className="box app-surface admin-callout mb-4">
          <p className="is-size-7 mb-0">
            This page currently supports the Courses section only. User controls will be added when the backend user endpoints are available.
          </p>
        </section>

        <div className="columns is-variable is-4 prereq-layout">
          <aside className="column is-12-mobile is-5-desktop">
            <AdminCourseList
              allCoursesCount={allCoursesCount}
              courses={visibleCourses}
              loading={loadingCourses}
              error={coursesError}
              isRefreshing={isRefreshingCourses}
              searchText={searchText}
              searchField={searchField}
              sortField={sortField}
              selectedCourseId={selectedCourseId}
              courseMutationsReady={courseMutationsReady}
              onSearchTextChange={setSearchText}
              onSearchFieldChange={setSearchField}
              onSortFieldChange={setSortField}
              onRefresh={handleRefreshCourses}
              onSelectCourse={handleSelectCourse}
            />
          </aside>

          <main className="column is-12-mobile is-7-desktop">
            <AdminCourseDetail
              course={selectedCourseDetail}
              loading={selectedCourseLoading}
              error={selectedCourseError}
              courseMutationsReady={courseMutationsReady}
              onRetry={handleRetrySelectedCourse}
              onClearSelection={handleClearCourseSelection}
            />
          </main>
        </div>
      </div>
    </section>
  )
}
