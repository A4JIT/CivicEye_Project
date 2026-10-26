import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const Home = () => {
  const { isAuthenticated, user } = useAuth()

  return (
    <div className="home-container">
      <section className="hero">
        <div className="hero-badge">
          <span>✨</span>
          <span>Next-Gen Civic Tech Platform</span>
        </div>
        <h1 className="hero-title">
          Smarter Cities with <br />
          <span className="hero-gradient">JanNayak AI</span>
        </h1>
        <p className="hero-subtitle">
          Empowering citizens and municipal departments with intelligent complaint
          classification, automated workflow routing, and transparent civic issue resolution.
        </p>

        <div className="hero-cta">
          {isAuthenticated ? (
            <Link to="/dashboard" className="btn btn-primary btn-lg">
              Go to Dashboard ({user?.role}) →
            </Link>
          ) : (
            <>
              <Link to="/register" className="btn btn-primary btn-lg">
                Report an Issue Now
              </Link>
              <Link to="/login" className="btn btn-secondary btn-lg">
                Sign In to Portal
              </Link>
            </>
          )}
        </div>
      </section>

      <section className="features-grid">
        <div className="feature-card">
          <div className="feature-icon">🤖</div>
          <h2 className="feature-title">AI Smart Categorization</h2>
          <p>
            Automatically analyzes issue descriptions and photos to recommend
            accurate civic departments and resolution priorities with 100% transparency.
          </p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">⚡</div>
          <h2 className="feature-title">Automated Department Routing</h2>
          <p>
            Instant dispatch to Sanitation, Roads, Water Works, or Electricity boards
            with zero bureaucratic delays.
          </p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">📊</div>
          <h2 className="feature-title">Real-Time Lifecycle Tracking</h2>
          <p>
            Track your submitted complaints from filing to on-ground resolution with
            status updates and time-stamped audit trails.
          </p>
        </div>
      </section>
    </div>
  )
}

export default Home
