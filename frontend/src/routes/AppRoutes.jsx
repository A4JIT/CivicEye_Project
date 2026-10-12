import { Routes, Route } from 'react-router-dom'
import Home from '../pages/public/Home'
import Login from '../pages/public/Login'
import Register from '../pages/public/Register'
import NotFound from '../pages/public/NotFound'
import ProtectedRoute from '../components/common/ProtectedRoute'
import { useAuth } from '../context/AuthContext'
import CitizenDashboard from '../pages/citizen/CitizenDashboard'
import AdminDashboard from '../pages/admin/AdminDashboard'
import DepartmentDashboard from '../pages/department/DepartmentDashboard'

/**
 * Dispatches to the appropriate dashboard component based on authenticated user role
 */
const RoleBasedDashboard = () => {
  const { user } = useAuth()
  if (user?.role === 'ADMIN') {
    return <AdminDashboard />
  }
  if (user?.role === 'DEPARTMENT_USER') {
    return <DepartmentDashboard />
  }
  return <CitizenDashboard />
}

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      
      {/* Dynamic Role-Based Dashboard Route */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <RoleBasedDashboard />
          </ProtectedRoute>
        }
      />

      {/* Explicit Role-Restricted Dashboard Routes */}
      <Route
        path="/dashboard/citizen"
        element={
          <ProtectedRoute allowedRoles={['CITIZEN', 'ADMIN']}>
            <CitizenDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/dashboard/admin"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/dashboard/department"
        element={
          <ProtectedRoute allowedRoles={['DEPARTMENT_USER', 'ADMIN']}>
            <DepartmentDashboard />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRoutes
