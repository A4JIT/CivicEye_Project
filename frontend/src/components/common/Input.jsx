const Input = ({
  id,
  label,
  name,
  type = 'text',
  value,
  onChange,
  error,
  placeholder,
  required = false,
  disabled = false,
  endAdornment = null,
  autoComplete,
  className = '',
  ...props
}) => {
  const inputId = id || name

  return (
    <div className={`form-group ${className}`}>
      {label && (
        <label htmlFor={inputId} className="form-label">
          <span>
            {label} {required && <span style={{ color: 'var(--error)' }}>*</span>}
          </span>
        </label>
      )}
      <div className="form-input-wrapper">
        <input
          id={inputId}
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          disabled={disabled}
          autoComplete={autoComplete}
          className={`form-input ${error ? 'has-error' : ''}`}
          {...props}
        />
        {endAdornment}
      </div>
      {error && (
        <div className="form-error" role="alert">
          <span>⚠</span> {error}
        </div>
      )}
    </div>
  )
}

export default Input
