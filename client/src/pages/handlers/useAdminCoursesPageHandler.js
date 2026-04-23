import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createAdminCourse,
  loadAdminCourseDetail,
  loadAdminCourses,
} from '../../services/adminCoursesService'
import {
  buildPrerequisiteExpression,
  mapCourseDetailToAdminSummary,
} from '../../services/utils/adminCoursesMapper'
import {
  addChildNodeAtPath,
  changeNodeTypeAtPath,
  createPrerequisiteNode,
  removeNodeAtPath,
  setNodeCourseCodeAtPath,
  validatePrerequisiteTreeBeforeSave,
} from '../../services/utils/adminCourseTreeUtils'

const EMPTY_ADD_COURSE_DRAFT = {
  courseCode: '',
  crn: '',
  title: '',
  credits: '',
  attributesText: '',
}

function toComparableId(value) {
  if (value === null || value === undefined) {
    return ''
  }

  return String(value)
}

function asSearchText(value) {
  if (value === null || value === undefined) {
    return ''
  }

  return String(value).toLowerCase()
}

function normalizeCourseCode(value) {
  return String(value || '').trim().toUpperCase()
}

function validateAddCourseDraft(draft, prerequisiteTree) {
  const errors = []

  const courseCode = String(draft?.courseCode || '').trim()
  const title = String(draft?.title || '').trim()
  const creditsInput = String(draft?.credits || '').trim()

  if (!courseCode) {
    errors.push('course_code is required')
  }

  if (!title) {
    errors.push('title is required')
  }

  if (!creditsInput) {
    errors.push('credits is required')
  } else {
    const parsedCredits = Number(creditsInput)
    const isValidInteger = Number.isInteger(parsedCredits)

    if (!isValidInteger) {
      errors.push('credits must be a whole number')
    } else if (parsedCredits < 0) {
      errors.push('credits must be >= 0')
    }
  }

  return [
    ...errors,
    ...validatePrerequisiteTreeBeforeSave(prerequisiteTree, courseCode),
  ]
}

function sortByField(courses, sortField) {
  const nextCourses = [...courses]

  nextCourses.sort((left, right) => {
    if (sortField === 'credits') {
      const leftCredits = Number(left.credits ?? -1)
      const rightCredits = Number(right.credits ?? -1)
      return leftCredits - rightCredits
    }

    return String(left[sortField] || '').localeCompare(String(right[sortField] || ''), undefined, {
      sensitivity: 'base',
      numeric: true,
    })
  })

  return nextCourses
}

