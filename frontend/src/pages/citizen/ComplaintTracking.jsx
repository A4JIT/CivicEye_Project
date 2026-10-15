import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import complaintService from '../../services/complaintService'
import feedbackService from '../../services/feedbackService'
import Loader from '../../components/common/Loader'
import ErrorMessage from '../../components/common/ErrorMessage'

const MILESTONES = [
  { key: 'SUBMITTED', label: 'Submitted', desc: 'Complaint registered by citizen' },
  { key: 'ASSIGNED', label: 'Assigned', desc: 'Routed to municipal department' },
  { key: 'IN_PROGRESS', label: 'In Progress', desc: 'Maintenance squad deployed' },
  { key: 'RESOLVED', label: 'Resolved', desc: 'Defect repaired & verified' },
  { key: 'CLOSED', label: 'Closed', desc: 'Citizen confirmed & officially closed' },
]

const getStepIndex = (status) => {
  switch (status) {
    case 'SUBMITTED':
    case 'UNDER_REVIEW':
    case 'NEEDS_INFORMATION':
      return 0
    case 'ASSIGNED':
      return 1
    case 'IN_PROGRESS':
      return 2
    case 'RESOLVED':
      return 3
    case 'CLOSED':
      return 4
    case 'REJECTED':
      return -1
    default:
      return 0
  }
}

