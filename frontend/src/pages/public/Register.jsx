import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Input from '../../components/common/Input'
import Button from '../../components/common/Button'
import ErrorMessage from '../../components/common/ErrorMessage'

const Register = () => {
  const { register, loading } = useAuth()
  const navigate = useNavigate()

  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  })

  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const validate = () => {
    const newErrors = {}

    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Full name is required'
    } else if (formData.fullName.trim().length > 100) {
      newErrors.fullName = 'Full name cannot exceed 100 characters'
    }

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required'
    } else if (formData.username.trim().length < 3) {
      newErrors.username = 'Username must be at least 3 characters'
    } else if (formData.username.trim().length > 50) {
      newErrors.username = 'Username cannot exceed 50 characters'
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      newErrors.email = 'Please enter a valid email address'
    }

    if (!formData.password) {
      newErrors.password = 'Password is required'
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Confirm your password'
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match'
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
    setSuccessMessage('')

    if (!validate()) {
      return
    }

    try {
      const payload = {
        fullName: formData.fullName.trim(),
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
        role: 'CITIZEN',
        departmentId: null,
      }

      await register(payload)
      setSuccessMessage('Registration successful! Redirecting to login...')
      setTimeout(() => {
        navigate('/login')
      }, 1500)
    } catch (err) {
      setSubmitError(err.message || 'Registration failed. Please check the details provided.')
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-top-bar">
          <Link to="/" className="btn-auth-back-home" data-testid="btn-register-home" title="Return to Homepage">
            ← 🏠 Back to Home
          </Link>
        </div>

        <div className="auth-header">
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <span style={{ fontSize: '1.4rem' }}>👥</span>
            <span className="badge badge-info" style={{ fontWeight: 600 }}>Citizen Portal</span>
          </div>
          <h1>Create an Account</h1>
          <p>Join JanNayak AI to report civic issues and improve your city</p>
        </div>

        <ErrorMessage
          message={submitError}
          onDismiss={() => setSubmitError('')}
        />

        {successMessage && (
          <ErrorMessage
            message={successMessage}
            type="success"
          />
        )}

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <Input
            id="fullName"
            name="fullName"
            label="Full Name"
            type="text"
            value={formData.fullName}
            onChange={handleChange}
            placeholder="John Doe"
            error={errors.fullName}
            required
            autoComplete="name"
          />

          <Input
            id="username"
            name="username"
            label="Username"
            type="text"
            value={formData.username}
            onChange={handleChange}
            placeholder="johndoe"
            error={errors.username}
            required
            autoComplete="username"
          />

          <Input
            id="email"
            name="email"
            label="Email Address"
            type="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="john.doe@example.com"
            error={errors.email}
            required
            autoComplete="email"
          />

          <Input
            id="password"
            name="password"
            label="Password (min 6 characters)"
            type={showPassword ? 'text' : 'password'}
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            error={errors.password}
            required
            autoComplete="new-password"
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

          <Input
            id="confirmPassword"
            name="confirmPassword"
            label="Confirm Password"
            type={showPassword ? 'text' : 'password'}
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="••••••••"
            error={errors.confirmPassword}
            required
            autoComplete="new-password"
          />

          <Button
            type="submit"
            variant="primary"
            fullWidth
            loading={loading}
          >
            Create Account
          </Button>
        </form>

        <div className="auth-footer" style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div>
            Already have an account?{' '}
            <Link to="/login">Sign in here</Link>
          </div>
          <div style={{ borderTop: '1px solid rgba(255, 255, 255, 0.1)', paddingTop: '10px', fontSize: '0.85rem' }}>
            <span style={{ color: '#94a3b8' }}>System Administrator? </span>
            <Link to="/admin/register" style={{ color: '#38bdf8', fontWeight: 600 }}>
              Register with Admin Code →
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Register
