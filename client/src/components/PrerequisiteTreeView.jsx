function PrerequisiteTreeNode({ node, path }) {
  if (!node) {
    return null
  }

  if (node.type === 'COURSE') {
    return (
      <li className="admin-prereq-node admin-prereq-course" data-path={path}>
        <span className="tag is-info is-light">COURSE</span>
        <strong className="admin-prereq-course-code">{node.courseCode || 'Unknown course'}</strong>
      </li>
    )
  }

  const children = Array.isArray(node.children) ? node.children : []

  return (
    <li className="admin-prereq-node admin-prereq-group" data-path={path}>
      <div className="admin-prereq-node-head">
        <span className="tag is-light">{node.type}</span>
        <span className="is-size-7 has-text-grey">{children.length} child node{children.length === 1 ? '' : 's'}</span>
      </div>

      {children.length ? (
        <ul className="admin-prereq-children">
          {children.map((childNode, index) => (
            <PrerequisiteTreeNode key={`${path}-${index}`} node={childNode} path={`${path}-${index}`} />
          ))}
        </ul>
      ) : (
        <p className="is-size-7 has-text-grey mb-0">No child nodes.</p>
      )}
    </li>
  )
}

function PrerequisiteTreeView({ tree }) {
  if (!tree) {
    return (
      <p className="is-size-7 has-text-grey mb-0">
        This course has no prerequisites.
      </p>
    )
  }

  return (
    <ul className="admin-prereq-tree">
      <PrerequisiteTreeNode node={tree} path="root" />
    </ul>
  )
}

export default PrerequisiteTreeView
