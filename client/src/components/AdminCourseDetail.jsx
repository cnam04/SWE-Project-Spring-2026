import PrerequisiteTreeView from './PrerequisiteTreeView'

function detailValue(value, fallback = 'Not provided') {
  if (value === null || value === undefined || value === '') {
    return fallback
  }

  return String(value)
}

function AdminCourseDetail({
  course,
  loading,
  error,
  courseMutationsReady,
  onRetry,
  onClearSelection,
  onEditCourse,
  onEditPrerequisites,
}) {
  return (
    <section className="box app-surface h-full">
      <div className="is-flex is-align-items-center is-justify-content-space-between is-gap-2 mb-3 is-flex-wrap-wrap">
        <h2 className="title is-5 mb-0">Course Details</h2>
        <div className="buttons mb-0 are-small">
          {course ? (
            <button className="button is-light" type="button" onClick={onClearSelection}>
              Clear Selection
            </button>
          ) : null}
          {error ? (
            <button className="button is-link is-light" type="button" onClick={onRetry}>
              Retry
            </button>
          ) : null}
        </div>
      </div>

      {!course && !loading && !error ? (
        <p className="is-size-7 has-text-grey">
          Select a course from the list to view prerequisite details.
        </p>
      ) : null}

      {loading ? <p>Loading selected course...</p> : null}

      {!loading && error ? <p className="error-text">Error: {error}</p> : null}

      {!loading && !error && course ? (
        <div>
          <p className="detail-row"><span>Course ID</span><strong>{detailValue(course.courseId)}</strong></p>
          <p className="detail-row"><span>Course Code</span><strong>{detailValue(course.courseCode)}</strong></p>
          <p className="detail-row"><span>CRN</span><strong>{detailValue(course.crn)}</strong></p>
          <p className="detail-row"><span>Title</span><strong>{detailValue(course.title)}</strong></p>
          <p className="detail-row"><span>Credits</span><strong>{detailValue(course.credits)}</strong></p>

          <div className="mb-4">
            <p className="label is-small mb-2">Attributes</p>
            <div className="tags mb-0">
              {course.attributes.length ? (
                course.attributes.map((attribute) => (
                  <span key={`${course.courseId}-${attribute}`} className="tag is-info is-light">
                    {attribute}
                  </span>
                ))
              ) : (
                <span className="tag is-light">None</span>
              )}
            </div>
          </div>

          <div className="buttons are-small mb-4">
            <button
              className="button is-light"
              type="button"
              onClick={onEditCourse}
              //disabled={!courseMutationsReady}
              title="Edit the selected course information."
            >
              Edit Course
            </button>
            <button
              className="button is-light"
              type="button"
              onClick={onEditPrerequisites}
              //disabled={!courseMutationsReady}
              title="Edit the selected course prerequisite tree."
            >
              Edit Prerequisites
            </button>
          </div>

          <div className="admin-prereq-section">
            <p className="label is-small mb-2">Prerequisite Summary</p>
            <p className="is-size-7 has-text-grey mb-3">{course.prerequisiteExpression}</p>
            <PrerequisiteTreeView tree={course.prerequisiteTree} />
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default AdminCourseDetail
