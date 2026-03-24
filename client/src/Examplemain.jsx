import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './Exampleindex.css'
import App from './ExampleApp.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
