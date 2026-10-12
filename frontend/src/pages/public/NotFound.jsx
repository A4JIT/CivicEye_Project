import { Link } from 'react-router-dom'

const NotFound = () => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '5rem 1rem',
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>404</div>
      <h1 style={{ marginBottom: '1rem' }}>Page Not Found</h1>
      <p style={{ maxWidth: '400px', marginBottom: '2rem', color: 'var(--text-muted)' }}>
        The page you are looking for doesn't exist or has been moved.
      </p>
      <Link to="/" className="btn btn-primary">
        Return to Home
      </Link>
    </div>
  )
}

export default NotFound
