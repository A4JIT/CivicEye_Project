import { useState, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import departmentService from '../../services/departmentService'

const Sidebar = () => {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(false)
  const [assignedCount, setAssignedCount] = useState(null)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const role = user?.role || 'CITIZEN'

  useEffect(() => {
    let isMounted = true
    if (role === 'DEPARTMENT_USER') {
      departmentService
        .getDepartmentComplaints({ status: 'ASSIGNED' })
        .then((data) => {
          if (isMounted) {
            const count = data?.totalItems ?? (data?.complaints ? data.complaints.length : null)
            if (count !== null && count !== undefined) {
              setAssignedCount(count)
            }
          }
        })
        .catch(() => {})
    }
    return () => {
      isMounted = false
    }
  }, [role])

  const citizenNav = [
    { label: 'Overview', path: '/dashboard', icon: '📊' },
    { label: 'Report Issue', path: '/complaints/new', icon: '📝' },
    { label: 'My Complaints', path: '/complaints/my', icon: '📋' },
    { label: 'City Alerts', path: '/alerts', icon: '🔔' },
  ]

  const adminNav = [
    { label: 'Command Center', path: '/dashboard', icon: '🏛️' },
    { label: 'All Complaints', path: '/admin/complaints', icon: '📑' },
    { label: 'Departments', path: '/admin/departments', icon: '🏢' },
    { label: 'User Directory', path: '/admin/users', icon: '👥' },
  ]

  const departmentNav = [
    { label: 'Department Queue', path: '/dashboard', icon: '🏢' },
    {
      label: 'Assigned Work',
      path: '/department/complaints',
      icon: '🔧',
      badge: assignedCount != null && assignedCount > 0 ? assignedCount : null,
    },
    { label: 'High Priority', path: '/department/complaints?priority=HIGH', icon: '⚡' },
    { label: 'Resolved Archive', path: '/department/complaints?status=RESOLVED', icon: '✅' },
  ]

  let navItems = citizenNav
  if (role === 'ADMIN') navItems = adminNav
  else if (role === 'DEPARTMENT_USER') navItems = departmentNav

  const getInitials = (name) => {
    if (!name) return 'U'
    return name
      .split(' ')
      .map((part) => part[0])
      .join('')
      .substring(0, 2)
      .toUpperCase()
  }

  return (
    <aside className={`dashboard-sidebar ${collapsed ? 'collapsed' : ''}`} data-testid="dashboard-sidebar">
      <div className="sidebar-header">
        <button
          className="sidebar-toggle"
          onClick={() => setCollapsed(!collapsed)}
          title={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
          aria-label="Toggle Sidebar"
        >
          {collapsed ? '▶' : '◀'}
        </button>
        {!collapsed && (
          <div className="sidebar-brand-title">
            <span className="brand-dot"></span>
            <span>CivicPortal</span>
          </div>
        )}
      </div>

      <div className="sidebar-user-card">
        <div className="user-avatar" title={user?.fullName || user?.username}>
          {getInitials(user?.fullName || user?.username)}
        </div>
        {!collapsed && (
          <div className="user-info">
            <div className="user-name">{user?.fullName || user?.username || 'User'}</div>
            <div className="user-role-badge">{role.replace('_', ' ')}</div>
          </div>
        )}
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path
          return (
            <Link
              key={item.label}
              to={item.path}
              className={`sidebar-link ${isActive ? 'active' : ''}`}
              title={collapsed ? item.label : undefined}
            >
              <span className="sidebar-icon">{item.icon}</span>
              {!collapsed && <span className="sidebar-label">{item.label}</span>}
              {!collapsed && item.badge != null && (
                <span className="sidebar-badge" data-testid="sidebar-badge">{item.badge}</span>
              )}
            </Link>
          )
        })}
      </nav>

      <div className="sidebar-footer">
        <button
          className="sidebar-logout-btn"
          onClick={handleLogout}
          title={collapsed ? 'Log Out' : undefined}
        >
          <span className="sidebar-icon">🚪</span>
          {!collapsed && <span>Sign Out</span>}
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
