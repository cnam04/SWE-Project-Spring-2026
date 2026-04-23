import PrerequisiteTreeEditor from './PrerequisiteTreeEditor'

function AdminAddCoursePanel({
  draft,
  availableCourses,
  prerequisiteTree,
  prerequisiteSummary,
  validationErrors,
  submitError,
  isSaving,
  onCancel,
  onDraftChange,
  onSave,
  onSetRootType,
  onClearTree,
  onChangeNodeType,
  onChangeNodeCourse,
  onAddChildNode,
  onRemoveNode,
}) {
  return (
    <section className="box app-surface h-full">
      <div className="is-flex is-align-items-center is-justify-content-space-between is-gap-2 mb-3 is-flex-wrap-wrap">
        <h2 className="title is-5 mb-0">Add Course</h2>
        <button className="button is-light is-small" type="button" onClick={onCancel} disabled={isSaving}>
          Cancel
        </button>
      </div>

      {submitError ? (
        <article className="notification is-danger is-light admin-error-banner">
          {submitError}
        </article>
      ) : null}

      {validationErrors.length ? (
        <article className="notification is-danger is-light admin-error-banner">
          <p className="has-text-weight-semibold mb-2">Please fix the following before saving:</p>
          <ul className="admin-validation-list">
            {validationErrors.map((errorMessage) => (
              <li key={errorMessage}>{errorMessage}</li>
            ))}
          </ul>
        </article>
      ) : null}

      <div className="columns is-multiline is-variable is-3 mb-1">
        <div className="column is-12-mobile is-6-tablet">
          <label className="label is-small">Course Code</label>
          <div className="control">
            <input
              className="input"
              type="text"
              value={draft.courseCode}
              onChange={(event) => onDraftChange('courseCode', event.target.value)}
              placeholder="Ex: CPS250"
              disabled={isSaving}
            />
          </div>
        </div>

        <div className="column is-12-mobile is-6-tablet">
          <label className="label is-small">CRN (optional)</label>
          <div className="control">
            <input
              className="input"
              type="text"
              value={draft.crn}
              onChange={(event) => onDraftChange('crn', event.target.value)}
              placeholder="Ex: 10250"
              disabled={isSaving}
            />
          </div>
        </div>

        <div className="column is-12">
          <label className="label is-small">Title</label>
          <div className="control">
            <input
              className="input"
              type="text"
              value={draft.title}
              onChange={(event) => onDraftChange('title', event.target.value)}
              placeholder="Ex: Programming Languages"
              disabled={isSaving}
            />
          </div>
        </div>

        <div className="column is-12-mobile is-4-tablet">
          <label className="label is-small">Credits</label>
          <div className="control">
            <input
              className="input"
              type="number"
              min="0"
              step="1"
              value={draft.credits}
              onChange={(event) => onDraftChange('credits', event.target.value)}
              placeholder="Ex: 3"
              disabled={isSaving}
            />
          </div>
        </div>

        <div className="column is-12-mobile is-8-tablet">
          <label className="label is-small">Attributes (comma separated)</label>
          <div className="control">
            <input
              className="input"
              type="text"
              value={draft.attributesText}
              onChange={(event) => onDraftChange('attributesText', event.target.value)}
              placeholder="Ex: Core, Junior"
              disabled={isSaving}
            />
          </div>
        </div>
      </div>

      <div className="admin-prereq-section">
        <p className="label is-small mb-2">Prerequisite Summary (unsaved)</p>
        <p className="is-size-7 has-text-grey mb-3">{prerequisiteSummary}</p>

        <p className="label is-small mb-2">Prerequisite Tree Builder</p>
        <PrerequisiteTreeEditor
          tree={prerequisiteTree}
          availableCourses={availableCourses}
          onSetRootType={onSetRootType}
          onClearTree={onClearTree}
          onChangeNodeType={onChangeNodeType}
          onChangeNodeCourse={onChangeNodeCourse}
          onAddChildNode={onAddChildNode}
          onRemoveNode={onRemoveNode}
        />
      </div>

      <div className="buttons are-small mt-4 mb-0">
        <button className="button is-link is-light" type="button" onClick={onSave} disabled={isSaving}>
          {isSaving ? 'Saving...' : 'Save Course'}
        </button>
        <button className="button is-light" type="button" onClick={onCancel} disabled={isSaving}>
          Cancel Add Course
        </button>
      </div>
    </section>
  )
}

export default AdminAddCoursePanel
