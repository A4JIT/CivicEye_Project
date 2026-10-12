const ErrorMessage = ({ message, type = 'error', onDismiss = null }) => {
  if (!message) return null

  const typeClass = type === 'success' ? 'alert-success' : type === 'warning' ? 'alert-warning' : 'alert-error'
  const icon = type === 'success' ? '✓' : type === 'warning' ? '⚠' : '✖'

  return (
    <div className={`alert ${typeClass}`} role="alert">
      <span style={{ fontWeight: 'bold' }}>{icon}</span>
      <span style={{ flex: 1 }}>{message}</span>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          style={{
            background: 'none',
            border: 'none',
            color: 'inherit',
            cursor: 'pointer',
            fontSize: '1rem',
            marginLeft: '0.5rem',
          }}
          aria-label="Dismiss alert"
        >
          ×
        </button>
      )}
    </div>
  )
}

export default ErrorMessage
