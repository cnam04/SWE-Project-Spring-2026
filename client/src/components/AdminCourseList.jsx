function isSelectedCourse(selectedCourseId, courseId) {
  if (selectedCourseId === null || selectedCourseId === undefined) {
    return false
  }

  return String(selectedCourseId) === String(courseId)
}

function renderCourseSubtitle(course) {
  const creditsText = course.credits === null || course.credits === undefined
    ? 'Credits unavailable'
    : `${course.credits} credits`

  if (!course.crn) {
    return creditsText
  }

  return `CRN ${course.crn} - ${creditsText}`
}

function AdminCourseList({
  allCoursesCount,
  courses,
  loading,
  error,
  isRefreshing,
  isAddCourseMode,
  searchText,
  searchField,
  sortField,
  selectedCourseId,
  onSearchTextChange,
  onSearchFieldChange,
  onSortFieldChange,
  onOpenAddCourse,
  onRefresh,
  onSelectCourse,
}) {
  return (
    <section className="box app-surface h-full">
      <div className="is-flex is-align-items-center is-justify-content-space-between is-gap-2 mb-3 is-flex-wrap-wrap">
        <h2 className="title is-5 mb-0">Courses</h2>
        <div className="buttons mb-0 are-small">
          <button
            className="button is-link is-light"
            type="button"
            onClick={onRefresh}
            disabled={isRefreshing}
          >
            {isRefreshing ? 'Refreshing...' : 'Refresh'}
          </button>
          <button
            className={`button is-light ${isAddCourseMode ? 'is-link' : ''}`}
            type="button"
            onClick={onOpenAddCourse}
          >
            {isAddCourseMode ? 'Adding Course...' : 'Add Course'}
          </button>
        </div>
      </div>

      <div className="columns is-multiline is-variable is-2 mb-1">
        <div className="column is-12">
          <label className="label is-small">Search Courses</label>
          <div className="control">
            <input
              className="input"
              type="text"
              value={searchText}
              onChange={(event) => onSearchTextChange(event.target.value)}
              placeholder="Search by title, code, or CRN"
            />
          </div>
        </div>
        <div className="column is-6-tablet is-12-mobile">
          <label className="label is-small">Search By</label>
          <div className="select is-fullwidth">
            <select value={searchField} onChange={(event) => onSearchFieldChange(event.target.value)}>
              <option value="title">Title</option>
              <option value="courseCode">Course Code</option>
              <option value="crn">CRN</option>
            </select>
          </div>
        </div>
        <div className="column is-6-tablet is-12-mobile">
          <label className="label is-small">Sort By</label>
          <div className="select is-fullwidth">
            <select value={sortField} onChange={(event) => onSortFieldChange(event.target.value)}>
              <option value="courseCode">Course Code</option>
              <option value="title">Title</option>
              <option value="credits">Credits</option>
            </select>
          </div>
        </div>
      </div>

      <p className="is-size-7 has-text-grey mb-3">
        Showing {courses.length} of {allCoursesCount} course{allCoursesCount === 1 ? '' : 's'}.
      </p>

      {loading ? <p>Loading courses...</p> : null}
      {!loading && error ? <p className="error-text">Error: {error}</p> : null}

      {!loading && !error && !courses.length ? (
        <p className="is-size-7 has-text-grey">No courses match the active filters.</p>
      ) : null}

      {!loading && !error && courses.length ? (
        <div className="admin-course-list">
          {courses.map((course) => {
            const selected = isSelectedCourse(selectedCourseId, course.courseId)

            return (
              <article
                key={String(course.courseId)}
                className={`media admin-course-item ${selected ? 'is-selected' : ''}`}
              >
                <div className="media-content">
                  <p className="has-text-weight-semibold mb-1">{course.title || 'Untitled course'}</p>
                  <p className="is-size-7 has-text-grey mb-1">{renderCourseSubtitle(course)}</p>
                  <div className="tags are-small mb-0">
                    <span className="tag is-light">{course.courseCode || 'No code'}</span>
                    {course.attributes.slice(0, 2).map((attribute) => (
                      <span key={`${course.courseId}-${attribute}`} className="tag is-info is-light">
                        {attribute}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="media-right">
                  <button
                    className={`button is-small ${selected ? 'is-warning is-light' : 'is-link is-light'}`}
                    type="button"
                    onClick={() => onSelectCourse(course.courseId)}
                  >
                    {selected ? 'Selected' : 'View'}
                  </button>
                </div>
              </article>
            )
          })}
        </div>
      ) : null}
    </section>
  )
}

export default AdminCourseList
