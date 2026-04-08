import React, { useState } from 'react';
import '../styles/CoursePrereqPage.css';

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

  const handleSelectCourse = (course) => {
    setSelectedCourse(course);
  };

  const handleUnselectCourse = () => {
    setSelectedCourse(null);
  };

  return (
    <div className="prereq-page">
      {/* Top Section */}
      <header className="top-section">
        <input type="text" placeholder="Search courses..." />
        <select defaultValue="title">
          <option value="title">Title</option>
          <option value="crn">CRN</option>
          <option value="courseId">Course ID</option>
        </select>
        <button>Search</button>
      </header>

      {/* Main Content Area */}
      <div className="main-content">
        {/* Left Sidebar */}
        <aside className="left-sidebar">
          <h3>Search Results</h3>
          <p>Result list here...</p>
          
          <hr />
          
          <h3>Filters</h3>
          <p>Filter chips/dropdowns...</p>
          
          <hr />
          
          <h3>Legend</h3>
          <p>Graph node meanings...</p>
        </aside>

        {/* Center Container */}
        <main className="center-container">
          <h2>Graph Visualization</h2>
          <p>This area will visually dominate the page and hold the graph.</p>
          
          <div className="dummy-course-list">
            <h3>Test Dummy Courses</h3>
            {DUMMY_COURSES.map((course) => (
              <div key={course.id} className="course-card">
                <h3>{course.title} ({course.id})</h3>
                <p>CRN: {course.crn}</p>
                <p>Attributes: {course.attributes.join(', ')}</p>
                <p>Completed: {course.completed ? 'Yes' : 'No'}</p>
                {selectedCourse?.id === course.id ? (
                  <button onClick={handleUnselectCourse}>Unselect Node</button>
                ) : (
                  <button onClick={() => handleSelectCourse(course)}>Select Node</button>
                )}
              </div>
            ))}
          </div>
        </main>

        {/* Right Panel */}
        <aside className="right-panel">
          <h3>Course Details</h3>
          {selectedCourse ? (
            <div>
              <p><strong>Course ID:</strong> {selectedCourse.id}</p>
              <p><strong>CRN:</strong> {selectedCourse.crn}</p>
              <p><strong>Title:</strong> {selectedCourse.title}</p>
              <p><strong>Attributes:</strong> {selectedCourse.attributes.join(', ')}</p>
              <p><strong>Completed:</strong> {selectedCourse.completed ? 'Yes' : 'No'}</p>
            </div>
          ) : (
            <p>Select a course node to view details.</p>
          )}
        </aside>
      </div>
    </div>
  );
}
