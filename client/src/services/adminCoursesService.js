import { getCourseById, getCourses } from '../api'
import {
  mapCourseDetailForAdmin,
  mapCourseSummariesForAdminList,
} from './utils/adminCoursesMapper'

/**
 * SERVICE LAYER:
 * Shapes admin course payloads for UI use.
 */
export async function loadAdminCourses() {
  const rawCourses = await getCourses()
  return mapCourseSummariesForAdminList(rawCourses)
}

export async function loadAdminCourseDetail(courseId) {
  const rawCourse = await getCourseById(courseId)
  return mapCourseDetailForAdmin(rawCourse)
}
