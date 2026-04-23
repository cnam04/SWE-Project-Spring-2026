import { createCourse, getCourseById, getCourses, updateCourse, updatePrerequisites } from '../api'
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
  // call POST /api/courses with the request payload, get back the created course, and map it for admin detail view
  const rawCreatedCourse = await createCourse(requestPayload)
  return mapCourseDetailForAdmin(rawCreatedCourse)
}

export async function updateAdminCourse(courseId, draftCourse, prerequisiteTree) {
  const requestPayload = mapCreateCourseDraftToRequest(draftCourse, prerequisiteTree)
  const { prerequisiteTree: requestPrerequisiteTree, ...patchPayload } = requestPayload

  // PATCH updates only basic course fields per API contract.
  const rawUpdatedCourse = await updateCourse(courseId, patchPayload)

  // PUT always sends the full current tree, including null to clear prerequisites.
  const rawUpdatedWithPrereqs = await updatePrerequisites(courseId, {
    prerequisiteTree: requestPrerequisiteTree,
  })

  return mapCourseDetailForAdmin(
    rawUpdatedWithPrereqs || {
      ...rawUpdatedCourse,
      prerequisiteTree: requestPrerequisiteTree,
    },
  )
}

export async function updateAdminCoursePrerequisites(courseId, prerequisiteTree) {
  const requestPayload = { prerequisiteTree }
  await updatePrerequisites(courseId, requestPayload) // call PUT /api/courses/:courseId/prerequisites with the new tree
}
