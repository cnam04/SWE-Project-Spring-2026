import React, { useState } from 'react';
import '../styles/CoursePrereqPage.css';
import ReactFlowVisualization from '../components/ReactFlowVisualization';

const DUMMY_COURSES = [
  {
    id: 'CS101',
    crn: '12345',
    title: 'Introduction to Computer Science',
    attributes: ['Freshman', 'Core'],
    completed: true,
  },
  {
    id: 'CS201',
    crn: '23456',
    title: 'Data Structures',
    attributes: ['Sophomore', 'Core'],
    completed: false,
  },
  {
    id: 'CS301',
    crn: '34567',
    title: 'Algorithms',
    attributes: ['Junior', 'Core'],
    completed: false,
  }
];

export default function CoursePrereqPage() {
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [searchText, setSearchText] = useState('');
  const [searchField, setSearchField] = useState('title');

  const handleSelectCourse = (course) => {
    setSelectedCourse(course);
  };

  const handleUnselectCourse = () => {
    setSelectedCourse(null);
  };

  const filteredCourses = DUMMY_COURSES.filter((course) => {
    const query = searchText.trim().toLowerCase();
    if (!query) {
      return true;
    }

    if (searchField === 'courseId') {
      return course.id.toLowerCase().includes(query);
    }

    return String(course[searchField]).toLowerCase().includes(query);
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
                  placeholder="Type to filter sample nodes..."
                />
              </div>
            </div>
            <div className="column is-12-mobile is-5-tablet">
              <label className="label is-small">Search By</label>
              <div className="select is-fullwidth">
                <select value={searchField} onChange={(event) => setSearchField(event.target.value)}>
                  <option value="title">Title</option>
                  <option value="crn">CRN</option>
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
              <p className="is-size-7 has-text-grey mb-3">
                Showing {filteredCourses.length} sample node{filteredCourses.length === 1 ? '' : 's'}.
              </p>
              <div className="tags mb-4">
                {filteredCourses.map((course) => (
                  <span key={course.id} className="tag is-light">{course.id}</span>
                ))}
              </div>

              <h3 className="title is-7 mb-2">Legend</h3>
              <div className="legend-stack">
                <p className="is-size-7"><span className="tag is-success is-light">Completed</span> Finished courses</p>
                <p className="is-size-7"><span className="tag is-info is-light">In Plan</span> Pending courses</p>
              </div>
            </section>
          </aside>

          <main className="column is-12-mobile is-6-desktop">
            <section className="box app-surface">
              <h2 className="title is-5 mb-2">Graph Visualization</h2>
              <p className="is-size-7 has-text-grey mb-4">
                Placeholder canvas for React Flow nodes and edges.
              </p>

                <ReactFlowVisualization />

              {filteredCourses.length ? (
                <div className="sample-node-list">
                  {filteredCourses.map((course) => (
                    <article key={course.id} className="media node-item">
                      <div className="media-content">
                        <p className="has-text-weight-semibold mb-1">{course.title}</p>
                        <p className="is-size-7 has-text-grey mb-0">{course.id} • CRN {course.crn}</p>
                      </div>
                      <div className="media-right">
                        {selectedCourse?.id === course.id ? (
                          <button className="button is-small is-warning is-light" onClick={handleUnselectCourse}>
                            Unselect
                          </button>
                        ) : (
                          <button className="button is-small is-link is-light" onClick={() => handleSelectCourse(course)}>
                            Select
                          </button>
                        )}
                      </div>
                    </article>
                  ))}
                </div>
              ) : (
                <p className="is-size-7 has-text-grey">No sample nodes match the current filter.</p>
              )}
            </section>
          </main>

          <aside className="column is-12-mobile is-3-desktop">
            <section className="box app-surface h-full">
              <h2 className="title is-5 mb-3">Course Details</h2>
              {selectedCourse ? (
                <div>
                  <p className="detail-row"><span>Course ID</span><strong>{selectedCourse.id}</strong></p>
                  <p className="detail-row"><span>CRN</span><strong>{selectedCourse.crn}</strong></p>
                  <p className="detail-row"><span>Title</span><strong>{selectedCourse.title}</strong></p>
                  <p className="detail-row"><span>Status</span><strong>{selectedCourse.completed ? 'Completed' : 'In Plan'}</strong></p>
                  <div className="tags mt-3">
                    {selectedCourse.attributes.map((attribute) => (
                      <span key={attribute} className="tag is-info is-light">{attribute}</span>
                    ))}
                  </div>
                </div>
              ) : (
                <p className="is-size-7 has-text-grey">
                  Select a sample node to inspect details.
                </p>
              )}
            </section>
          </aside>
        </div>
      </div>
    </section>
  );
}
