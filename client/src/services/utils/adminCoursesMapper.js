function normalizeText(value, fallback = '') {
  if (value === null || value === undefined) {
    return fallback
  }

  return String(value)
}

function normalizeAttributes(attributes) {
  if (!Array.isArray(attributes)) {
    return []
  }

  return attributes
    .map((attribute) => normalizeText(attribute).trim())
    .filter(Boolean)
}

function normalizePrerequisiteNode(node) {
  if (!node || typeof node !== 'object') {
    return null
  }

  const rawType = normalizeText(node.type).toUpperCase()
  const type = rawType === 'COURSE' || rawType === 'AND' || rawType === 'OR' ? rawType : 'COURSE'

  if (type === 'COURSE') {
    return {
      type,
      courseCode: normalizeText(node.courseCode).trim(),
      children: [],
    }
  }

  const rawChildren = Array.isArray(node.children) ? node.children : []

  return {
    type,
    courseCode: '',
    children: rawChildren
      .map((child) => normalizePrerequisiteNode(child))
      .filter(Boolean),
  }
}

function sanitizePrerequisiteTreeForRequest(node) {
  const normalizedNode = normalizePrerequisiteNode(node)

  if (!normalizedNode) {
    return null
  }

  if (normalizedNode.type === 'COURSE') {
    return {
      type: 'COURSE',
      courseCode: normalizeText(normalizedNode.courseCode).trim(),
      children: [],
    }
  }

  return {
    type: normalizedNode.type,
    children: normalizedNode.children.map((childNode) => sanitizePrerequisiteTreeForRequest(childNode)),
  }
}

export function buildPrerequisiteExpression(node) {
  if (!node) {
    return 'This course has no prerequisites.'
  }

  if (node.type === 'COURSE') {
    return node.courseCode || 'Unknown course'
  }

  const validChildren = Array.isArray(node.children) ? node.children : []
  const renderedChildren = validChildren
    .map((child) => buildPrerequisiteExpression(child))
    .filter(Boolean)

  if (!renderedChildren.length) {
    return `${node.type} (empty)`
  }

  if (renderedChildren.length === 1) {
    return renderedChildren[0]
  }

  return `(${renderedChildren.join(` ${node.type} `)})`
}

function mapCourseBase(rawCourse) {
  const source = rawCourse || {}

  return {
    courseId: source.course_id ?? source.courseId ?? source.id ?? null,
    courseCode: normalizeText(source.course_code ?? source.courseCode).trim(),
    crn: normalizeText(source.crn).trim(),
    title: normalizeText(source.title).trim(),
    credits: Number.isFinite(source.credits) ? source.credits : source.credits ?? null,
    attributes: normalizeAttributes(source.attributes),
  }
}

export function mapCourseDetailToAdminSummary(courseDetail) {
  return mapCourseBase(courseDetail)
}

export function mapCreateCourseDraftToRequest(draft, prerequisiteTree) {
  const source = draft || {}
  const rawAttributes = normalizeText(source.attributesText)

  const attributes = rawAttributes
    .split(',')
    .map((attribute) => attribute.trim())
    .filter(Boolean)
    .filter((attribute, index, allValues) => allValues.indexOf(attribute) === index)

  const normalizedCourseCode = normalizeText(source.courseCode).trim()
  const normalizedCrn = normalizeText(source.crn).trim()
  const normalizedTitle = normalizeText(source.title).trim()
  const creditsInput = normalizeText(source.credits).trim()

  return {
    course_code: normalizedCourseCode,
    crn: normalizedCrn || null,
    title: normalizedTitle,
    credits: creditsInput === '' ? null : Number(creditsInput),
    attributes,
    prerequisiteTree: sanitizePrerequisiteTreeForRequest(prerequisiteTree),
  }
}

export function mapCourseSummariesForAdminList(rawCourses) {
  if (!Array.isArray(rawCourses)) {
    return []
  }

  return rawCourses.map((rawCourse) => mapCourseBase(rawCourse))
}

export function mapCourseDetailForAdmin(rawCourseDetail) {
  const baseCourse = mapCourseBase(rawCourseDetail)
  const prerequisiteTree = normalizePrerequisiteNode(rawCourseDetail?.prerequisiteTree)

  return {
    ...baseCourse,
    prerequisiteTree,
    prerequisiteExpression: buildPrerequisiteExpression(prerequisiteTree),
  }
}
