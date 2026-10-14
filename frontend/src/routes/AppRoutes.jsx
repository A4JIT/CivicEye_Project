import { Routes, Route } from 'react-router-dom'
import Home from '../pages/public/Home'
import Login from '../pages/public/Login'
import Register from '../pages/public/Register'
import NotFound from '../pages/public/NotFound'
import ProtectedRoute from '../components/common/ProtectedRoute'
import { useAuth } from '../context/AuthContext'
import CitizenDashboard from '../pages/citizen/CitizenDashboard'
import CreateComplaint from '../pages/citizen/CreateComplaint'
import ComplaintTracking from '../pages/citizen/ComplaintTracking'
import AdminDashboard from '../pages/admin/AdminDashboard'
import AllComplaints from '../pages/admin/AllComplaints'
import ReviewComplaint from '../pages/admin/ReviewComplaint'
import DepartmentDashboard from '../pages/department/DepartmentDashboard'
import DepartmentComplaints from '../pages/department/DepartmentComplaints'

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
        path="/admin/complaints"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AllComplaints />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/complaints/:id/review"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <ReviewComplaint />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/complaints/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <ReviewComplaint />
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
      <Route
        path="/department/complaints"
        element={
          <ProtectedRoute allowedRoles={['DEPARTMENT_USER', 'ADMIN']}>
            <DepartmentComplaints />
          </ProtectedRoute>
        }
      />
      <Route
        path="/department/assigned"
        element={
          <ProtectedRoute allowedRoles={['DEPARTMENT_USER', 'ADMIN']}>
            <DepartmentComplaints />
          </ProtectedRoute>
        }
      />

      {/* Complaint Registration Routes */}
      <Route
        path="/complaints/new"
        element={
          <ProtectedRoute allowedRoles={['CITIZEN', 'ADMIN']}>
            <CreateComplaint />
          </ProtectedRoute>
        }
      />
      <Route
        path="/complaints/create"
        element={
          <ProtectedRoute allowedRoles={['CITIZEN', 'ADMIN']}>
            <CreateComplaint />
          </ProtectedRoute>
        }
      />

      {/* Complaint Lifecycle & Tracking Routes */}
      <Route path="/complaints/track" element={<ComplaintTracking />} />
      <Route path="/complaints/track/:complaintNumber" element={<ComplaintTracking />} />
      <Route path="/complaints/:id" element={<ComplaintTracking />} />

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRoutes
