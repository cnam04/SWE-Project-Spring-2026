import React, { useCallback, useEffect, useRef, useState } from 'react';
import CourseGraphCanvas from '../components/graph/CourseGraphCanvas';
import { useCoursePrereqGraphHandler } from './handlers/useCoursePrereqGraphHandler';
import { loadAdminCourseDetail, loadAdminCourses } from '../services/adminCoursesService';
import '../styles/CoursePrereqPage.css';

function asSearchValue(value) {
  if (value === null || value === undefined) {
    return '';
  }

  return String(value).toLowerCase();
}

function asDisplayValue(value) {
  if (value === null || value === undefined || value === '') {
    return 'N/A';
  }

  return String(value);
}

export default function CoursePrereqPage() {
  const [courses, setCourses] = useState([]);
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [isRefreshingCourses, setIsRefreshingCourses] = useState(false);
  const [coursesError, setCoursesError] = useState('');

  const [selectedCourseId, setSelectedCourseId] = useState(null);
  const [selectedCourseDetail, setSelectedCourseDetail] = useState(null);
  const [selectedCourseLoading, setSelectedCourseLoading] = useState(false);
  const [selectedCourseError, setSelectedCourseError] = useState('');
  const selectedCourseRequestIdRef = useRef(0);

  const [searchText, setSearchText] = useState('');
  const [searchField, setSearchField] = useState('title');

  const {
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
  } = useCoursePrereqGraphHandler();

  const fetchCourses = useCallback(async (showRefreshingState) => {
    if (showRefreshingState) {
      setIsRefreshingCourses(true);
    }

    try {
      const nextCourses = await loadAdminCourses();
      setCourses(nextCourses);
      setCoursesError('');
    } catch (err) {
      setCoursesError(err.message || 'Unable to load courses');
    } finally {
      setLoadingCourses(false);
      setIsRefreshingCourses(false);
    }
  }, []);

  const fetchSelectedCourseDetail = useCallback(async (courseId) => {
    const requestId = selectedCourseRequestIdRef.current + 1;
    selectedCourseRequestIdRef.current = requestId;

    setSelectedCourseLoading(true);
    setSelectedCourseError('');

    try {
      const detail = await loadAdminCourseDetail(courseId);

      if (selectedCourseRequestIdRef.current !== requestId) {
        return;
      }

      setSelectedCourseDetail(detail);
    } catch (err) {
      if (selectedCourseRequestIdRef.current !== requestId) {
        return;
      }

      setSelectedCourseDetail(null);
      setSelectedCourseError(err.message || 'Unable to load selected course details');
    } finally {
      if (selectedCourseRequestIdRef.current === requestId) {
        setSelectedCourseLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    fetchCourses(false);
  }, [fetchCourses]);

  useEffect(() => {
    if (selectedCourseId === null) {
      return;
    }

    const hasSelectedCourse = courses.some((course) => String(course.courseId) === String(selectedCourseId));

    if (!hasSelectedCourse) {
      setSelectedCourseId(null);
      setSelectedCourseDetail(null);
      setSelectedCourseError('');
      resetGraphState();
    }
  }, [courses, resetGraphState, selectedCourseId]);

  const handleSelectCourse = (courseId) => {
    resetGraphState();
    setSelectedCourseId(courseId);
    fetchSelectedCourseDetail(courseId);
  };

  const handleUnselectCourse = () => {
    selectedCourseRequestIdRef.current += 1;
    setSelectedCourseId(null);
    setSelectedCourseDetail(null);
    setSelectedCourseLoading(false);
    setSelectedCourseError('');
    resetGraphState();
  };

  const handleRetryCourses = () => {
    fetchCourses(true);
  };

  const handleRetrySelectedCourse = () => {
    if (selectedCourseId === null) {
      return;
    }

    fetchSelectedCourseDetail(selectedCourseId);
  };

  const handleGenerateGraphForSelectedCourse = () => {
    handleGenerateGraph(selectedCourseId);
  };

  const filteredCourses = courses.filter((course) => {
    const query = searchText.trim().toLowerCase();
    if (!query) {
      return true;
    }

    if (searchField === 'courseCode') {
      return asSearchValue(course.courseCode).includes(query);
    }

    if (searchField === 'courseId') {
      return asSearchValue(course.courseId).includes(query);
    }

    if (searchField === 'crn') {
      return asSearchValue(course.crn).includes(query);
    }

    return asSearchValue(course.title).includes(query);
  });

  return (
    <section className="app-page">
      <div className="container is-app">
        <header className="app-page-header">
          <h1 className="title is-4 app-page-title">Course Prerequisite View</h1>
        </header>

        <section className="box app-surface mb-4">
          <div className="columns is-multiline is-variable is-3">
            <div className="column is-12-mobile is-7-tablet">
              <label className="label is-small">Search Courses</label>
              <div className="control">
                <input
                  className="input"
                  type="text"
                  value={searchText}
                  onChange={(event) => setSearchText(event.target.value)}
                  placeholder="Type to filter courses..."
                />
              </div>
            </div>
            <div className="column is-12-mobile is-5-tablet">
              <label className="label is-small">Search By</label>
              <div className="select is-fullwidth">
                <select value={searchField} onChange={(event) => setSearchField(event.target.value)}>
                  <option value="title">Title</option>
                  <option value="crn">CRN</option>
                  <option value="courseCode">Course Code</option>
                  <option value="courseId">Course ID</option>
                </select>
              </div>
            </div>
          </div>
        </section>

        <div className="columns is-variable is-4 prereq-layout">
          <aside className="column is-12-mobile is-3-desktop">
            <section className="box app-surface h-full">
              <h2 className="title is-5 mb-2">Search Results</h2>
              {loadingCourses ? (
                <p className="is-size-7 has-text-grey mb-0">Loading courses...</p>
              ) : coursesError ? (
                <div>
                  <p className="is-size-7 has-text-danger mb-3">{coursesError}</p>
                  <button className="button is-small is-link is-light" type="button" onClick={handleRetryCourses} disabled={isRefreshingCourses}>
                    {isRefreshingCourses ? 'Retrying...' : 'Retry'}
                  </button>
                </div>
              ) : (
                <>
                  <p className="is-size-7 has-text-grey mb-3">
                    Showing {filteredCourses.length} course{filteredCourses.length === 1 ? '' : 's'}.
                  </p>
                  <div className="tags mb-0">
                    {filteredCourses.map((course) => (
                      <span key={course.courseId} className="tag is-light">{course.courseCode || `ID ${course.courseId}`}</span>
                    ))}
                  </div>
                </>
              )}
              <div className="course-list mt-4">
                {loadingCourses ? (
                <p className="is-size-7 has-text-grey">Loading courses from API...</p>
              ) : coursesError ? (
                <p className="is-size-7 has-text-danger">Unable to render course list until courses load successfully.</p>
              ) : filteredCourses.length ? (
                <div className="sample-node-list">
                  {filteredCourses.map((course) => (
                    <article key={course.courseId} className="media node-item">
                      <div className="media-content">
                        <p className="has-text-weight-semibold mb-1">{course.title}</p>
                        <p className="is-size-7 has-text-grey mb-0">
                          {course.courseCode || `ID ${course.courseId}`} • CRN {asDisplayValue(course.crn)}
                        </p>
                      </div>
                      <div className="media-right">
                        {selectedCourseId === course.courseId ? (
                          <button className="button is-small is-warning is-light" onClick={handleUnselectCourse}>
                            Unselect
                          </button>
                        ) : (
                          <button className="button is-small is-link is-light" onClick={() => handleSelectCourse(course.courseId)}>
                            Select
                          </button>
                        )}
                      </div>
                    </article>
                  ))}
                </div>
              ) : (
                <p className="is-size-7 has-text-grey">No courses match the current filter.</p>
              )}
              </div>
            </section>
          </aside>

          <main className="column is-12-mobile is-6-desktop">
            <section className="box app-surface h-full prereq-main-panel">
              <h2 className="title is-5 mb-2">Graph Visualization</h2>
              <p className="is-size-7 has-text-grey mb-4">
                Generate a graph from the selected course details panel.
              </p>

              <CourseGraphCanvas graphData={graphData} isLoading={isGraphLoading} error={graphError} />
            </section>
          </main>

          <aside className="column is-12-mobile is-3-desktop">
            <section className="box app-surface h-full">
              <h2 className="title is-5 mb-3">Course Details</h2>
              {selectedCourseLoading ? (
                <p className="is-size-7 has-text-grey">Loading selected course details...</p>
              ) : selectedCourseError ? (
                <div>
                  <p className="is-size-7 has-text-danger mb-3">{selectedCourseError}</p>
                  <button className="button is-small is-link is-light" type="button" onClick={handleRetrySelectedCourse}>
                    Retry
                  </button>
                </div>
              ) : selectedCourseDetail ? (
                <div>
                  <p className="detail-row"><span>Course ID</span><strong>{asDisplayValue(selectedCourseDetail.courseId)}</strong></p>
                  <p className="detail-row"><span>Course Code</span><strong>{asDisplayValue(selectedCourseDetail.courseCode)}</strong></p>
                  <p className="detail-row"><span>CRN</span><strong>{asDisplayValue(selectedCourseDetail.crn)}</strong></p>
                  <p className="detail-row"><span>Title</span><strong>{asDisplayValue(selectedCourseDetail.title)}</strong></p>
                  <p className="detail-row"><span>Credits</span><strong>{asDisplayValue(selectedCourseDetail.credits)}</strong></p>

                  <p className="is-size-7 has-text-grey mt-3 mb-2">Attributes</p>
                  <div className="tags">
                    {Array.isArray(selectedCourseDetail.attributes) && selectedCourseDetail.attributes.length ? (
                      selectedCourseDetail.attributes.map((attribute) => (
                        <span key={attribute} className="tag is-info is-light">{attribute}</span>
                      ))
                    ) : (
                      <span className="is-size-7 has-text-grey">No attributes</span>
                    )}
                  </div>

                  <p className="is-size-7 has-text-grey mt-3 mb-1">Prerequisite Summary</p>
                  <p className="is-size-7 mb-0">{selectedCourseDetail.prerequisiteExpression}</p>

                  <div className="graph-options-section mt-4">
                    <p className="is-size-7 has-text-grey mb-2">Graph Options</p>

                    <p className="label is-small mb-1">Use Student Context?</p>
                    <div className="field mb-2">
                      <label className="radio mr-3">
                        <input
                          type="radio"
                          name="use-student-context"
                          checked={!useStudentContext}
                          onChange={() => setUseStudentContext(false)}
                        />
                        <span className="ml-1">No</span>
                      </label>
                      <label className="radio">
                        <input
                          type="radio"
                          name="use-student-context"
                          checked={useStudentContext}
                          onChange={() => setUseStudentContext(true)}
                        />
                        <span className="ml-1">Yes</span>
                      </label>
                    </div>

                    {useStudentContext ? (
                      <div className="field mb-2">
                        <label className="label is-small mb-1">Student ID</label>
                        <div className="control">
                          <input
                            className="input is-small"
                            type="text"
                            inputMode="numeric"
                            value={studentIdInput}
                            onChange={(event) => setStudentIdInput(event.target.value)}
                            placeholder="Enter student ID"
                          />
                        </div>
                      </div>
                    ) : null}

                    <p className="label is-small mb-1">Expand prerequisite chain?</p>
                    <div className="field mb-3">
                      <label className="radio mr-3">
                        <input
                          type="radio"
                          name="expand-prereq-graph"
                          checked={!expandGraph}
                          onChange={() => setExpandGraph(false)}
                        />
                        <span className="ml-1">False</span>
                      </label>
                      <label className="radio">
                        <input
                          type="radio"
                          name="expand-prereq-graph"
                          checked={expandGraph}
                          onChange={() => setExpandGraph(true)}
                        />
                        <span className="ml-1">True</span>
                      </label>
                    </div>

                    <button
                      className="button is-small is-link"
                      type="button"
                      onClick={handleGenerateGraphForSelectedCourse}
                      disabled={selectedCourseId === null || isGraphLoading}
                    >
                      {isGraphLoading ? 'Generating...' : 'Generate Graph'}
                    </button>
                  </div>
                </div>
              ) : (
                <p className="is-size-7 has-text-grey">
                  Select a course to inspect details from the API.
                </p>
              )}
            </section>
          </aside>
        </div>
      </div>
    </section>
  );
}
