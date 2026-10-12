import { Routes, Route } from 'react-router-dom'
import Home from '../pages/public/Home'
import Login from '../pages/public/Login'
import Register from '../pages/public/Register'
import NotFound from '../pages/public/NotFound'
import ProtectedRoute from '../components/common/ProtectedRoute'
import { useAuth } from '../context/AuthContext'

const DashboardPlaceholder = () => {
  const { user } = useAuth()
  return (
    <div style={{ padding: '4rem 2rem', textAlign: 'center' }}>
      <h1>{user?.role?.replace('_', ' ')} Dashboard</h1>
      <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>
        Welcome back, <strong>{user?.fullName || user?.username}</strong> ({user?.email})!
      </p>
      <p style={{ marginTop: '0.5rem', color: 'var(--text-dim)' }}>
        Phase 1 Authentication successfully verified.
      </p>
    </div>
  )
}

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPlaceholder />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRoutes
