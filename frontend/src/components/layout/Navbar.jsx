import { Link } from 'react-router-dom'

const Navbar = () => {
  return (
    <header className="navbar" data-testid="navbar">
      <Link to="/" className="navbar-brand" data-testid="navbar-brand">
        <div className="navbar-logo-icon">👁</div>
        <span>CivicEye AI</span>
      </Link>
    </header>
  )
}

export default Navbar
