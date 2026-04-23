import { createCourse, getCourseById, getCourses } from '../api'
import {
  mapCreateCourseDraftToRequest,
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

export async function createAdminCourse(draftCourse, prerequisiteTree) {
  const requestPayload = mapCreateCourseDraftToRequest(draftCourse, prerequisiteTree)
  const rawCreatedCourse = await createCourse(requestPayload)
  return mapCourseDetailForAdmin(rawCreatedCourse)
}
