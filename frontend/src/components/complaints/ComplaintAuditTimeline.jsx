import { useState, useEffect, useMemo } from 'react'
import historyService from '../../services/historyService'
import Loader from '../common/Loader'
import AuthenticatedImage from '../common/AuthenticatedImage'

const EVENT_TYPE_CONFIG = {
  SUBMITTED: {
    icon: '📝',
    color: '#3b82f6',
    bgColor: 'rgba(59, 130, 246, 0.12)',
    borderColor: 'rgba(59, 130, 246, 0.35)',
    label: 'Complaint Submitted',
  },
  UNDER_REVIEW: {
    icon: '🔍',
    color: '#8b5cf6',
    bgColor: 'rgba(139, 92, 246, 0.12)',
    borderColor: 'rgba(139, 92, 246, 0.35)',
    label: 'Under Administrative Review',
  },
  ASSIGNED: {
    icon: '🏢',
    color: '#06b6d4',
    bgColor: 'rgba(6, 182, 212, 0.12)',
    borderColor: 'rgba(6, 182, 212, 0.35)',
    label: 'Department Assigned',
  },
  REASSIGNED: {
    icon: '🔄',
    color: '#0ea5e9',
    bgColor: 'rgba(14, 165, 233, 0.12)',
    borderColor: 'rgba(14, 165, 233, 0.35)',
    label: 'Department Reassigned',
  },
  IN_PROGRESS: {
    icon: '⚡',
    color: '#f59e0b',
    bgColor: 'rgba(245, 158, 11, 0.12)',
    borderColor: 'rgba(245, 158, 11, 0.35)',
    label: 'Field Squad In Progress',
  },
  NEEDS_INFORMATION: {
    icon: '❓',
    color: '#ec4899',
    bgColor: 'rgba(236, 72, 153, 0.12)',
    borderColor: 'rgba(236, 72, 153, 0.35)',
    label: 'Information Requested',
  },
  RESOLVED: {
    icon: '✅',
    color: '#10b981',
    bgColor: 'rgba(16, 185, 129, 0.12)',
    borderColor: 'rgba(16, 185, 129, 0.35)',
    label: 'Resolution Verified',
  },
  FEEDBACK: {
    icon: '⭐',
    color: '#eab308',
    bgColor: 'rgba(234, 179, 8, 0.12)',
    borderColor: 'rgba(234, 179, 8, 0.35)',
    label: 'Citizen Satisfaction Feedback',
  },
  CLOSED: {
    icon: '🔒',
    color: '#14b8a6',
    bgColor: 'rgba(20, 184, 166, 0.12)',
    borderColor: 'rgba(20, 184, 166, 0.35)',
    label: 'Officially Closed',
  },
  REJECTED: {
    icon: '🛑',
    color: '#ef4444',
    bgColor: 'rgba(239, 68, 68, 0.12)',
    borderColor: 'rgba(239, 68, 68, 0.35)',
    label: 'Complaint Rejected',
  },
  AI_RECOMMENDATION: {
    icon: '🤖',
    color: '#a855f7',
    bgColor: 'rgba(168, 85, 247, 0.12)',
    borderColor: 'rgba(168, 85, 247, 0.35)',
    label: 'AI Recommendation Generated',
  },
  STATUS_CHANGED: {
    icon: '🏷️',
    color: '#6366f1',
    bgColor: 'rgba(99, 102, 241, 0.12)',
    borderColor: 'rgba(99, 102, 241, 0.35)',
    label: 'Status Transition',
  },
}

const ACTOR_ROLE_BADGES = {
  CITIZEN: { label: 'Citizen', className: 'role-citizen', icon: '👤' },
  ADMIN: { label: 'Admin Triage', className: 'role-admin', icon: '🛡️' },
  DEPARTMENT_USER: { label: 'Department Staff', className: 'role-officer', icon: '👷' },
  OFFICER: { label: 'Department Staff', className: 'role-officer', icon: '👷' },
  SYSTEM: { label: 'JanNayak System', className: 'role-system', icon: '⚙️' },
  AI: { label: 'AI Engine', className: 'role-ai', icon: '🤖' },
}

/**
 * Format ISO datetime into readable human format
 */
const formatTimestamp = (isoStr) => {
  if (!isoStr) return 'N/A'
  try {
    const d = new Date(isoStr)
    return d.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    })
  } catch {
    return isoStr
  }
}

/**
 * Interactive Complaint Audit Timeline Component (Phase 9)
 */
