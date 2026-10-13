import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
})

// Request interceptor to attach JWT token to all requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle errors and session expiration
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response) {
      // 401 Unauthorized: token expired or invalid
      const token = localStorage.getItem('token')
      const isDemoToken = token && token.startsWith('demo-')
      const url = error.config?.url || ''
      const isPublicEndpoint = url.includes('/complaints/number/') || url.includes('/timeline')

      if (error.response.status === 401 && !isDemoToken && !isPublicEndpoint) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.dispatchEvent(new Event('civiceye:auth:logout'))
      }
    }
    return Promise.reject(error)
  }
)

export default api
