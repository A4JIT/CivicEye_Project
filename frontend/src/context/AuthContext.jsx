import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import authService from '../services/authService'

export const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => authService.getCurrentUser())
  const [token, setToken] = useState(() => authService.getToken())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const logout = useCallback(() => {
    authService.clearSession()
    setUser(null)
    setToken(null)
    setError(null)
  }, [])

  useEffect(() => {
    // Listen for logout events dispatched by the axios 401 interceptor
    const handleLogoutEvent = () => {
      logout()
    }

    window.addEventListener('civiceye:auth:logout', handleLogoutEvent)
    return () => {
      window.removeEventListener('civiceye:auth:logout', handleLogoutEvent)
    }
  }, [logout])

  const login = async (usernameOrEmail, password) => {
    setLoading(true)
    setError(null)
    try {
      const response = await authService.login({ usernameOrEmail, password })
      const authData = response.data || response
      authService.setSession(authData)
      setToken(authData.token)
      setUser({
        id: authData.userId || authData.id,
        userId: authData.userId || authData.id,
        username: authData.username,
        email: authData.email,
        fullName: authData.fullName,
        role: authData.role,
      })
      return authData
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.message ||
        'Authentication failed. Please check your credentials.'
      setError(message)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }

  const register = async (userData) => {
    setLoading(true)
    setError(null)
    try {
      const response = await authService.register(userData)
      const authData = response.data || response
      if (authData.token) {
        authService.setSession(authData)
        setToken(authData.token)
        setUser({
          id: authData.userId || authData.id,
          userId: authData.userId || authData.id,
          username: authData.username,
          email: authData.email,
          fullName: authData.fullName,
          role: authData.role,
        })
      }
      return authData
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.message ||
        'Registration failed. Please verify the information provided.'
      setError(message)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }

  const clearError = () => setError(null)

  const value = {
    user,
    token,
    isAuthenticated: Boolean(token && user),
    loading,
    error,
    login,
    register,
    logout,
    clearError,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export default AuthContext
