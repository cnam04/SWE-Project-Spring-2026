import { getCourseGraph } from '../api'
import { mapCoursePrerequisiteGraph } from './utils/prerequisiteGraphMapper'

function hasValue(value) {
  return value !== null && value !== undefined && value !== ''
}

function parseInteger(value) {
  const parsed = Number(value)
  return Number.isInteger(parsed) ? parsed : null
}

function normalizeCourseId(courseId) {
  if (!hasValue(courseId)) {
    throw new Error('Select a course before generating a graph.')
  }

  const parsedId = parseInteger(courseId)
  if (parsedId === null) {
    throw new Error('Course ID must be a number.')
  }

  return parsedId
}

function resolveStudentIdOptions(graphOptions = {}) {
  const rawStudentId = hasValue(graphOptions.studentId)
    ? graphOptions.studentId
    : graphOptions.studentIdInput

  const hasExplicitContextToggle = typeof graphOptions.useStudentContext === 'boolean'
  const useStudentContext = hasExplicitContextToggle
    ? graphOptions.useStudentContext
    : hasValue(rawStudentId)

  if (!useStudentContext) {
    return { useStudentContext: false, studentId: null }
  }

  if (!hasValue(rawStudentId)) {
    throw new Error('Enter a student ID or turn off student context.')
  }

  const parsedStudentId = parseInteger(rawStudentId)
  if (parsedStudentId === null) {
    throw new Error('Student ID must be a number.')
  }

  return {
    useStudentContext: true,
    studentId: parsedStudentId,
  }
}

export function buildCourseGraphQueryOptions(graphOptions = {}) {
  const nextQuery = {}
  const studentContext = resolveStudentIdOptions(graphOptions)

  if (studentContext.useStudentContext) {
    nextQuery.studentId = studentContext.studentId
  }

  if (graphOptions.expand === true) {
    nextQuery.expand = true
  }

  return nextQuery
}

export async function loadCoursePrerequisiteGraph(courseId, graphOptions = {}) {
  const normalizedCourseId = normalizeCourseId(courseId)
  const queryOptions = buildCourseGraphQueryOptions(graphOptions)
  const rawGraph = await getCourseGraph(normalizedCourseId, queryOptions)

  return mapCoursePrerequisiteGraph(rawGraph)
}
