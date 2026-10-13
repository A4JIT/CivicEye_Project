import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import complaintService from '../../services/complaintService'
import Loader from '../../components/common/Loader'
import ErrorMessage from '../../components/common/ErrorMessage'

const MILESTONES = [
  { key: 'SUBMITTED', label: 'Submitted', desc: 'Complaint registered by citizen' },
  { key: 'ASSIGNED', label: 'Assigned', desc: 'Routed to municipal department' },
  { key: 'IN_PROGRESS', label: 'In Progress', desc: 'Maintenance squad deployed' },
  { key: 'RESOLVED', label: 'Resolved', desc: 'Defect repaired & verified' },
]

const getStepIndex = (status) => {
  switch (status) {
    case 'SUBMITTED':
      return 0
    case 'UNDER_REVIEW':
    case 'NEEDS_INFORMATION':
      return 0
    case 'ASSIGNED':
      return 1
    case 'IN_PROGRESS':
      return 2
    case 'RESOLVED':
    case 'CLOSED':
      return 3
    case 'REJECTED':
      return -1
    default:
      return 0
  }
}

const ComplaintTracking = () => {
  const { complaintNumber: routeNumber } = useParams()
  const navigate = useNavigate()

  const [searchInput, setSearchInput] = useState(routeNumber || '')
  const [complaint, setComplaint] = useState(null)
  const [timeline, setTimeline] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [copied, setCopied] = useState(false)

  const fetchComplaintDetails = async (numberToFetch) => {
    if (!numberToFetch || !numberToFetch.trim()) return

    setLoading(true)
    setError(null)
    try {
      const response = await complaintService.getComplaintByNumber(numberToFetch.trim())
      if (response?.success && response?.data) {
        setComplaint(response.data)
        // Fetch timeline using complaint ID
        if (response.data.id) {
          try {
            const timelineRes = await complaintService.getComplaintTimeline(response.data.id)
            if (timelineRes?.success && timelineRes?.data) {
              setTimeline(timelineRes.data)
            }
          } catch (tErr) {
            console.warn('Unable to load timeline:', tErr)
          }
        }
      } else {
        throw new Error(response?.message || 'Complaint not found')
      }
    } catch (err) {
      console.error('Fetch tracking error:', err)
      const msg = err.response?.data?.message || err.message || 'Unable to find complaint with this tracking number.'
      setError(msg)
      setComplaint(null)
      setTimeline([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (routeNumber) {
      setSearchInput(routeNumber)
      fetchComplaintDetails(routeNumber)
    }
  }, [routeNumber])

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    if (!searchInput.trim()) return
    navigate(`/complaints/track/${encodeURIComponent(searchInput.trim())}`)
    fetchComplaintDetails(searchInput.trim())
  }

  const handleCopyNumber = () => {
    if (complaint?.complaintNumber) {
      navigator.clipboard.writeText(complaint.complaintNumber)
      setCopied(true)
      setTimeout(() => setCopied(false), 2500)
    }
  }

  const activeStep = complaint ? getStepIndex(complaint.status) : 0
  const isRejected = complaint?.status === 'REJECTED'

  return (
    <div className="tracking-page-container">
      {/* Top Header & Search Bar */}
      <div className="tracking-header">
        <div className="header-badge">🛰️ Live Lifecycle Tracker</div>
        <h1 className="page-title">Track Civic Defect Status</h1>
        <p className="page-subtitle">
          Real-time municipal progress tracking, department assignment, and before/after verification proof.
        </p>

        <form className="tracking-search-form" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            className="form-input tracking-search-input"
            placeholder="Enter Tracking ID (e.g. CE-2026-0001)"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            data-testid="tracking-search-input"
          />
          <button
            type="submit"
            className="btn btn-primary btn-track"
            data-testid="btn-search-tracking"
            disabled={loading}
          >
            {loading ? 'Searching...' : '🔍 Track Issue'}
          </button>
        </form>

        <div className="quick-tracking-chips">
          <span className="chips-label">Recent demo IDs:</span>
          {['CE-2026-0001', 'CE-2026-0004', 'CE-2026-0006'].map((num) => (
            <button
              key={num}
              type="button"
              className="tracking-chip-btn"
              onClick={() => {
                setSearchInput(num)
                navigate(`/complaints/track/${num}`)
                fetchComplaintDetails(num)
              }}
            >
              {num}
            </button>
          ))}
        </div>
      </div>

      {loading && <Loader message="Retrieving real-time complaint lifecycle..." />}

      {error && (
        <div className="tracking-error-wrapper">
          <ErrorMessage message={error} onDismiss={() => setError(null)} />
        </div>
      )}

      {/* Main Complaint Lifecycle Presentation */}
      {complaint && !loading && (
        <div className="tracking-content" data-testid="tracking-details-card">
          {/* Complaint Header Card */}
          <div className="tracking-summary-card">
            <div className="summary-card-top">
              <div className="complaint-id-row">
                <span className="tracking-number-badge">
                  {complaint.complaintNumber}
                </span>
                <button
                  type="button"
                  className="btn-copy-id"
                  onClick={handleCopyNumber}
                  title="Copy Tracking ID"
                >
                  {copied ? '✓ Copied' : '📋 Copy ID'}
                </button>
              </div>

              <div className="summary-status-group">
                <span className={`priority-badge priority-${complaint.priority?.toLowerCase() || 'medium'}`}>
                  {complaint.priority || 'MEDIUM'}
                </span>
                <span className={`status-badge status-${complaint.status?.toLowerCase().replace(/_/g, '-') || 'submitted'}`}>
                  {complaint.status?.replace(/_/g, ' ') || 'SUBMITTED'}
                </span>
              </div>
            </div>

            <h2 className="tracking-complaint-title">{complaint.title}</h2>
            <p className="tracking-complaint-desc">{complaint.description}</p>

            <div className="tracking-meta-grid">
              <div className="meta-item">
                <span className="meta-label">🏷️ Category:</span>
                <span className="meta-value">{complaint.category?.replace(/_/g, ' ')}</span>
              </div>
              <div className="meta-item">
                <span className="meta-label">📍 Location:</span>
                <span className="meta-value">{complaint.location}</span>
              </div>
              <div className="meta-item">
                <span className="meta-label">🏛️ Assigned Dept:</span>
                <span className="meta-value">{complaint.departmentName || 'Awaiting Routing'}</span>
              </div>
              <div className="meta-item">
                <span className="meta-label">👷 Assigned Officer:</span>
                <span className="meta-value">{complaint.assignedToName || 'Unassigned'}</span>
              </div>
              <div className="meta-item">
                <span className="meta-label">📅 Filed On:</span>
                <span className="meta-value">
                  {complaint.createdAt ? new Date(complaint.createdAt).toLocaleDateString() : 'N/A'}
                </span>
              </div>
              {complaint.resolvedAt && (
                <div className="meta-item">
                  <span className="meta-label">✅ Resolved At:</span>
                  <span className="meta-value">
                    {new Date(complaint.resolvedAt).toLocaleDateString()}
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* Stepper Progress Bar */}
          <div className="tracking-stepper-card">
            <h3 className="card-heading">Resolution Progress</h3>

            {isRejected ? (
              <div className="rejected-banner" data-testid="rejected-banner">
                <div className="banner-icon">🛑</div>
                <div>
                  <strong>Complaint Rejected by Municipal Authority</strong>
                  <p>{complaint.rejectionReason || 'Issue marked outside municipal jurisdiction or invalid report.'}</p>
                </div>
              </div>
            ) : (
              <div className="stepper-wrapper" data-testid="lifecycle-stepper">
                {MILESTONES.map((step, idx) => {
                  const isCompleted = activeStep > idx
                  const isCurrent = activeStep === idx

                  return (
                    <div
                      key={step.key}
                      className={`stepper-step ${isCompleted ? 'completed' : ''} ${isCurrent ? 'current' : ''}`}
                      data-testid={`stepper-step-${step.key}`}
                    >
                      <div className="step-indicator">
                        {isCompleted ? '✓' : idx + 1}
                      </div>
                      <div className="step-info">
                        <div className="step-title">{step.label}</div>
                        <div className="step-subtitle">{step.desc}</div>
                      </div>
                      {idx < MILESTONES.length - 1 && <div className="step-line" />}
                    </div>
                  )
                })}
              </div>
            )}
          </div>

          {/* Evidence Showcase: Defect Photo vs. Resolution Photo */}
          {(complaint.imageUrl || complaint.resolutionImageUrl) && (
            <div className="tracking-evidence-card">
              <h3 className="card-heading">Visual Evidence & Resolution Proof</h3>
              <div className="evidence-grid">
                {complaint.imageUrl && (
                  <div className="evidence-box">
                    <span className="evidence-badge badge-reported">📸 Citizen Defect Evidence</span>
                    <img
                      src={complaint.imageUrl}
                      alt="Reported civic defect"
                      className="evidence-photo"
                    />
                  </div>
                )}
                {complaint.resolutionImageUrl && (
                  <div className="evidence-box">
                    <span className="evidence-badge badge-resolved">✅ Field Resolution Proof</span>
                    <img
                      src={complaint.resolutionImageUrl}
                      alt="Field resolution proof"
                      className="evidence-photo"
                    />
                    {complaint.resolutionNotes && (
                      <p className="resolution-notes-caption">
                        <strong>Officer Note:</strong> {complaint.resolutionNotes}
                      </p>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Chronological Audit Timeline */}
          <div className="tracking-timeline-card" data-testid="timeline-audit-card">
            <h3 className="card-heading">Audit Activity Log</h3>
            {timeline.length === 0 ? (
              <p className="no-timeline-text">No timeline transitions recorded yet.</p>
            ) : (
              <div className="timeline-stream">
                {timeline.map((event, idx) => (
                  <div key={event.id || idx} className="timeline-node" data-testid="timeline-node">
                    <div className="timeline-node-marker" />
                    <div className="timeline-node-body">
                      <div className="timeline-node-header">
                        <span className="node-transition">
                          {event.fromStatus ? `${event.fromStatus} ➔ ` : 'Started as '}
                          <strong>{event.toStatus}</strong>
                        </span>
                        <span className="node-time">
                          {event.createdAt ? new Date(event.createdAt).toLocaleString() : ''}
                        </span>
                      </div>
                      <div className="node-actor">
                        👤 Updated by: <strong>{event.changedByName || 'System'}</strong> ({event.changedByRole || 'SYSTEM'})
                      </div>
                      {event.remarks && <p className="node-remarks">💬 {event.remarks}</p>}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Navigation Actions */}
          <div className="tracking-actions-bar">
            <Link to="/dashboard" className="btn btn-secondary">
              📊 Back to Dashboard
            </Link>
            <Link to="/complaints/new" className="btn btn-primary">
              ➕ Report Another Defect
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}

export default ComplaintTracking
