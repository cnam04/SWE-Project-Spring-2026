import { useCallback, useRef, useState } from 'react'
import { loadCoursePrerequisiteGraph } from '../../services/prerequisiteGraphService'

export function useCoursePrereqGraphHandler() {
  const [graphData, setGraphData] = useState(null)
  const [isGraphLoading, setIsGraphLoading] = useState(false)
  const [graphError, setGraphError] = useState('')

  const [useStudentContext, setUseStudentContext] = useState(false)
  const [studentIdInput, setStudentIdInput] = useState('')
  const [expandGraph, setExpandGraph] = useState(false)

  const graphRequestIdRef = useRef(0)

  const resetGraphState = useCallback(() => {
    graphRequestIdRef.current += 1
    setGraphData(null)
    setIsGraphLoading(false)
    setGraphError('')
  }, [])

  const handleGenerateGraph = useCallback(async (courseId) => {
    const requestId = graphRequestIdRef.current + 1
    graphRequestIdRef.current = requestId

    setIsGraphLoading(true)
    setGraphError('')

    try {
      const nextGraphData = await loadCoursePrerequisiteGraph(courseId, {
        useStudentContext,
        studentIdInput,
        expand: expandGraph,
      })

      if (graphRequestIdRef.current !== requestId) {
        return
      }

      setGraphData(nextGraphData)
    } catch (err) {
      if (graphRequestIdRef.current !== requestId) {
        return
      }

      setGraphData(null)
      setGraphError(err.message || 'Could not generate graph.')
    } finally {
      if (graphRequestIdRef.current === requestId) {
        setIsGraphLoading(false)
      }
    }
  }, [expandGraph, studentIdInput, useStudentContext])

  return {
    graphData,
    isGraphLoading,
    graphError,
    useStudentContext,
    studentIdInput,
    expandGraph,
    setUseStudentContext,
    setStudentIdInput,
    setExpandGraph,
    handleGenerateGraph,
    resetGraphState,
  }
}
