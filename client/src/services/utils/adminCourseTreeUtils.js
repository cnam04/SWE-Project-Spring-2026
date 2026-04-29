function normalizeText(value, fallback = '') {
  if (value === null || value === undefined) {
    return fallback
  }

  return String(value)
}

function normalizeNodeType(nodeType) {
  const normalizedType = normalizeText(nodeType).trim().toUpperCase()

  if (normalizedType === 'AND' || normalizedType === 'OR' || normalizedType === 'COURSE') {
    return normalizedType
  }

  return 'COURSE'
}

function normalizeCourseCode(value) {
  return normalizeText(value).trim().toUpperCase()
}

function cloneNode(node) {
  if (!node || typeof node !== 'object') {
    return createPrerequisiteNode('COURSE')
  }

  const type = normalizeNodeType(node.type)

  if (type === 'COURSE') {
    return {
      type,
      courseCode: normalizeText(node.courseCode).trim(),
      children: [],
    }
  }

  const children = Array.isArray(node.children) ? node.children : []

  return {
    type,
    courseCode: '',
    children: children.map((childNode) => cloneNode(childNode)),
  }
}

function updateNodeAtPath(node, path, updater) {
  if (!node) {
    return node
  }

  if (!Array.isArray(path) || !path.length) {
    return updater(cloneNode(node))
  }

  const [childIndex, ...nextPath] = path
  if (typeof childIndex !== 'number' || childIndex < 0) {
    return cloneNode(node)
  }

  const currentNode = cloneNode(node)
  if (currentNode.type === 'COURSE') {
    return currentNode
  }

  const children = Array.isArray(currentNode.children) ? currentNode.children : []
  if (childIndex >= children.length) {
    return currentNode
  }

  const nextChildren = children.map((childNode, index) => (
    index === childIndex
      ? updateNodeAtPath(childNode, nextPath, updater)
      : cloneNode(childNode)
  ))

  return {
    ...currentNode,
    children: nextChildren,
  }
}

function pathToLabel(path) {
  if (!Array.isArray(path) || !path.length) {
    return 'Root'
  }

  return `Root > ${path.map((segment) => segment + 1).join(' > ')}`
}

function validateNode(node, path, draftCourseCode, errors) {
  if (!node || typeof node !== 'object') {
    errors.push(`${pathToLabel(path)} is not a valid node`)
    return
  }

  const type = normalizeNodeType(node.type)
  if (type !== normalizeText(node.type).trim().toUpperCase()) {
    errors.push(`${pathToLabel(path)} has an invalid node type`)
  }

  if (type === 'COURSE') {
    const courseCode = normalizeText(node.courseCode).trim()
    if (!courseCode) {
      errors.push(`${pathToLabel(path)} COURSE nodes must select a course`)
    }

    const children = Array.isArray(node.children) ? node.children : []
    if (children.length > 0) {
      errors.push(`${pathToLabel(path)} COURSE nodes cannot have children`)
    }

    if (courseCode && normalizeCourseCode(courseCode) === normalizeCourseCode(draftCourseCode)) {
      errors.push('A course cannot require itself')
    }

    return
  }

  const children = Array.isArray(node.children) ? node.children : []
  if (children.length < 2) {
    errors.push(`${pathToLabel(path)} ${type} nodes must contain at least 2 children`)
  }

  const siblingCourseCodes = new Set()
  for (const childNode of children) {
    if (!childNode || typeof childNode !== 'object') {
      continue
    }

    if (normalizeNodeType(childNode.type) === 'COURSE') {
      const childCourseCode = normalizeCourseCode(childNode.courseCode)
      if (!childCourseCode) {
        continue
      }

      if (siblingCourseCodes.has(childCourseCode)) {
        errors.push(`${pathToLabel(path)} has duplicate sibling course prerequisites (${childCourseCode})`)
      }

      siblingCourseCodes.add(childCourseCode)
    }
  }

  children.forEach((childNode, childIndex) => {
    validateNode(childNode, [...path, childIndex], draftCourseCode, errors)
  })
}

export function createPrerequisiteNode(nodeType = 'COURSE') {
  const type = normalizeNodeType(nodeType)

  if (type === 'COURSE') {
    return {
      type,
      courseCode: '',
      children: [],
    }
  }

  return {
    type,
    courseCode: '',
    children: [],
  }
}

export function changeNodeTypeAtPath(tree, path, nodeType) {
  if (!tree) {
    return tree
  }

  const nextNodeType = normalizeNodeType(nodeType)

  return updateNodeAtPath(tree, path, (node) => {
    if (nextNodeType === 'COURSE') {
      return {
        type: 'COURSE',
        courseCode: node.type === 'COURSE' ? normalizeText(node.courseCode).trim() : '',
        children: [],
      }
    }

    const children = node.type === 'COURSE'
      ? []
      : (Array.isArray(node.children) ? node.children.map((childNode) => cloneNode(childNode)) : [])

    return {
      type: nextNodeType,
      courseCode: '',
      children,
    }
  })
}

export function setNodeCourseCodeAtPath(tree, path, courseCode) {
  if (!tree) {
    return tree
  }

  return updateNodeAtPath(tree, path, (node) => {
    if (node.type !== 'COURSE') {
      return node
    }

    return {
      ...node,
      courseCode: normalizeText(courseCode).trim(),
      children: [],
    }
  })
}

export function addChildNodeAtPath(tree, path, childType) {
  if (!tree) {
    return tree
  }

  return updateNodeAtPath(tree, path, (node) => {
    if (node.type === 'COURSE') {
      return node
    }

    const children = Array.isArray(node.children) ? node.children : []

    return {
      ...node,
      children: [...children, createPrerequisiteNode(childType)],
    }
  })
}

export function removeNodeAtPath(tree, path) {
  if (!tree) {
    return null
  }

  if (!Array.isArray(path) || !path.length) {
    return null
  }

  const parentPath = path.slice(0, -1)
  const nodeIndex = path[path.length - 1]

  if (typeof nodeIndex !== 'number' || nodeIndex < 0) {
    return cloneNode(tree)
  }

  return updateNodeAtPath(tree, parentPath, (parentNode) => {
    if (parentNode.type === 'COURSE') {
      return parentNode
    }

    const children = Array.isArray(parentNode.children) ? parentNode.children : []

    return {
      ...parentNode,
      children: children
        .filter((_, index) => index !== nodeIndex)
        .map((childNode) => cloneNode(childNode)),
    }
  })
}

export function validatePrerequisiteTreeBeforeSave(tree, draftCourseCode) {
  if (!tree) {
    return []
  }

  const errors = []
  validateNode(tree, [], draftCourseCode, errors)

  return [...new Set(errors)]
}