export function useAdminCoursesPageHandler() {
  const [courses, setCourses] = useState([])
  const [loadingCourses, setLoadingCourses] = useState(true)
  const [coursesError, setCoursesError] = useState('')
  const [isRefreshingCourses, setIsRefreshingCourses] = useState(false)

  const [searchText, setSearchText] = useState('')
  const [searchField, setSearchField] = useState('title')
  const [sortField, setSortField] = useState('courseCode')

  const [selectedCourseId, setSelectedCourseId] = useState(null)
  const [selectedCourseDetail, setSelectedCourseDetail] = useState(null)
  const [selectedCourseLoading, setSelectedCourseLoading] = useState(false)
  const [selectedCourseError, setSelectedCourseError] = useState('')
  const [courseDetailCache, setCourseDetailCache] = useState({})

  const [isAddCourseMode, setIsAddCourseMode] = useState(false)
  const [addCourseDraft, setAddCourseDraft] = useState(EMPTY_ADD_COURSE_DRAFT)
  const [addCoursePrerequisiteTree, setAddCoursePrerequisiteTree] = useState(null)
  const [addCourseValidationErrors, setAddCourseValidationErrors] = useState([])
  const [addCourseSubmitError, setAddCourseSubmitError] = useState('')
  const [isSavingCourse, setIsSavingCourse] = useState(false)

  const fetchCourses = useCallback(async (showRefreshingState) => {
    if (showRefreshingState) {
      setIsRefreshingCourses(true)
    }

    try {
      const nextCourses = await loadAdminCourses()
      setCourses(nextCourses)
      setCoursesError('')
    } catch (err) {
      setCoursesError(err.message || 'Unable to load courses')
    } finally {
      setLoadingCourses(false)
      setIsRefreshingCourses(false)
    }
  }, [])

  const resetAddCourseDraft = useCallback(() => {
    setAddCourseDraft(EMPTY_ADD_COURSE_DRAFT)
    setAddCoursePrerequisiteTree(null)
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  useEffect(() => {
    fetchCourses(false)
  }, [fetchCourses])

  const fetchSelectedCourseDetail = useCallback(async (courseId, forceRefresh = false) => {
    if (!courseId && courseId !== 0) {
      setSelectedCourseDetail(null)
      setSelectedCourseError('')
      return
    }

    const cacheKey = toComparableId(courseId)
    if (!forceRefresh && courseDetailCache[cacheKey]) {
      setSelectedCourseDetail(courseDetailCache[cacheKey])
      setSelectedCourseError('')
      return
    }

    setSelectedCourseLoading(true)

    try {
      const detail = await loadAdminCourseDetail(courseId)
      setSelectedCourseDetail(detail)
      setSelectedCourseError('')
      setCourseDetailCache((previousCache) => ({
        ...previousCache,
        [cacheKey]: detail,
      }))
    } catch (err) {
      setSelectedCourseDetail(null)
      setSelectedCourseError(err.message || 'Unable to load selected course details')
    } finally {
      setSelectedCourseLoading(false)
    }
  }, [courseDetailCache])

  useEffect(() => {
    if (selectedCourseId === null) {
      setSelectedCourseDetail(null)
      setSelectedCourseError('')
      return
    }

    fetchSelectedCourseDetail(selectedCourseId)
  }, [fetchSelectedCourseDetail, selectedCourseId])

  useEffect(() => {
    if (selectedCourseId === null) {
      return
    }

    const hasSelectedCourse = courses.some((course) => (
      toComparableId(course.courseId) === toComparableId(selectedCourseId)
    ))

    if (!hasSelectedCourse) {
      setSelectedCourseId(null)
      setSelectedCourseDetail(null)
      setSelectedCourseError('')
    }
  }, [courses, selectedCourseId])

  const filteredCourses = useMemo(() => {
    const query = searchText.trim().toLowerCase()

    const matchingCourses = query
      ? courses.filter((course) => {
        if (searchField === 'courseCode') {
          return asSearchText(course.courseCode).includes(query)
        }

        if (searchField === 'crn') {
          return asSearchText(course.crn).includes(query)
        }

        return asSearchText(course.title).includes(query)
      })
      : courses

    return sortByField(matchingCourses, sortField)
  }, [courses, searchField, searchText, sortField])

  const availablePrerequisiteCourses = useMemo(() => {
    const normalizedCurrentDraftCode = normalizeCourseCode(addCourseDraft.courseCode)

    return sortByField(courses, 'courseCode').filter((course) => (
      normalizeCourseCode(course.courseCode) !== normalizedCurrentDraftCode
    ))
  }, [addCourseDraft.courseCode, courses])

  const addCoursePrerequisiteSummary = useMemo(() => (
    buildPrerequisiteExpression(addCoursePrerequisiteTree)
  ), [addCoursePrerequisiteTree])

  const handleRefreshCourses = useCallback(() => {
    fetchCourses(true)
  }, [fetchCourses])

  const handleOpenAddCourse = useCallback(() => {
    resetAddCourseDraft()
    setIsAddCourseMode(true)
  }, [resetAddCourseDraft])

  const handleCancelAddCourse = useCallback(() => {
    setIsAddCourseMode(false)
    resetAddCourseDraft()
  }, [resetAddCourseDraft])

  const handleAddCourseDraftChange = useCallback((fieldName, nextValue) => {
    setAddCourseDraft((previousDraft) => ({
      ...previousDraft,
      [fieldName]: nextValue,
    }))
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleSetPrerequisiteRootType = useCallback((nodeType) => {
    setAddCoursePrerequisiteTree(createPrerequisiteNode(nodeType))
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleClearPrerequisiteTree = useCallback(() => {
    setAddCoursePrerequisiteTree(null)
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleChangePrerequisiteNodeType = useCallback((path, nodeType) => {
    setAddCoursePrerequisiteTree((previousTree) => changeNodeTypeAtPath(previousTree, path, nodeType))
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleChangePrerequisiteNodeCourse = useCallback((path, courseCode) => {
    setAddCoursePrerequisiteTree((previousTree) => setNodeCourseCodeAtPath(previousTree, path, courseCode))
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleAddPrerequisiteChild = useCallback((path, childType) => {
    setAddCoursePrerequisiteTree((previousTree) => addChildNodeAtPath(previousTree, path, childType))
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleRemovePrerequisiteNode = useCallback((path) => {
    setAddCoursePrerequisiteTree((previousTree) => removeNodeAtPath(previousTree, path))
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')
  }, [])

  const handleSaveAddCourse = useCallback(async () => {
    const validationErrors = validateAddCourseDraft(addCourseDraft, addCoursePrerequisiteTree)
    if (validationErrors.length) {
      setAddCourseValidationErrors(validationErrors)
      setAddCourseSubmitError('')
      return
    }

    setIsSavingCourse(true)
    setAddCourseValidationErrors([])
    setAddCourseSubmitError('')

    try {
      const createdCourse = await createAdminCourse(addCourseDraft, addCoursePrerequisiteTree)
      const createdSummary = mapCourseDetailToAdminSummary(createdCourse)
      const createdCacheKey = toComparableId(createdCourse.courseId)

      setCourses((previousCourses) => {
        const uniqueCourses = previousCourses.filter((course) => (
          toComparableId(course.courseId) !== createdCacheKey
        ))

        return [...uniqueCourses, createdSummary]
      })

      setCourseDetailCache((previousCache) => ({
        ...previousCache,
        [createdCacheKey]: createdCourse,
      }))

      setSelectedCourseId(createdCourse.courseId)
      setSelectedCourseDetail(createdCourse)
      setSelectedCourseError('')
      setSearchText('')
      setIsAddCourseMode(false)
      resetAddCourseDraft()
    } catch (err) {
      setAddCourseSubmitError(err.message || 'Unable to create course')
    } finally {
      setIsSavingCourse(false)
    }
  }, [addCourseDraft, addCoursePrerequisiteTree, resetAddCourseDraft])

  const handleSelectCourse = useCallback((courseId) => {
    setSelectedCourseId(courseId)
    setIsAddCourseMode(false)
  }, [])

  const handleClearCourseSelection = useCallback(() => {
    setSelectedCourseId(null)
  }, [])

  const handleRetrySelectedCourse = useCallback(() => {
    if (selectedCourseId === null) {
      return
    }

    fetchSelectedCourseDetail(selectedCourseId, true)
  }, [fetchSelectedCourseDetail, selectedCourseId])

  return {
    allCoursesCount: courses.length,
    allCourses: courses,
    visibleCourses: filteredCourses,
    loadingCourses,
    coursesError,
    isRefreshingCourses,
    isAddCourseMode,
    addCourseDraft,
    addCoursePrerequisiteTree,
    addCoursePrerequisiteSummary,
    addCourseValidationErrors,
    addCourseSubmitError,
    isSavingCourse,
    availablePrerequisiteCourses,
    searchText,
    searchField,
    sortField,
    selectedCourseId,
    selectedCourseDetail,
    selectedCourseLoading,
    selectedCourseError,
    courseMutationsReady: false,
    setSearchText,
    setSearchField,
    setSortField,
    handleOpenAddCourse,
    handleCancelAddCourse,
    handleAddCourseDraftChange,
    handleSetPrerequisiteRootType,
    handleClearPrerequisiteTree,
    handleChangePrerequisiteNodeType,
    handleChangePrerequisiteNodeCourse,
    handleAddPrerequisiteChild,
    handleRemovePrerequisiteNode,
    handleSaveAddCourse,
    handleRefreshCourses,
    handleSelectCourse,
    handleClearCourseSelection,
    handleRetrySelectedCourse,
  }
}
