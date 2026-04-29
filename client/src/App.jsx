import TestItemsPage from './pages/ExampleTestItemsPage'
import './App.css'
import Navbar from './components/Navbar'
import { Routes, Route } from 'react-router-dom'
import AdminPage from './pages/AdminPage'
import CoursePrereqPage from './pages/CoursePrereqPage'

function App() {
  return (
    <>
      <Navbar></Navbar>
      <Routes>
        <Route path="/" element={<TestItemsPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/prereq-vis" element={<CoursePrereqPage />} />
      </Routes>
    </>

  )
  
}

export default App
