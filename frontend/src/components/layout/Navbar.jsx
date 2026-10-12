import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Button from '../common/Button'

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const formatRole = (role) => {
    if (!role) return ''
    return role.replace('_', ' ')
  }

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        <div className="navbar-logo-icon">👁</div>
        <span>CivicEye AI</span>
      </Link>

      <nav className="navbar-links">
        <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
          Home
        </Link>
        {isAuthenticated && (
          <Link
            to="/dashboard"
            className={location.pathname === '/dashboard' ? 'active' : ''}
          >
            Dashboard
          </Link>
        )}
      </nav>

      <div className="navbar-auth">
        {isAuthenticated && user ? (
          <>
            <div className="user-badge">
              <span className="role-tag">{formatRole(user.role)}</span>
              <span>{user.fullName || user.username}</span>
            </div>
            <Button variant="secondary" size="sm" onClick={handleLogout}>
              Log Out
            </Button>
          </>
        ) : (
          <>
            <Link to="/login" className="btn btn-secondary btn-sm">
              Log In
            </Link>
            <Link to="/register" className="btn btn-primary btn-sm">
              Register
            </Link>
          </>
        )}
      </div>
    </header>
  )
}

export default Navbar
