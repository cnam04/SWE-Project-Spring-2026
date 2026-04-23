import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  loadAdminCourseDetail,
  loadAdminCourses,
} from '../../services/adminCoursesService'

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

  const handleRefreshCourses = useCallback(() => {
    fetchCourses(true)
  }, [fetchCourses])

  const handleSelectCourse = useCallback((courseId) => {
    setSelectedCourseId(courseId)
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
    visibleCourses: filteredCourses,
    loadingCourses,
    coursesError,
    isRefreshingCourses,
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
    handleRefreshCourses,
    handleSelectCourse,
    handleClearCourseSelection,
    handleRetrySelectedCourse,
  }
}
