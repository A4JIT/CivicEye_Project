import api from './api'

export const authService = {
  /**
   * Log in user with username/email and password
   * @param {Object} credentials - { usernameOrEmail, password }
   * @returns {Promise<Object>} ApiResponse containing AuthResponse
   */
  async login(credentials) {
    const response = await api.post('/api/auth/login', credentials)
    return response.data
  },

  /**
   * Register a new user
   * @param {Object} userData - { username, email, password, fullName, role }
   * @returns {Promise<Object>} ApiResponse containing AuthResponse
   */
  async register(userData) {
    const response = await api.post('/api/auth/register', userData)
    return response.data
  },

  /**
   * Check authentication service health
   * @returns {Promise<Object>}
   */
  async getHealth() {
    const response = await api.get('/api/auth/health')
    return response.data
  },

  /**
   * Save authentication payload to local storage
   */
  setSession(authData) {
    if (authData.token) {
      localStorage.setItem('token', authData.token)
    }
    const user = {
      userId: authData.userId,
      username: authData.username,
      email: authData.email,
      fullName: authData.fullName,
      role: authData.role,
    }
    localStorage.setItem('user', JSON.stringify(user))
  },

  /**
   * Clear authentication payload from local storage
   */
  clearSession() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  /**
   * Get current user from local storage
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('user')
    if (!userStr) return null
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  },

  /**
   * Get JWT token from local storage
   */
  getToken() {
    return localStorage.getItem('token')
  },
}

export default authService
