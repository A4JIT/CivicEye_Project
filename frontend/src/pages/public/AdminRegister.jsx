import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Input from '../../components/common/Input'
import Button from '../../components/common/Button'
import ErrorMessage from '../../components/common/ErrorMessage'

const AdminRegister = () => {
  const { registerAdmin, loading } = useAuth()
  const navigate = useNavigate()

  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    adminCode: '',
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

    if (!formData.adminCode.trim()) {
      newErrors.adminCode = 'Special Admin Authorization Code is required'
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
        adminCode: formData.adminCode.trim(),
        password: formData.password,
      }

      await registerAdmin(payload)
      setSuccessMessage('Administrator verified and registered! Redirecting to Command Center...')
      setTimeout(() => {
        navigate('/dashboard')
      }, 1500)
    } catch (err) {
      setSubmitError(
        err.message || 'Administrator registration failed. Please ensure your secret code is valid.'
      )
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: '520px', borderTop: '4px solid #ef4444' }}>
        <div className="auth-top-bar">
          <Link to="/" className="btn-auth-back-home" data-testid="btn-admin-register-home" title="Return to Homepage">
            ← 🏠 Back to Home
          </Link>
        </div>

        <div className="auth-header">
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <span style={{ fontSize: '1.4rem' }}>🛡️</span>
            <span
              className="badge"
              style={{
                background: 'rgba(239, 68, 68, 0.15)',
                color: '#f87171',
                border: '1px solid rgba(239, 68, 68, 0.4)',
                fontWeight: 700,
                letterSpacing: '0.5px',
              }}
            >
              RESTRICTED ACCESS
            </span>
          </div>
          <h1>Admin Registration</h1>
          <p>Create a System Administrator account with citywide oversight privileges</p>
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
          {/* Special Admin Code Banner */}
          <div
            style={{
              background: 'rgba(15, 23, 42, 0.75)',
              border: '1px dashed rgba(56, 189, 248, 0.4)',
              borderRadius: '8px',
              padding: '12px 14px',
              marginBottom: '14px',
            }}
          >
            <Input
              id="adminCode"
              name="adminCode"
              label="Special Admin Authorization Code"
              type="password"
              value={formData.adminCode}
              onChange={handleChange}
              placeholder="Enter special authorization code"
              error={errors.adminCode}
              required
              autoComplete="off"
            />
            <span style={{ fontSize: '0.78rem', color: '#94a3b8', display: 'block', marginTop: '-10px' }}>
              🔒 A confidential security code issued by project administrators is required.
            </span>
          </div>

          <Input
            id="fullName"
            name="fullName"
            label="Administrator Full Name"
            type="text"
            value={formData.fullName}
            onChange={handleChange}
            placeholder="Jane Administrator"
            error={errors.fullName}
            required
            autoComplete="name"
          />

          <Input
            id="username"
            name="username"
            label="Admin Username"
            type="text"
            value={formData.username}
            onChange={handleChange}
            placeholder="admin_ops"
            error={errors.username}
            required
            autoComplete="username"
          />

          <Input
            id="email"
            name="email"
            label="Official Municipal Email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="admin@civiceye.com"
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
            style={{
              background: 'linear-gradient(135deg, #0284c7 0%, #2563eb 100%)',
              fontWeight: 600,
            }}
          >
            Authorize & Create Admin Account
          </Button>
        </form>

        <div className="auth-footer" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div>
            Citizen reporting grievances?{' '}
            <Link to="/register">Register as Citizen</Link>
          </div>
          <div>
            Already have an account?{' '}
            <Link to="/login">Sign in here</Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AdminRegister
