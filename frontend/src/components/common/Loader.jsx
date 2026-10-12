const Loader = ({ message = 'Loading...', fullPage = false }) => {
  const containerStyle = fullPage
    ? {
        minHeight: '60vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '1rem',
      }
    : {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '0.75rem',
        padding: '1.5rem',
      }

  return (
    <div style={containerStyle} aria-live="polite">
      <div className="spinner" style={{ width: '2rem', height: '2rem', borderWidth: '3px' }} />
      {message && <p style={{ color: 'var(--text-muted)' }}>{message}</p>}
    </div>
  )
}

export default Loader
