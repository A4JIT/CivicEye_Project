const Select = ({
  id,
  label,
  name,
  value,
  onChange,
  options = [],
  error,
  required = false,
  disabled = false,
  placeholder,
  className = '',
  ...props
}) => {
  const selectId = id || name

  return (
    <div className={`form-group ${className}`}>
      {label && (
        <label htmlFor={selectId} className="form-label">
          <span>
            {label} {required && <span style={{ color: 'var(--error)' }}>*</span>}
          </span>
        </label>
      )}
      <div className="form-input-wrapper">
        <select
          id={selectId}
          name={name}
          value={value}
          onChange={onChange}
          disabled={disabled}
          required={required}
          className={`form-select ${error ? 'has-error' : ''}`}
          {...props}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>
      {error && (
        <div className="form-error" role="alert">
          <span>⚠</span> {error}
        </div>
      )}
    </div>
  )
}

export default Select
