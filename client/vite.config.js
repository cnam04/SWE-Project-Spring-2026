import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// In Docker the backend is reachable at http://backend:8080;
// locally it's http://localhost:8080 (the default).
const apiTarget = process.env.VITE_API_BASE_URL || 'http://localhost:8080';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,          // listen on 0.0.0.0 so Docker port-mapping works
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true
      }
    }
  }
})
