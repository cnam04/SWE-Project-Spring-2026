function normalizeText(value, fallback = '') {
  if (value === null || value === undefined) {
    return fallback
  }

  return String(value)
}

function normalizeInteger(value) {
  if (value === null || value === undefined || value === '') {
    return null
  }

  const parsed = Number(value)
  if (!Number.isInteger(parsed)) {
    return null
  }

  return parsed
}

function normalizePosition(rawPosition) {
  const x = Number(rawPosition?.x)
  const y = Number(rawPosition?.y)

  return {
    x: Number.isFinite(x) ? x : 0,
    y: Number.isFinite(y) ? y : 0,
  }
}

function mapGraphNode(rawNode, index) {
  const source = rawNode || {}
  const fallbackId = `node-${index}`

  return {
    id: normalizeText(source.id, fallbackId),
    type: normalizeText(source.type, 'courseNode'),
    position: normalizePosition(source.position),
    data: source.data && typeof source.data === 'object' ? source.data : {},
  }
}

function mapGraphEdge(rawEdge, index) {
  const source = rawEdge || {}
  const fallbackId = `edge-${index}`

  return {
    id: normalizeText(source.id, fallbackId),
    source: normalizeText(source.source),
    target: normalizeText(source.target),
    type: normalizeText(source.type, 'smoothstep'),
    data: source.data && typeof source.data === 'object' ? source.data : {},
  }
}

export function mapCoursePrerequisiteGraph(rawGraph) {
  const source = rawGraph && typeof rawGraph === 'object' ? rawGraph : {}
  const nodes = Array.isArray(source.nodes) ? source.nodes : []
  const edges = Array.isArray(source.edges) ? source.edges : []

  return {
    courseId: source.courseId ?? source.course_id ?? null,
    courseCode: normalizeText(source.courseCode ?? source.course_code).trim(),
    title: normalizeText(source.title).trim(),
    studentId: normalizeInteger(source.studentId ?? source.student_id),
    statusMode: normalizeText(source.statusMode ?? source.status_mode),
    layoutDirection: normalizeText(source.layoutDirection ?? source.layout_direction, 'LR'),
    nodes: nodes.map((node, index) => mapGraphNode(node, index)),
    edges: edges.map((edge, index) => mapGraphEdge(edge, index)),
  }
}
