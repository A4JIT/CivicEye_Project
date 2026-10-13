const STATUS_CONFIG = {
  SUBMITTED: { label: 'Submitted', icon: '📥', className: 'status-submitted' },
  UNDER_REVIEW: { label: 'Under Review', icon: '🔍', className: 'status-under-review' },
  NEEDS_INFORMATION: { label: 'Needs Information', icon: '❓', className: 'status-needs-info' },
  ASSIGNED: { label: 'Assigned', icon: '🏢', className: 'status-assigned' },
  IN_PROGRESS: { label: 'In Progress', icon: '⚡', className: 'status-in-progress' },
  RESOLVED: { label: 'Resolved', icon: '✅', className: 'status-resolved' },
  CLOSED: { label: 'Closed', icon: '🔒', className: 'status-closed' },
  REJECTED: { label: 'Rejected', icon: '❌', className: 'status-rejected' },
}

const ComplaintStatusBadge = ({ status }) => {
  const normalized = status ? status.toUpperCase().replace(/\s+/g, '_') : 'SUBMITTED'
  const config = STATUS_CONFIG[normalized] || {
    label: status || 'Unknown',
    icon: 'ℹ️',
    className: 'status-default',
  }

  return (
    <span
      className={`badge badge-status ${config.className}`}
      data-testid={`status-badge-${normalized.toLowerCase()}`}
    >
      <span className="badge-icon" aria-hidden="true">{config.icon}</span>
      <span className="badge-text">{config.label}</span>
    </span>
  )
}

export default ComplaintStatusBadge
