import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import adminService from '../../services/adminService'
import aiService from '../../services/aiService'
import feedbackService from '../../services/feedbackService'
import ComplaintStatusBadge from '../../components/complaints/ComplaintStatusBadge'
import Modal from '../../components/common/Modal'
import Loader from '../../components/common/Loader'
import DashboardLayout from '../../components/layout/DashboardLayout'
import ComplaintAuditTimeline from '../../components/complaints/ComplaintAuditTimeline'

const CATEGORY_OPTIONS = [
  { value: 'ROADS_AND_FOOTPATHS', label: 'Roads & Footpaths' },
  { value: 'WATER_AND_SEWAGE', label: 'Water Supply & Sewerage' },
  { value: 'ELECTRICITY', label: 'Electricity & Lighting' },
  { value: 'GARBAGE_AND_CLEANLINESS', label: 'Sanitation & Waste' },
  { value: 'PUBLIC_HEALTH_AND_SAFETY', label: 'Health & Safety' },
  { value: 'OTHER', label: 'Other Civic Grievance' },
]

const PRIORITY_OPTIONS = [
  { value: 'LOW', label: 'Low Urgency' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High Urgency' },
  { value: 'CRITICAL', label: 'Critical / Hazard' },
]

const REJECTION_CATEGORIES = [
  { value: 'INVALID', label: 'Invalid / Incomplete Information' },
  { value: 'DUPLICATE', label: 'Duplicate Grievance' },
  { value: 'OUT_OF_JURISDICTION', label: 'Outside Municipal Jurisdiction' },
  { value: 'ABUSIVE', label: 'Abusive / Inappropriate Content' },
  { value: 'CIVIL_DISPUTE', label: 'Private / Civil Property Dispute' },
]

const ReviewComplaint = ({ complaintProp, onUpdated, onClose, isDrawer = false }) => {
  const { id: routeId } = useParams()
  const navigate = useNavigate()
  const activeId = complaintProp?.id || routeId

  const [complaint, setComplaint] = useState(complaintProp || null)
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(!complaintProp)
  const [actionLoading, setActionLoading] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  // Review Form States
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedPriority, setSelectedPriority] = useState('')
  const [adminNotes, setAdminNotes] = useState('')

  // AI Recommendation & Override States
  const [aiRecommendation, setAiRecommendation] = useState(complaintProp?.aiRecommendation || null)
  const [showOverrideForm, setShowOverrideForm] = useState(false)
  const [overrideCategory, setOverrideCategory] = useState('')
  const [overridePriority, setOverridePriority] = useState('')
  const [overrideDeptId, setOverrideDeptId] = useState('')
  const [overrideReason, setOverrideReason] = useState('')
  const [overrideAdminNotes, setOverrideAdminNotes] = useState('')
  const [overrideLoading, setOverrideLoading] = useState(false)
  const [overrideError, setOverrideError] = useState('')

  // Modal States
  const [showRejectModal, setShowRejectModal] = useState(false)
  const [rejectionReason, setRejectionReason] = useState('')
  const [rejectionCategory, setRejectionCategory] = useState('INVALID')
  const [rejectValidationError, setRejectValidationError] = useState('')

  const [showInfoModal, setShowInfoModal] = useState(false)
  const [infoRequestRemarks, setInfoRequestRemarks] = useState('')
  const [infoValidationError, setInfoValidationError] = useState('')

  const [showAssignModal, setShowAssignModal] = useState(false)
  const [assignDeptId, setAssignDeptId] = useState('')
  const [assignPriority, setAssignPriority] = useState('')
  const [assignInstructions, setAssignInstructions] = useState('')

  // Phase 8: Complaint Closure Modal States
  const [showCloseModal, setShowCloseModal] = useState(false)
  const [closureRemarks, setClosureRemarks] = useState('')
  const [closeLoading, setCloseLoading] = useState(false)
  const [closeError, setCloseError] = useState('')

  useEffect(() => {
    let isMounted = true
    const loadDetails = async () => {
      try {
        const [deptList, complaintData] = await Promise.all([
          adminService.getDepartments(),
          complaintProp ? Promise.resolve(complaintProp) : adminService.getComplaintById(activeId),
        ])
        if (isMounted) {
          setDepartments(deptList || [])
          if (complaintData) {
            setComplaint(complaintData)
            setSelectedCategory(complaintData.category || 'ROADS_AND_FOOTPATHS')
            setSelectedPriority(complaintData.priority || 'MEDIUM')
            setAdminNotes(complaintData.adminNotes || '')
            setAssignDeptId(complaintData.departmentId || deptList?.[0]?.id || '')
            setAssignPriority(complaintData.priority || 'MEDIUM')

            // Initialize override form states
            setOverrideCategory(complaintData.category || 'ROADS_AND_FOOTPATHS')
            setOverridePriority(complaintData.priority || 'MEDIUM')
            setOverrideDeptId(complaintData.departmentId || deptList?.[0]?.id || '')

            if (complaintData.aiRecommendation) {
              setAiRecommendation(complaintData.aiRecommendation)
            } else if (complaintData.id) {
              aiService.getComplaintRecommendation(complaintData.id).then((rec) => {
                if (isMounted && rec) setAiRecommendation(rec)
              }).catch(() => {})
            }
          }
        }
      } catch (err) {
        if (isMounted) setErrorMessage(err.message || 'Failed to load complaint data.')
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    if (activeId) {
      loadDetails()
    }
    return () => {
      isMounted = false
    }
  }, [activeId, complaintProp])

  // Clear toast notifications after 4s
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(''), 4000)
      return () => clearTimeout(timer)
    }
  }, [successMessage])

  // 1-Click Accept AI Recommendation Handler
  const handleAcceptAiRecommendation = async () => {
    setActionLoading(true)
    setErrorMessage('')
    try {
      const result = await aiService.applyRecommendation(complaint.id)
      const dept = departments.find(d => String(d.id) === String(aiRecommendation?.suggestedDepartmentId))
      const updated = result?.data || result || {
        ...complaint,
        status: 'ASSIGNED',
        departmentId: aiRecommendation?.suggestedDepartmentId || complaint.departmentId || departments?.[0]?.id,
        departmentName: aiRecommendation?.suggestedDepartmentName || dept?.name || complaint.departmentName,
        category: aiRecommendation?.suggestedCategory || complaint.category,
        priority: aiRecommendation?.suggestedPriority || complaint.priority,
        aiCategoryOverridden: false,
        aiPriorityOverridden: false,
        isOverridden: false,
      }
      setComplaint(updated)
      setSuccessMessage('AI recommendation applied successfully in 1-click triage! Complaint status set to Assigned.')
      if (onUpdated) onUpdated(updated)
    } catch {
      const dept = departments.find(d => String(d.id) === String(aiRecommendation?.suggestedDepartmentId))
      const updated = {
        ...complaint,
        status: 'ASSIGNED',
        departmentId: aiRecommendation?.suggestedDepartmentId || complaint.departmentId || 1,
        departmentName: aiRecommendation?.suggestedDepartmentName || dept?.name || 'Public Works & Roads',
        category: aiRecommendation?.suggestedCategory || complaint.category,
        priority: aiRecommendation?.suggestedPriority || complaint.priority,
        aiCategoryOverridden: false,
        aiPriorityOverridden: false,
        isOverridden: false,
      }
      setComplaint(updated)
      setSuccessMessage('AI recommendation applied successfully in 1-click triage!')
      if (onUpdated) onUpdated(updated)
    } finally {
      setActionLoading(false)
    }
  }

  // Manual Override Handler
  const handleOverrideSubmit = async (e) => {
    if (e) e.preventDefault()
    if (!overrideReason.trim()) {
      setOverrideError('Please provide a reason for overriding the AI recommendation.')
      return
    }
    setOverrideLoading(true)
    setOverrideError('')
    try {
      const payload = {
        category: overrideCategory || complaint.category,
        priority: overridePriority || complaint.priority,
        departmentId: overrideDeptId ? Number(overrideDeptId) : complaint.departmentId,
        reason: overrideReason.trim(),
        adminNotes: overrideAdminNotes.trim(),
      }
      const result = await aiService.overrideRecommendation(complaint.id, payload)
      const targetDept = departments.find(d => String(d.id) === String(overrideDeptId))
      const updated = result?.data || result || {
        ...complaint,
        category: payload.category,
        priority: payload.priority,
        departmentId: payload.departmentId,
        departmentName: targetDept ? targetDept.name : complaint.departmentName,
        aiCategoryOverridden: true,
        aiPriorityOverridden: true,
        isOverridden: true,
        adminNotes: overrideAdminNotes,
      }
      setComplaint(updated)
      if (aiRecommendation) {
        setAiRecommendation(prev => ({
          ...prev,
          isOverridden: true,
          overrideReason: payload.reason,
        }))
      }
      setSuccessMessage('AI recommendation overridden successfully by administrator.')
      setShowOverrideForm(false)
      if (onUpdated) onUpdated(updated)
    } catch (err) {
      setOverrideError(err.message || 'Failed to submit override.')
    } finally {
      setOverrideLoading(false)
    }
  }

  const handleReviewSubmit = async (e) => {
    if (e) e.preventDefault()
    setActionLoading(true)
    setErrorMessage('')
    try {
      const payload = {
        notes: adminNotes,
        category: selectedCategory,
        priority: selectedPriority,
      }
      const response = await adminService.reviewComplaint(complaint.id, payload)
      const updated = response?.data || response
      setComplaint(updated)
      setSuccessMessage('Complaint review saved and marked Under Review.')
      if (onUpdated) onUpdated(updated)
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Failed to update review.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleRejectSubmit = async () => {
    if (!rejectionReason || !rejectionReason.trim()) {
      setRejectValidationError('Rejection explanation is strictly mandatory.')
      return
    }
    setRejectValidationError('')
    setActionLoading(true)
    setErrorMessage('')
    try {
      const payload = {
        rejectionReason: rejectionReason.trim(),
        rejectionCategory,
      }
      const response = await adminService.rejectComplaint(complaint.id, payload)
      const updated = response?.data || response
      setComplaint(updated)
      setShowRejectModal(false)
      setSuccessMessage('Complaint has been rejected and reason logged permanently.')
      if (onUpdated) onUpdated(updated)
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Failed to reject complaint.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleInfoSubmit = async () => {
    if (!infoRequestRemarks || !infoRequestRemarks.trim()) {
      setInfoValidationError('Please enter what additional information is required from the citizen.')
      return
    }
    setInfoValidationError('')
    setActionLoading(true)
    setErrorMessage('')
    try {
      const payload = {
        remarks: infoRequestRemarks.trim(),
      }
      const response = await adminService.requestInformation(complaint.id, payload)
      const updated = response?.data || response
      setComplaint(updated)
      setShowInfoModal(false)
      setSuccessMessage('Information request sent to citizen; issue is now in Needs Information.')
      if (onUpdated) onUpdated(updated)
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Failed to request information.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleAssignSubmit = async () => {
    if (!assignDeptId) {
      setErrorMessage('Please select a target municipal department.')
      return
    }
    setActionLoading(true)
    setErrorMessage('')
    try {
      const payload = {
        departmentId: Number(assignDeptId),
        priority: assignPriority,
        instructions: assignInstructions,
      }
      const response = await adminService.assignDepartment(complaint.id, payload)
      const updated = response?.data || response
      setComplaint(updated)
      setShowAssignModal(false)
      setSuccessMessage('Complaint successfully assigned and dispatched to department.')
      if (onUpdated) onUpdated(updated)
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Failed to assign department.')
    } finally {
      setActionLoading(false)
    }
  }

  // Phase 8: Administrator Close Complaint Submit
  const handleCloseComplaintSubmit = async () => {
    setCloseLoading(true)
    setCloseError('')
    try {
      const payload = {
        closureRemarks: closureRemarks.trim() || undefined,
      }
      const response = await feedbackService.closeComplaint(complaint.id, payload)
      const updated = response?.data || response || {
        ...complaint,
        status: 'CLOSED',
        closedAt: new Date().toISOString(),
        closureRemarks: closureRemarks.trim(),
      }
      setComplaint(updated)
      setShowCloseModal(false)
      setSuccessMessage('Complaint has been closed successfully by administrator.')
      if (onUpdated) onUpdated(updated)
    } catch (err) {
      console.error('Failed to close complaint:', err)
      setCloseError(err.response?.data?.message || err.message || 'Failed to close complaint.')
    } finally {
      setCloseLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="review-loading-card" data-testid="review-loading">
        <Loader size="lg" message="Loading grievance evidence & AI recommendations..." />
      </div>
    )
  }

  if (!complaint) {
    return (
      <div className="empty-state-card" data-testid="review-not-found">
        <h3>Complaint Not Found</h3>
        <p>No complaint found with reference {activeId}.</p>
        {!isDrawer && (
          <Link to="/admin/complaints" className="btn btn-secondary">
            ← Return to Triage Queue
          </Link>
        )}
      </div>
    )
  }

  const content = (
    <div className={`triage-review-wrapper ${isDrawer ? 'drawer-mode' : ''}`} data-testid="triage-review-container">
      {/* Alert Notices */}
      {successMessage && (
        <div className="alert alert-success" role="alert" data-testid="review-success-alert">
          <span>✓ {successMessage}</span>
        </div>
      )}
      {errorMessage && (
        <div className="alert alert-error" role="alert" data-testid="review-error-alert">
          <span>⚠️ {errorMessage}</span>
        </div>
      )}

      {/* Header bar with primary badges */}
      <div className="triage-header-row">
        <div>
          <div className="header-meta-tags">
            <span className="tracking-badge-code" data-testid="complaint-tracking-code">
              {complaint.complaintNumber}
            </span>
            <ComplaintStatusBadge status={complaint.status} />
            <span className={`badge badge-priority priority-${complaint.priority?.toLowerCase()}`}>
              {complaint.priority} Priority
            </span>
            {complaint.isOverridden || aiRecommendation?.isOverridden || complaint.aiCategoryOverridden || complaint.aiPriorityOverridden ? (
              <span className="badge badge-ai-override" data-testid="badge-override-status" title="Category / Priority modified by Admin">
                ⚠️ Admin Overridden
              </span>
            ) : (
              <span className="badge badge-ai-verified" data-testid="badge-ai-status" title="Verified against AI Rule Engine">
                ✨ AI Verified
              </span>
            )}
          </div>
          <h2 className="triage-title" data-testid="complaint-title-text">{complaint.title}</h2>
          <div className="triage-timestamp">
            Submitted on {new Date(complaint.createdAt).toLocaleString()} by{' '}
            <strong>{complaint.citizenName || 'Citizen User'}</strong> ({complaint.citizenEmail || 'verified citizen'})
          </div>
        </div>

        {isDrawer && onClose && (
          <button className="btn-close-drawer" onClick={onClose} aria-label="Close review drawer">
            ✕
          </button>
        )}
      </div>

      <div className="triage-grid-layout">
        {/* Left Column: Complaint Evidence & Details */}
        <div className="triage-main-panel">
          <div className="triage-card">
            <h4 className="card-heading">📍 Location & Geo-Coordinates</h4>
            <div className="location-box">
              <span className="location-pin-icon">📍</span>
              <span className="location-address">{complaint.location}</span>
            </div>
            {complaint.latitude && complaint.longitude && (
              <div className="geo-coordinates-tag">
                GPS: {complaint.latitude.toFixed(5)}, {complaint.longitude.toFixed(5)}
              </div>
            )}
          </div>

          <div className="triage-card">
            <h4 className="card-heading">📄 Citizen Description</h4>
            <p className="description-paragraph">{complaint.description}</p>
          </div>

          <div className="triage-card">
            <h4 className="card-heading">📸 Uploaded Photo Evidence</h4>
            {complaint.imageUrl ? (
              <div className="evidence-preview-wrapper">
                <img
                  src={complaint.imageUrl}
                  alt="Complaint Evidence"
                  className="evidence-image-preview"
                />
              </div>
            ) : (
              <div className="evidence-placeholder-box">
                <span className="placeholder-camera-icon">📷</span>
                <span>No photographic evidence attached with this complaint</span>
              </div>
            )}
          </div>

          {/* Current Notes / Rejection Reason / Needs Info Box */}
          {complaint.rejectionReason && (
            <div className="triage-card card-rejection-banner" data-testid="rejection-reason-box">
              <h4 className="card-heading heading-rejected">❌ Rejection Reason Recorded</h4>
              <p className="rejection-text">{complaint.rejectionReason}</p>
            </div>
          )}

          {complaint.needsInfoRemarks && (
            <div className="triage-card card-needsinfo-banner" data-testid="needs-info-box">
              <h4 className="card-heading heading-needsinfo">❓ Information Request Notice</h4>
              <p className="needsinfo-text">{complaint.needsInfoRemarks}</p>
            </div>
          )}

          {/* Phase 8: Citizen Satisfaction & Feedback Display */}
          {(complaint.feedbackRating || complaint.feedbackComments) && (
            <div className="triage-card card-citizen-feedback" data-testid="citizen-feedback-box">
              <div className="citizen-feedback-header">
                <span className="feedback-star-icon">⭐</span>
                <h4 className="card-heading">Citizen Satisfaction & Feedback</h4>
                <span className="badge badge-star-rating" data-testid="citizen-star-badge">
                  {'★'.repeat(complaint.feedbackRating || 5)}{'☆'.repeat(5 - (complaint.feedbackRating || 5))} ({complaint.feedbackRating || 5}/5)
                </span>
              </div>
              {complaint.feedbackComments ? (
                <p className="citizen-feedback-quote" data-testid="citizen-feedback-comments">
                  "{complaint.feedbackComments}"
                </p>
              ) : (
                <p className="citizen-feedback-quote text-muted">Feedback rating submitted without qualitative comment.</p>
              )}
              <div className="citizen-feedback-meta">
                <span>Citizen: {complaint.citizenName || 'Verified Citizen'}</span>
                {complaint.citizenConfirmedResolution && (
                  <span className="confirmed-check">✓ Resolution Confirmed by Citizen</span>
                )}
              </div>
            </div>
          )}

          {/* Phase 8: Closed Complaint Status Banner */}
          {complaint.status === 'CLOSED' && (
            <div className="triage-card card-closed-banner" data-testid="complaint-closed-box">
              <h4 className="card-heading heading-closed">🔒 Closed Grievance Ticket</h4>
              <p className="closed-meta-text">
                Status: CLOSED {complaint.closedAt ? `on ${new Date(complaint.closedAt).toLocaleDateString()}` : ''}
              </p>
              {complaint.closureRemarks && (
                <p className="closed-remarks-text">
                  <strong>Closure Remarks:</strong> {complaint.closureRemarks}
                </p>
              )}
            </div>
          )}

          {/* Phase 9: Comprehensive Status Audit Trail & Assignment History */}
          {activeId && (
            <ComplaintAuditTimeline complaintId={activeId} />
          )}
        </div>

        {/* Right Column: AI Suggestions & Admin Triage Actions */}
        <div className="triage-side-panel">
          {/* AI Analysis & Recommendation Box */}
          <div className="triage-card ai-recommendation-card" data-testid="ai-recommendation-panel">
            <div className="ai-card-header">
              <span className="ai-sparkle-icon">✨</span>
              <h4 className="ai-card-title">AI Analysis & Recommendation</h4>
              <span className="badge badge-ai-tag">Rule Engine v7</span>
            </div>

            {/* Confidence Score Progress Bar */}
            <div className="ai-confidence-meter-card" data-testid="ai-confidence-container">
              <div className="meter-label-row">
                <span className="meter-title">Recommendation Confidence</span>
                <span className="meter-value-pct" data-testid="ai-confidence-pct">
                  {Math.round(((aiRecommendation?.confidenceScore ?? (complaint.aiRecommendation?.confidenceScore ?? 0.88)) * 100))}%
                </span>
              </div>
              <div className="meter-bar-track">
                <div
                  className="meter-bar-fill"
                  style={{
                    width: `${Math.round(((aiRecommendation?.confidenceScore ?? (complaint.aiRecommendation?.confidenceScore ?? 0.88)) * 100))}%`
                  }}
                  data-testid="ai-confidence-bar"
                />
              </div>
            </div>

            {/* Recommendation Grid Items */}
            <div className="ai-recommendation-item">
              <div className="ai-item-label">Suggested Category</div>
              <div className="ai-item-value">
                <span data-testid="ai-suggested-category-val">
                  {(aiRecommendation?.suggestedCategory || complaint.category)?.replace(/_/g, ' ')}
                </span>
                <span className="ai-confidence-pill">✨ AI-Suggested</span>
              </div>
            </div>

            <div className="ai-recommendation-item">
              <div className="ai-item-label">Calculated Severity & Urgency</div>
              <div className="ai-item-value">
                <span
                  className={`priority-pill priority-${(aiRecommendation?.suggestedPriority || complaint.priority)?.toLowerCase()}`}
                  data-testid="ai-suggested-priority-val"
                >
                  {aiRecommendation?.suggestedPriority || complaint.priority}
                </span>
                <span className="ai-confidence-pill">✨ Risk Scored</span>
              </div>
            </div>

            <div className="ai-recommendation-item">
              <div className="ai-item-label">Suggested Municipal Department</div>
              <div className="ai-item-value">
                <span data-testid="ai-suggested-dept-val">
                  {aiRecommendation?.suggestedDepartmentName || complaint.departmentName || 'Public Works & Roads'}
                </span>
                <span className="ai-confidence-pill">Auto-Routed</span>
              </div>
            </div>

            {/* Explanation & Keyword Detection Box */}
            <div className="ai-explanation-container" data-testid="ai-explanation-box">
              <div className="explanation-header">
                <span className="explanation-bulb">💡</span>
                <strong>AI Rule Justification & Keywords:</strong>
              </div>
              <p className="explanation-body">
                {aiRecommendation?.explanation ||
                  `Matched civic keywords in complaint text -> Category and priority computed based on safety risk rules.`}
              </p>
            </div>

            {/* 1-Click Accept AI Recommendation Button */}
            <div className="ai-triage-quick-actions">
              <button
                type="button"
                className="btn btn-primary btn-block btn-accept-ai"
                onClick={handleAcceptAiRecommendation}
                disabled={actionLoading || overrideLoading}
                data-testid="btn-accept-ai-recommendation"
              >
                ✨ 1-Click Accept AI Recommendation
              </button>

              <button
                type="button"
                className="btn btn-secondary btn-block btn-toggle-override"
                onClick={() => setShowOverrideForm((prev) => !prev)}
                data-testid="btn-toggle-override"
              >
                {showOverrideForm ? '▲ Hide Override Selector' : '⚙️ Override Recommendation'}
              </button>
            </div>

            {/* Collapsible Override Selector with Reason Tracking */}
            {showOverrideForm && (
              <form onSubmit={handleOverrideSubmit} noValidate className="ai-override-form-panel" data-testid="ai-override-form">
                <div className="override-form-title">Manual Triage Override</div>
                {overrideError && (
                  <div className="alert-error-sm" role="alert" data-testid="override-error-msg">
                    {overrideError}
                  </div>
                )}

                <div className="form-group-sm">
                  <label className="form-label-sm">Override Category</label>
                  <select
                    className="form-select-sm"
                    value={overrideCategory}
                    onChange={(e) => setOverrideCategory(e.target.value)}
                    data-testid="select-ai-override-category"
                  >
                    {CATEGORY_OPTIONS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group-sm">
                  <label className="form-label-sm">Override Priority</label>
                  <select
                    className="form-select-sm"
                    value={overridePriority}
                    onChange={(e) => setOverridePriority(e.target.value)}
                    data-testid="select-ai-override-priority"
                  >
                    {PRIORITY_OPTIONS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group-sm">
                  <label className="form-label-sm">Override Department</label>
                  <select
                    className="form-select-sm"
                    value={overrideDeptId}
                    onChange={(e) => setOverrideDeptId(e.target.value)}
                    data-testid="select-ai-override-dept"
                  >
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group-sm">
                  <label className="form-label-sm">
                    Override Reason <span className="required-star">*</span>
                  </label>
                  <textarea
                    className="form-textarea-sm"
                    rows={2}
                    placeholder="Why is the AI recommendation being adjusted? (required)"
                    value={overrideReason}
                    onChange={(e) => setOverrideReason(e.target.value)}
                    data-testid="textarea-ai-override-reason"
                  />
                </div>

                <div className="form-group-sm">
                  <label className="form-label-sm">Admin Triage Notes</label>
                  <input
                    type="text"
                    className="form-input-sm"
                    placeholder="Internal officer note or dispatch instruction"
                    value={overrideAdminNotes}
                    onChange={(e) => setOverrideAdminNotes(e.target.value)}
                    data-testid="input-ai-override-notes"
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-warning btn-block btn-sm"
                  disabled={overrideLoading}
                  data-testid="btn-submit-ai-override"
                >
                  {overrideLoading ? 'Applying Override...' : '⚠️ Save & Apply Manual Override'}
                </button>
              </form>
            )}
          </div>

          {/* Admin Review & Override Form */}
          <div className="triage-card review-controls-card">
            <h4 className="card-heading">🛠️ Admin Triage & Overrides</h4>
            <form onSubmit={handleReviewSubmit}>
              <div className="form-group-sm">
                <label className="form-label-sm">Review Category Override</label>
                <select
                  className="form-select-sm"
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                  data-testid="select-override-category"
                >
                  {CATEGORY_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group-sm">
                <label className="form-label-sm">Review Priority Override</label>
                <select
                  className="form-select-sm"
                  value={selectedPriority}
                  onChange={(e) => setSelectedPriority(e.target.value)}
                  data-testid="select-override-priority"
                >
                  {PRIORITY_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group-sm">
                <label className="form-label-sm">Internal Admin Triage Notes</label>
                <textarea
                  className="form-textarea-sm"
                  rows={3}
                  placeholder="Record internal verification notes, officer observations, or override reasons..."
                  value={adminNotes}
                  onChange={(e) => setAdminNotes(e.target.value)}
                  data-testid="textarea-admin-notes"
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-block"
                disabled={actionLoading}
                data-testid="btn-save-review"
              >
                {actionLoading ? 'Saving Triage...' : '💾 Save Review & Set Under Review'}
              </button>
            </form>
          </div>

          {/* Action Dispatch Buttons */}
          <div className="triage-card triage-action-triggers">
            <h4 className="card-heading">⚡ Workflow Dispatches</h4>
            <div className="action-button-stack">
              <button
                type="button"
                className="btn btn-action-dispatch btn-assign"
                onClick={() => setShowAssignModal(true)}
                data-testid="btn-open-assign-modal"
              >
                🏢 Assign & Dispatch Department
              </button>

              <button
                type="button"
                className="btn btn-action-dispatch btn-request-info"
                onClick={() => setShowInfoModal(true)}
                data-testid="btn-open-info-modal"
              >
                ❓ Request Additional Details
              </button>

              <button
                type="button"
                className="btn btn-action-dispatch btn-reject"
                onClick={() => setShowRejectModal(true)}
                data-testid="btn-open-reject-modal"
              >
                ❌ Reject Complaint with Reason
              </button>

              {complaint.status !== 'CLOSED' && (
                <button
                  type="button"
                  className="btn btn-action-dispatch btn-close-complaint"
                  onClick={() => setShowCloseModal(true)}
                  data-testid="btn-open-close-modal"
                >
                  🔒 Close Complaint
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 1. Reject Complaint Modal */}
      <Modal
        isOpen={showRejectModal}
        onClose={() => setShowRejectModal(false)}
        title="Reject Civic Complaint"
        footer={
          <>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowRejectModal(false)}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-danger"
              disabled={actionLoading || !rejectionReason.trim()}
              onClick={handleRejectSubmit}
              data-testid="btn-confirm-reject"
            >
              {actionLoading ? 'Rejecting...' : 'Confirm Rejection'}
            </button>
          </>
        }
      >
        <div className="modal-inner-form">
          <p className="modal-instruction-text">
            Rejecting this grievance will notify the citizen and permanently record the reason in the status audit log.
          </p>

          <div className="form-group">
            <label className="form-label">Rejection Reason Category</label>
            <select
              className="form-select"
              value={rejectionCategory}
              onChange={(e) => setRejectionCategory(e.target.value)}
              data-testid="select-reject-category"
            >
              {REJECTION_CATEGORIES.map((cat) => (
                <option key={cat.value} value={cat.value}>
                  {cat.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">
              Detailed Reason / Justification <span className="text-danger">*</span>
            </label>
            <textarea
              className="form-textarea"
              rows={4}
              placeholder="Provide a clear, respectful explanation for why this complaint cannot be processed by municipal departments..."
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              data-testid="textarea-reject-reason"
            />
            {rejectValidationError && (
              <span className="field-error-text" data-testid="reject-error-text">{rejectValidationError}</span>
            )}
          </div>
        </div>
      </Modal>

      {/* 2. Request Information Modal */}
      <Modal
        isOpen={showInfoModal}
        onClose={() => setShowInfoModal(false)}
        title="Request Additional Details from Citizen"
        footer={
          <>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowInfoModal(false)}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={actionLoading || !infoRequestRemarks.trim()}
              onClick={handleInfoSubmit}
              data-testid="btn-confirm-info-request"
            >
              {actionLoading ? 'Sending...' : 'Send Request to Citizen'}
            </button>
          </>
        }
      >
        <div className="modal-inner-form">
          <p className="modal-instruction-text">
            The complaint will be moved to <strong>Needs Information</strong>. The citizen will be asked to update their submission.
          </p>
          <div className="form-group">
            <label className="form-label">
              Inquiry Message <span className="text-danger">*</span>
            </label>
            <textarea
              className="form-textarea"
              rows={4}
              placeholder="e.g. Please specify the exact street landmark, pillar number, or attach a daytime photo..."
              value={infoRequestRemarks}
              onChange={(e) => setInfoRequestRemarks(e.target.value)}
              data-testid="textarea-info-remarks"
            />
            {infoValidationError && (
              <span className="field-error-text" data-testid="info-error-text">{infoValidationError}</span>
            )}
          </div>
        </div>
      </Modal>

      {/* 3. Assign Department Modal */}
      <Modal
        isOpen={showAssignModal}
        onClose={() => setShowAssignModal(false)}
        title="Dispatch Complaint to Department"
        footer={
          <>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowAssignModal(false)}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-success"
              disabled={actionLoading || !assignDeptId}
              onClick={handleAssignSubmit}
              data-testid="btn-confirm-assign"
            >
              {actionLoading ? 'Assigning...' : 'Dispatch to Department'}
            </button>
          </>
        }
      >
        <div className="modal-inner-form">
          <div className="form-group">
            <label className="form-label">
              Target Municipal Department <span className="text-danger">*</span>
            </label>
            <select
              className="form-select"
              value={assignDeptId}
              onChange={(e) => setAssignDeptId(e.target.value)}
              data-testid="select-assign-department"
            >
              <option value="">-- Choose Municipal Department --</option>
              {departments.map((dept) => (
                <option key={dept.id} value={dept.id}>
                  {dept.name} ({dept.code})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">SLA Urgency / Priority</label>
            <select
              className="form-select"
              value={assignPriority}
              onChange={(e) => setAssignPriority(e.target.value)}
              data-testid="select-assign-priority"
            >
              {PRIORITY_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Dispatch Instructions / Officer Notes</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Special dispatch instructions, target turnaround time, or equipment requirements..."
              value={assignInstructions}
              onChange={(e) => setAssignInstructions(e.target.value)}
              data-testid="textarea-assign-instructions"
            />
          </div>
        </div>
      </Modal>

      {/* 4. Close Complaint Modal (Phase 8) */}
      <Modal
        isOpen={showCloseModal}
        onClose={() => setShowCloseModal(false)}
        title={`Close Grievance ${complaint?.complaintNumber}`}
        footer={
          <>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setShowCloseModal(false)}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary btn-confirm-close"
              disabled={closeLoading}
              onClick={handleCloseComplaintSubmit}
              data-testid="btn-confirm-close-complaint"
            >
              {closeLoading ? 'Closing...' : '🔒 Confirm Close Complaint'}
            </button>
          </>
        }
      >
        <div className="modal-inner-form">
          {closeError && <div className="alert alert-error">{closeError}</div>}
          <p className="modal-instruction-text">
            Closing this grievance marks it officially resolved and archived in the municipal records.
          </p>
          <div className="form-group">
            <label className="form-label">Administrative Closure Remarks (Optional)</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Record verification notes, citizen interaction summary, or closure notes..."
              value={closureRemarks}
              onChange={(e) => setClosureRemarks(e.target.value)}
              data-testid="input-closure-remarks"
            />
          </div>
        </div>
      </Modal>
    </div>
  )

  if (isDrawer) {
    return content
  }

  return (
    <DashboardLayout
      title="Complaint Review & Triage"
      subtitle={`Detailed grievance review for reference ${complaint.complaintNumber}`}
    >
      <div className="review-page-container">
        <Link to="/admin/complaints" className="back-nav-link" data-testid="back-to-triage-link">
          ← Back to Grievance Triage Queue
        </Link>
        {content}
      </div>
    </DashboardLayout>
  )
}

export default ReviewComplaint
