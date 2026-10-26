import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Sidebar from './Sidebar'

const DashboardLayout = ({ children, title, subtitle }) => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const userInitial = (user?.fullName || user?.username || 'U')[0].toUpperCase()

  return (
    <div className="dashboard-layout" data-testid="dashboard-layout">
      <Sidebar />
      <main className="dashboard-main-area">
        <header className="dashboard-header">
          <div className="dashboard-header-text">
            {title && <h1 className="dashboard-title">{title}</h1>}
            {subtitle && <p className="dashboard-subtitle">{subtitle}</p>}
          </div>

          <div className="dashboard-header-right">
            <div className="dashboard-status-indicator">
              <span className="live-pulse"></span>
              <span className="live-text">Live System Connected</span>
            </div>

            {user && (
              <div className="dashboard-user-actions">
                <div className="dashboard-user-chip" title={user.email || user.username}>
                  <span className="user-chip-avatar">{userInitial}</span>
                  <div className="user-chip-info">
                    <span className="user-chip-name">{user.fullName || user.username}</span>
                    <span className="user-chip-role">{user.role?.replace(/_/g, ' ')}</span>
                  </div>
                </div>

                <button
                  type="button"
                  className="btn btn-outline btn-sm btn-dashboard-logout"
                  onClick={handleLogout}
                  data-testid="btn-dashboard-logout"
                  title="Sign out of your session"
                >
                  <span className="logout-icon">🚪</span>
                  <span>Log Out</span>
                </button>
              </div>
            )}
          </div>
        </header>

        <section className="dashboard-body">{children}</section>
      </main>
    </div>
  )
}

export default DashboardLayout