const RATING_LABELS = {
  1: 'Poor - Unsatisfactory',
  2: 'Fair - Needs Improvement',
  3: 'Good - Standard Resolution',
  4: 'Very Good - Thorough Work',
  5: 'Excellent - Highly Satisfied',
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

  // Phase 8: Citizen Feedback & Resolution Closure States
  const [selectedRating, setSelectedRating] = useState(0)
  const [hoverRating, setHoverRating] = useState(0)
  const [feedbackComments, setFeedbackComments] = useState('')
  const [feedbackSubmitted, setFeedbackSubmitted] = useState(false)
  const [feedbackLoading, setFeedbackLoading] = useState(false)
  const [closureLoading, setClosureLoading] = useState(false)
  const [feedbackSuccessMsg, setFeedbackSuccessMsg] = useState('')
  const [closureSuccessMsg, setClosureSuccessMsg] = useState('')

  const fetchComplaintDetails = async (numberToFetch) => {
    if (!numberToFetch || !numberToFetch.trim()) return

    setLoading(true)
    setError(null)
    setFeedbackSuccessMsg('')
    setClosureSuccessMsg('')
    try {
      const response = await complaintService.getComplaintByNumber(numberToFetch.trim())
      if (response?.success && response?.data) {
        const data = response.data
        setComplaint(data)

        if (data.feedbackRating) {
          setSelectedRating(data.feedbackRating)
          setFeedbackComments(data.feedbackComments || '')
          setFeedbackSubmitted(true)
        } else {
          setSelectedRating(0)
          setFeedbackComments('')
          setFeedbackSubmitted(false)
        }

        // Fetch timeline using complaint ID
        if (data.id) {
          try {
            const timelineRes = await complaintService.getComplaintTimeline(data.id)
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

  // Phase 8: Submit 1-5 Star Satisfaction Rating & Comments
  const handleSubmitFeedback = async (e) => {
    if (e) e.preventDefault()
    if (!complaint?.id || selectedRating < 1) return

    setFeedbackLoading(true)
    setError(null)
    try {
      await feedbackService.submitFeedback(complaint.id, {
        rating: selectedRating,
        comments: feedbackComments.trim(),
      })
      setComplaint((prev) => ({
        ...prev,
        feedbackRating: selectedRating,
        feedbackComments: feedbackComments.trim(),
      }))
      setFeedbackSubmitted(true)
      setFeedbackSuccessMsg('Thank you! Your satisfaction rating has been recorded.')
    } catch (err) {
      console.error('Feedback submit error:', err)
      setError(err.response?.data?.message || err.message || 'Failed to submit feedback.')
    } finally {
      setFeedbackLoading(false)
    }
  }

  // Phase 8: 1-Click Confirm Resolution & Close Issue
  const handleConfirmResolution = async () => {
    if (!complaint?.id) return

    setClosureLoading(true)
    setError(null)
    try {
      const payload = {}
      if (selectedRating > 0) payload.rating = selectedRating
      if (feedbackComments.trim()) payload.comments = feedbackComments.trim()

      await feedbackService.confirmResolution(complaint.id, payload)
      const nowIso = new Date().toISOString()

      setComplaint((prev) => ({
        ...prev,
        status: 'CLOSED',
        closedAt: nowIso,
        citizenConfirmedResolution: true,
        feedbackRating: selectedRating || prev.feedbackRating,
        feedbackComments: feedbackComments.trim() || prev.feedbackComments,
      }))

      if (selectedRating > 0) {
        setFeedbackSubmitted(true)
      }

      // Add closure event to timeline view
      setTimeline((prev) => [
        ...prev,
        {
          id: Date.now(),
          fromStatus: 'RESOLVED',
          toStatus: 'CLOSED',
          changedByName: complaint.citizenName || 'Citizen',
          changedByRole: 'CITIZEN',
          remarks: 'Citizen confirmed resolution and officially closed the grievance ticket.',
          createdAt: nowIso,
        },
      ])

      setClosureSuccessMsg('Resolution confirmed! Grievance has been officially closed.')
    } catch (err) {
      console.error('Confirm resolution error:', err)
      setError(err.response?.data?.message || err.message || 'Failed to confirm resolution.')
    } finally {
      setClosureLoading(false)
    }
  }

  const activeStep = complaint ? getStepIndex(complaint.status) : 0
  const isRejected = complaint?.status === 'REJECTED'
  const isResolvedOrClosed = complaint?.status === 'RESOLVED' || complaint?.status === 'CLOSED'

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
              {complaint.closedAt && (
                <div className="meta-item">
                  <span className="meta-label">🔒 Closed On:</span>
                  <span className="meta-value">
                    {new Date(complaint.closedAt).toLocaleDateString()}
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
                  const isCompleted = activeStep > idx || (activeStep === 4 && idx === 4)
                  const isCurrent = activeStep === idx && activeStep !== 4

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

          {/* Dedicated Resolution Evidence & Proof Showcase Box */}
          {(isResolvedOrClosed || complaint.resolutionNotes || complaint.resolutionImageUrl || complaint.imageUrl) && (
            <div className="tracking-evidence-card resolution-proof-showcase-box" data-testid="resolution-evidence-showcase">
              <div className="evidence-header-row">
                <div>
                  <span className="showcase-mini-badge">Phase 8 Verification</span>
                  <h3 className="card-heading">Visual Evidence & Resolution Proof</h3>
                </div>
                {isResolvedOrClosed && (
                  <span className="verified-seal-pill" data-testid="verified-seal">
                    🛡️ Verified Field Resolution
                  </span>
                )}
              </div>

              {/* Resolution Remarks & Metadata Box */}
              {(complaint.resolutionNotes || complaint.resolvedAt || complaint.closedAt) && (
                <div className="resolution-meta-banner" data-testid="resolution-meta-banner">
                  <div className="resolution-meta-icon">📋</div>
                  <div className="resolution-meta-details">
                    <div className="meta-details-top">
                      <strong>Department Resolution Summary</strong>
                      {(complaint.resolvedAt || complaint.updatedAt) && (
                        <span className="resolved-date-tag">
                          Completed: {new Date(complaint.resolvedAt || complaint.updatedAt).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                    <p className="resolution-notes-text">
                      Defect repaired and certified completed by the field maintenance crew.
                    </p>
                    {complaint.closureRemarks && (
                      <p className="closure-remarks-sub">
                        <em>Closure Note:</em> {complaint.closureRemarks}
                      </p>
                    )}
                  </div>
                </div>
              )}

              {/* Before and After Visual Comparison Grid */}
              <div className="evidence-grid">
                {complaint.imageUrl && (
                  <div className="evidence-box before-box">
                    <span className="evidence-badge badge-reported">📸 Citizen Defect Evidence</span>
                    <img
                      src={complaint.imageUrl}
                      alt="Reported civic defect"
                      className="evidence-photo"
                    />
                  </div>
                )}
                {complaint.resolutionImageUrl && (
                  <div className="evidence-box after-box">
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

          {/* Phase 8: Citizen Satisfaction & Feedback Rating Widget */}
          {isResolvedOrClosed && (
            <div className="tracking-feedback-card" data-testid="citizen-feedback-widget">
              <div className="feedback-card-header">
                <span className="feedback-icon-sparkle">⭐</span>
                <div>
                  <h3 className="card-heading">Citizen Satisfaction & Feedback</h3>
                  <p className="feedback-card-subtitle">
                    Rate the resolution quality and service provided by the municipal department.
                  </p>
                </div>
              </div>

              {feedbackSuccessMsg && (
                <div className="alert alert-success" data-testid="feedback-success-alert">
                  ✓ {feedbackSuccessMsg}
                </div>
              )}

              {closureSuccessMsg && (
                <div className="alert alert-success" data-testid="closure-success-alert">
                  🔒 {closureSuccessMsg}
                </div>
              )}

              {/* Locked Verified Review Badge when Feedback is Submitted */}
              {feedbackSubmitted || complaint.feedbackRating ? (
                <div className="verified-review-card" data-testid="verified-feedback-badge">
                  <div className="verified-badge-top">
                    <span className="badge-verified-seal">✓ Verified Citizen Review</span>
                    <span className="verified-rating-stars" data-testid="display-stars">
                      {'★'.repeat(complaint.feedbackRating || selectedRating)}
                      {'☆'.repeat(5 - (complaint.feedbackRating || selectedRating))}
                      <strong className="star-num-label">({complaint.feedbackRating || selectedRating}/5)</strong>
                    </span>
                  </div>
                  <p className="verified-feedback-comment" data-testid="display-feedback-comment">
                    "{complaint.feedbackComments || feedbackComments || 'Resolution completed to citizen satisfaction.'}"
                  </p>
                  <div className="verified-badge-footer">
                    <span>Citizen ID: #{complaint.citizenId || 'Verified Resident'}</span>
                    {complaint.citizenConfirmedResolution && (
                      <span className="resolution-confirmed-tag">✓ Resolution Confirmed & Closed</span>
                    )}
                  </div>
                </div>
              ) : (
                /* Interactive 5-Star Rating & Comments Form */
                <form onSubmit={handleSubmitFeedback} className="feedback-form">
                  <div className="star-rating-selector-group">
                    <label className="feedback-input-label">Select Satisfaction Rating (1 to 5 Stars):</label>
                    <div className="stars-interactive-row" data-testid="star-rating-container">
                      {[1, 2, 3, 4, 5].map((star) => {
                        const isFilled = (hoverRating || selectedRating) >= star
                        return (
                          <button
                            key={star}
                            type="button"
                            className={`star-btn ${isFilled ? 'filled' : 'empty'}`}
                            onClick={() => setSelectedRating(star)}
                            onMouseEnter={() => setHoverRating(star)}
                            onMouseLeave={() => setHoverRating(0)}
                            data-testid={`star-rating-${star}`}
                            aria-label={`Rate ${star} star`}
                          >
                            ★
                          </button>
                        )
                      })}
                      <span className="rating-desc-text" data-testid="rating-selected-label">
                        {RATING_LABELS[hoverRating || selectedRating] || 'Tap a star to rate'}
                      </span>
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="feedback-input-label">
                      Qualitative Feedback & Crew Observations:
                    </label>
                    <textarea
                      className="form-textarea"
                      rows={3}
                      placeholder="Share your thoughts on the repair quality, timeliness, and field worker professionalism..."
                      value={feedbackComments}
                      onChange={(e) => setFeedbackComments(e.target.value)}
                      data-testid="feedback-comments-input"
                    />
                  </div>

                  <div className="feedback-actions-row">
                    <button
                      type="submit"
                      className="btn btn-primary btn-submit-feedback"
                      disabled={feedbackLoading || selectedRating < 1}
                      data-testid="btn-submit-feedback"
                    >
                      {feedbackLoading ? 'Submitting...' : '⭐ Submit Satisfaction Rating'}
                    </button>

                    {complaint.status === 'RESOLVED' && (
                      <button
                        type="button"
                        className="btn btn-success btn-confirm-close"
                        onClick={handleConfirmResolution}
                        disabled={closureLoading}
                        data-testid="btn-confirm-resolution"
                      >
                        {closureLoading ? 'Closing Ticket...' : '✓ Confirm Resolution & Close Issue'}
                      </button>
                    )}
                  </div>
                </form>
              )}

              {/* Standalone 1-Click Closure Action if Feedback already submitted but status still RESOLVED */}
              {complaint.status === 'RESOLVED' && (feedbackSubmitted || complaint.feedbackRating) && (
                <div className="pending-closure-banner">
                  <div className="pending-closure-text">
                    <strong>Resolution Verified:</strong> Your feedback is saved. You can now officially close this grievance.
                  </div>
                  <button
                    type="button"
                    className="btn btn-success btn-sm btn-confirm-close"
                    onClick={handleConfirmResolution}
                    disabled={closureLoading}
                    data-testid="btn-confirm-resolution"
                  >
                    {closureLoading ? 'Closing...' : '✓ Confirm Resolution & Close Issue'}
                  </button>
                </div>
              )}
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
