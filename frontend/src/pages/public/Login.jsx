import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Input from '../../components/common/Input'
import Button from '../../components/common/Button'
import ErrorMessage from '../../components/common/ErrorMessage'

const Login = () => {
  const { login, loading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
  })
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const validate = () => {
    const newErrors = {}

    if (!formData.usernameOrEmail.trim()) {
      newErrors.usernameOrEmail = 'Email or username is required'
    } else if (
      formData.usernameOrEmail.includes('@') &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.usernameOrEmail.trim())
    ) {
      newErrors.usernameOrEmail = 'Please enter a valid email address'
    }

    if (!formData.password) {
      newErrors.password = 'Password is required'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }))
    }
    if (submitError) {
      setSubmitError('')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitError('')

    if (!validate()) {
      return
    }

    try {
      await login(
        formData.usernameOrEmail.trim(),
        formData.password
      )
      const redirectPath = location.state?.from?.pathname || '/'
      navigate(redirectPath, { replace: true })
    } catch (err) {
      setSubmitError(err.message || 'Login failed. Please check your credentials.')
    }
  }

  const handleFillDemo = (usernameOrEmail, password) => {
    setFormData({ usernameOrEmail, password })
    setErrors({})
    setSubmitError('')
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Welcome Back</h1>
          <p>Sign in to your CivicEye AI account to continue</p>
        </div>

        <ErrorMessage
          message={submitError}
          onDismiss={() => setSubmitError('')}
        />

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <Input
            id="usernameOrEmail"
            name="usernameOrEmail"
            label="Email or Username"
            type="text"
            value={formData.usernameOrEmail}
            onChange={handleChange}
            placeholder="citizen@civiceye.local or username"
            error={errors.usernameOrEmail}
            required
            autoComplete="username"
          />

          <Input
            id="password"
            name="password"
            label="Password"
            type={showPassword ? 'text' : 'password'}
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            error={errors.password}
            required
            autoComplete="current-password"
            endAdornment={
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            }
          />

          <Button
            type="submit"
            variant="primary"
            fullWidth
            loading={loading}
          >
            Sign In
          </Button>
        </form>

        <div className="demo-accounts">
          <div className="demo-accounts-title">
            <span>Quick Fill Test Accounts</span>
          </div>
          <div className="demo-account-buttons">
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleFillDemo('citizen@civiceye.local', 'Password@123')}
            >
              Citizen Demo
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleFillDemo('admin@civiceye.local', 'Password@123')}
            >
              Admin Demo
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleFillDemo('officer@civiceye.local', 'Password@123')}
            >
              Department Demo
            </button>
          </div>
        </div>

        <div className="auth-footer">
          Don't have an account?{' '}
          <Link to="/register">Create an account</Link>
        </div>
      </div>
    </div>
  )
}

export default Login
