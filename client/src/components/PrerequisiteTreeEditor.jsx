const NODE_TYPES = ['COURSE', 'AND', 'OR']

function courseOptionLabel(course) {
  const code = course.courseCode || 'Unknown code'
  const title = course.title || 'Untitled course'
  return `${code} - ${title}`
}

function pathKey(path) {
  if (!Array.isArray(path) || !path.length) {
    return 'root'
  }

  return `root-${path.join('-')}`
}

function PrerequisiteNodeEditor({
  node,
  path,
  availableCourses,
  onAddChildNode,
  onChangeNodeCourse,
  onChangeNodeType,
  onClearTree,
  onRemoveNode,
}) {
  const isRoot = !path.length
  const nodeChildren = Array.isArray(node.children) ? node.children : []

  return (
    <li className="admin-prereq-node admin-prereq-group">
      <div className="admin-prereq-editor-head">
        <div className="select is-small">
          <select value={node.type} onChange={(event) => onChangeNodeType(path, event.target.value)}>
            {NODE_TYPES.map((nodeType) => (
              <option key={`${pathKey(path)}-${nodeType}`} value={nodeType}>{nodeType}</option>
            ))}
          </select>
        </div>

        <button
          className="button is-small is-light"
          type="button"
          onClick={isRoot ? onClearTree : () => onRemoveNode(path)}
        >
          {isRoot ? 'Remove Root' : 'Remove Node'}
        </button>
      </div>

      {node.type === 'COURSE' ? (
        <div className="mt-2">
          <label className="label is-small">Course prerequisite</label>
          <div className="select is-fullwidth is-small">
            <select
              value={node.courseCode || ''}
              onChange={(event) => onChangeNodeCourse(path, event.target.value)}
            >
              <option value="">Select a course</option>
              {availableCourses.map((course) => (
                <option key={`${pathKey(path)}-${course.courseId}`} value={course.courseCode}>
                  {courseOptionLabel(course)}
                </option>
              ))}
            </select>
          </div>
        </div>
      ) : (
        <>
          <div className="buttons are-small mb-2 mt-2">
            <button className="button is-light" type="button" onClick={() => onAddChildNode(path, 'COURSE')}>
              COURSE Child
            </button>
            <button className="button is-light" type="button" onClick={() => onAddChildNode(path, 'AND')}>
              AND Child
            </button>
            <button className="button is-light" type="button" onClick={() => onAddChildNode(path, 'OR')}>
              OR Child
            </button>
          </div>

          {nodeChildren.length ? (
            <ul className="admin-prereq-children">
              {nodeChildren.map((childNode, childIndex) => (
                <PrerequisiteNodeEditor
                  key={`${pathKey(path)}-child-${childIndex}`}
                  node={childNode}
                  path={[...path, childIndex]}
                  availableCourses={availableCourses}
                  onAddChildNode={onAddChildNode}
                  onChangeNodeCourse={onChangeNodeCourse}
                  onChangeNodeType={onChangeNodeType}
                  onClearTree={onClearTree}
                  onRemoveNode={onRemoveNode}
                />
              ))}
            </ul>
          ) : (
            <p className="is-size-7 has-text-grey mb-0">
              Add at least two children before saving this node.
            </p>
          )}
        </>
      )}
    </li>
  )
}

function PrerequisiteTreeEditor({
  tree,
  availableCourses,
  onSetRootType,
  onClearTree,
  onChangeNodeType,
  onChangeNodeCourse,
  onAddChildNode,
  onRemoveNode,
}) {
  if (!tree) {
    return (
      <div className="admin-empty-tree">
        <p className="is-size-7 has-text-grey mb-2">
          This course currently has no prerequisite tree.
        </p>
        <div className="buttons are-small mb-0">
          <button className="button is-light" type="button" onClick={() => onSetRootType('COURSE')}>
            Add COURSE Root
          </button>
          <button className="button is-light" type="button" onClick={() => onSetRootType('AND')}>
            Add AND Root
          </button>
          <button className="button is-light" type="button" onClick={() => onSetRootType('OR')}>
            Add OR Root
          </button>
        </div>
      </div>
    )
  }

  return (
    <ul className="admin-prereq-tree">
      <PrerequisiteNodeEditor
        node={tree}
        path={[]}
        availableCourses={availableCourses}
        onAddChildNode={onAddChildNode}
        onChangeNodeCourse={onChangeNodeCourse}
        onChangeNodeType={onChangeNodeType}
        onClearTree={onClearTree}
        onRemoveNode={onRemoveNode}
      />
    </ul>
  )
}

export default PrerequisiteTreeEditor
