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
  const [showAdminCode, setShowAdminCode] = useState(false)

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
          {/* Special Admin Code Security Card */}
          <div
            className="admin-security-card"
            style={{
              background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.7) 100%)',
              border: errors.adminCode ? '1px solid #ef4444' : '1px solid rgba(56, 189, 248, 0.35)',
              borderRadius: '12px',
              padding: '16px 18px',
              marginBottom: '18px',
              boxShadow: '0 4px 20px -2px rgba(2, 132, 199, 0.15), inset 0 1px 0 rgba(255, 255, 255, 0.05)',
              position: 'relative',
            }}
          >
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: '10px',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontSize: '1.15rem' }}>🔐</span>
                <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#f8fafc', letterSpacing: '0.3px' }}>
                  Admin Authorization Key
                </span>
              </div>
              <span
                style={{
                  fontSize: '0.68rem',
                  fontWeight: 700,
                  letterSpacing: '0.6px',
                  textTransform: 'uppercase',
                  padding: '3px 8px',
                  borderRadius: '6px',
                  background: 'rgba(56, 189, 248, 0.15)',
                  color: '#38bdf8',
                  border: '1px solid rgba(56, 189, 248, 0.3)',
                }}
              >
                RESTRICTED KEY
              </span>
            </div>

            <Input
              id="adminCode"
              name="adminCode"
              label="Special Admin Authorization Code"
              type={showAdminCode ? 'text' : 'password'}
              value={formData.adminCode}
              onChange={handleChange}
              placeholder="Enter special authorization code"
              error={errors.adminCode}
              required
              autoComplete="off"
              style={{
                fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                letterSpacing: showAdminCode ? '1px' : '2px',
                fontSize: '0.95rem',
                background: 'rgba(15, 23, 42, 0.85)',
                borderColor: errors.adminCode ? '#ef4444' : 'rgba(56, 189, 248, 0.4)',
              }}
              endAdornment={
                <button
                  type="button"
                  className="password-toggle-btn"
                  onClick={() => setShowAdminCode((prev) => !prev)}
                  aria-label={showAdminCode ? 'Hide authorization code' : 'Show authorization code'}
                  title={showAdminCode ? 'Hide code' : 'Show code'}
                  style={{ color: '#38bdf8' }}
                >
                  {showAdminCode ? 'Hide' : 'Show'}
                </button>
              }
            />

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '8px',
                marginTop: '6px',
                paddingTop: '6px',
                borderTop: '1px solid rgba(255, 255, 255, 0.06)',
              }}
            >
              <span style={{ fontSize: '0.78rem', color: '#94a3b8', display: 'flex', alignItems: 'center', gap: '5px' }}>
                <span>🔒</span> Confidential security key required for administrator clearance.
              </span>
              <button
                type="button"
                onClick={() => {
                  setFormData((prev) => ({ ...prev, adminCode: 'CIVICEYE-ADMIN-2026-SECURE' }))
                  if (errors.adminCode) {
                    setErrors((prev) => ({ ...prev, adminCode: '' }))
                  }
                }}
                style={{
                  background: 'rgba(56, 189, 248, 0.1)',
                  border: '1px dashed rgba(56, 189, 248, 0.5)',
                  color: '#38bdf8',
                  borderRadius: '6px',
                  padding: '3px 8px',
                  fontSize: '0.72rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                }}
                title="Auto-fill default authorization code"
              >
                ⚡ Use Default Key
              </button>
            </div>
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