const ComplaintAuditTimeline = ({
  complaintId,
  initialEvents = null,
  timelineData = null,
  _readOnly = false,
  className = '',
}) => {
  const hasProvidedEvents = initialEvents !== null && initialEvents !== undefined
  const [events, setEvents] = useState(hasProvidedEvents ? initialEvents : (timelineData?.events || []))
  const [_complaintMeta, setComplaintMeta] = useState(timelineData || null)
  const [loading, setLoading] = useState(!hasProvidedEvents && !timelineData && Boolean(complaintId))
  const [error, setError] = useState(null)
  const [filterType, setFilterType] = useState('ALL') // 'ALL' | 'STATUS' | 'ASSIGNMENT' | 'RESOLUTION'
  const [sortOrder, setSortOrder] = useState('asc') // 'asc' (chronological) | 'desc' (latest first)
  const [previewImage, setPreviewImage] = useState(null)

  // Fetch full timeline if complaintId is provided and events were not pre-loaded
  useEffect(() => {
    let isMounted = true

    if (complaintId && !hasProvidedEvents && !timelineData) {
      setLoading(true)
      setError(null)
      historyService
        .getComplaintHistory(complaintId)
        .then((res) => {
          if (!isMounted) return
          if (res?.success && res?.data) {
            setComplaintMeta(res.data)
            // Normalize events from new response or fallback
            const rawEvents = res.data.events || []
            setEvents(rawEvents)
          }
        })
        .catch((err) => {
          if (!isMounted) return
          console.error('Failed to load audit history for complaint', complaintId, err)
          setError('Unable to load complete audit timeline.')
        })
        .finally(() => {
          if (isMounted) setLoading(false)
        })
    } else if (hasProvidedEvents) {
      setEvents(initialEvents)
    }

    return () => {
      isMounted = false
    }
  }, [complaintId, hasProvidedEvents, initialEvents, timelineData])

  // Normalize event attributes across legacy and Phase 9 formats
  const normalizedEvents = useMemo(() => {
    return events.map((item, index) => {
      const type = item.eventType || item.toStatus || (item.remarks?.includes('Assigned') ? 'ASSIGNED' : 'STATUS_CHANGED')
      const time = item.timestamp || item.createdAt || item.date
      const actorName = item.actorName || item.changedByName || 'Civic Authority'
      const actorRole = item.actorRole || item.changedByRole || 'SYSTEM'
      const title = item.title || item.action || EVENT_TYPE_CONFIG[type]?.label || `Milestone: ${type}`
      const description = item.description || item.remarks || item.notes || ''
      const fromStatus = item.fromStatus || null
      const toStatus = item.toStatus || null
      const evidenceUrl = item.evidenceUrl || item.resolutionImageUrl || item.imageUrl || null

      return {
        id: item.id || `evt-${index}`,
        rawType: type,
        time,
        actorName,
        actorRole,
        title,
        description,
        fromStatus,
        toStatus,
        evidenceUrl,
      }
    })
  }, [events])

  // Filter events based on active sub-category filter
  const filteredEvents = useMemo(() => {
    let list = [...normalizedEvents]

    if (filterType === 'STATUS') {
      list = list.filter((e) =>
        ['SUBMITTED', 'UNDER_REVIEW', 'NEEDS_INFORMATION', 'STATUS_CHANGED', 'REJECTED', 'CLOSED'].includes(e.rawType)
      )
    } else if (filterType === 'ASSIGNMENT') {
      list = list.filter((e) => ['ASSIGNED', 'REASSIGNED'].includes(e.rawType))
    } else if (filterType === 'RESOLUTION') {
      list = list.filter((e) => ['IN_PROGRESS', 'RESOLVED', 'FEEDBACK'].includes(e.rawType))
    }

    if (sortOrder === 'desc') {
      list.reverse()
    }

    return list
  }, [normalizedEvents, filterType, sortOrder])

  return (
    <div
      className={`complaint-audit-timeline-container ${className}`}
      data-testid="timeline-audit-card"
    >
      {/* Timeline Controls & Filter Bar */}
      <div className="audit-timeline-header">
        <div className="audit-header-title-group">
          <span className="audit-icon-sparkle">⏱️</span>
          <div>
            <h3 className="audit-header-title">Audit Trail & Activity Log</h3>
            <span className="audit-header-subtitle">
              Verified chronological record of status transitions, officer handovers, and actions
            </span>
          </div>
        </div>

        <div className="audit-header-actions">
          {/* Quick Filter Tabs */}
          <div className="audit-filter-pills" role="tablist">
            <button
              type="button"
              className={`filter-pill-btn ${filterType === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilterType('ALL')}
            >
              All Events ({normalizedEvents.length})
            </button>
            <button
              type="button"
              className={`filter-pill-btn ${filterType === 'STATUS' ? 'active' : ''}`}
              onClick={() => setFilterType('STATUS')}
            >
              Status Shifts
            </button>
            <button
              type="button"
              className={`filter-pill-btn ${filterType === 'ASSIGNMENT' ? 'active' : ''}`}
              onClick={() => setFilterType('ASSIGNMENT')}
            >
              Assignments
            </button>
            <button
              type="button"
              className={`filter-pill-btn ${filterType === 'RESOLUTION' ? 'active' : ''}`}
              onClick={() => setFilterType('RESOLUTION')}
            >
              Resolution & Proof
            </button>
          </div>

          {/* Sort Toggle */}
          <button
            type="button"
            className="audit-sort-toggle-btn"
            onClick={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
            title={sortOrder === 'asc' ? 'Currently: Oldest to Newest' : 'Currently: Newest First'}
          >
            {sortOrder === 'asc' ? '↑ Chronological' : '↓ Latest First'}
          </button>
        </div>
      </div>

      {loading && (
        <div className="audit-loading-wrapper">
          <Loader text="Loading audit timeline..." />
        </div>
      )}

      {error && !loading && (
        <div className="audit-error-banner">
          <span>⚠️ {error}</span>
        </div>
      )}

      {!loading && filteredEvents.length === 0 && (
        <div className="audit-empty-state">
          <div className="audit-empty-icon">📜</div>
          <h4>No Milestones Recorded</h4>
          <p>There are no recorded status or assignment events matching the selected filter.</p>
        </div>
      )}

      {/* Vertical Interactive Timeline Stream */}
      {!loading && filteredEvents.length > 0 && (
        <div className="audit-timeline-stream">
          {filteredEvents.map((event, idx) => {
            const config = EVENT_TYPE_CONFIG[event.rawType] || EVENT_TYPE_CONFIG.STATUS_CHANGED
            const roleConfig = ACTOR_ROLE_BADGES[event.actorRole] || ACTOR_ROLE_BADGES.SYSTEM

            return (
              <div
                key={event.id || idx}
                className="audit-timeline-node"
                data-testid="timeline-node"
              >
                {/* Node Milestone Connector */}
                <div className="node-connector-col">
                  <div
                    className="node-icon-circle"
                    style={{
                      backgroundColor: config.bgColor,
                      borderColor: config.borderColor,
                      color: config.color,
                    }}
                  >
                    <span>{config.icon}</span>
                  </div>
                  {idx < filteredEvents.length - 1 && <div className="node-vertical-line" />}
                </div>

                {/* Node Card Content */}
                <div
                  className="node-card"
                  style={{
                    borderLeftColor: config.color,
                  }}
                >
                  <div className="node-card-top-row">
                    <div className="node-title-group">
                      <span className="node-event-title">{event.title}</span>
                      {event.fromStatus && event.toStatus && (
                        <span className="node-status-shift-badge">
                          {event.fromStatus} ➔ {event.toStatus}
                        </span>
                      )}
                    </div>

                    <div className="node-meta-group">
                      <span
                        className={`node-actor-badge ${roleConfig.className}`}
                        title={`Actor Role: ${roleConfig.label}`}
                      >
                        {roleConfig.icon} {event.actorName} ({roleConfig.label})
                      </span>
                      <time className="node-timestamp" dateTime={event.time}>
                        {formatTimestamp(event.time)}
                      </time>
                    </div>
                  </div>

                  {/* Contextual Description / Remarks */}
                  {event.description && (
                    <div className="node-description-box">
                      <p className="node-description-text">{event.description}</p>
                    </div>
                  )}

                  {/* Evidence Photo Preview Thumbnail (if any) */}
                  {event.evidenceUrl && (
                    <div className="node-evidence-preview">
                      <span className="evidence-preview-label">Attached Inspection Proof:</span>
                      <AuthenticatedImage
                        src={event.evidenceUrl}
                        alt="Audit Event Attachment"
                        className="node-evidence-thumb"
                        allowZoom={false}
                        onClick={() => setPreviewImage(event.evidenceUrl)}
                      />
                    </div>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}

      {/* Modal for Full-Size Image Preview */}
      {previewImage && (
        <div className="audit-photo-modal-overlay" onClick={() => setPreviewImage(null)}>
          <div className="audit-photo-modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="photo-modal-top">
              <h4>Inspection Evidence Full View</h4>
              <button
                type="button"
                className="btn-modal-close"
                onClick={() => setPreviewImage(null)}
              >
                ✕
              </button>
            </div>
            <AuthenticatedImage
              src={previewImage}
              alt="Enlarged Evidence"
              className="photo-modal-img"
              style={{ maxWidth: '100%', maxHeight: '75vh', objectFit: 'contain' }}
              allowZoom={false}
            />
          </div>
        </div>
      )}
    </div>
  )
}

export default ComplaintAuditTimeline
