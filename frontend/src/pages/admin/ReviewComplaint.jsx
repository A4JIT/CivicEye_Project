import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import adminService from '../../services/adminService'
import ComplaintStatusBadge from '../../components/complaints/ComplaintStatusBadge'
import Modal from '../../components/common/Modal'
import Loader from '../../components/common/Loader'
import DashboardLayout from '../../components/layout/DashboardLayout'

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
            {complaint.aiCategoryOverridden && (
              <span className="badge badge-ai-override" title="Category modified by Admin">
                Override: Category
              </span>
            )}
            {complaint.aiPriorityOverridden && (
              <span className="badge badge-ai-override" title="Priority modified by Admin">
                Override: Priority
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
        </div>

        {/* Right Column: AI Suggestions & Admin Triage Actions */}
        <div className="triage-side-panel">
          {/* AI Recommendation Box */}
          <div className="triage-card ai-recommendation-card" data-testid="ai-recommendation-panel">
            <div className="ai-card-header">
              <span className="ai-sparkle-icon">✨</span>
              <h4 className="ai-card-title">AI Recommendation Engine</h4>
              <span className="badge badge-ai-tag">Rule Baseline</span>
            </div>

            <div className="ai-recommendation-item">
              <div className="ai-item-label">Recommended Category</div>
              <div className="ai-item-value">
                <span>{complaint.category?.replace(/_/g, ' ')}</span>
                <span className="ai-confidence-pill">Keyword Match</span>
              </div>
            </div>

            <div className="ai-recommendation-item">
              <div className="ai-item-label">Calculated Severity & Urgency</div>
              <div className="ai-item-value">
                <span className={`priority-pill priority-${complaint.priority?.toLowerCase()}`}>
                  {complaint.priority}
                </span>
                <span className="ai-confidence-pill">Safety Rule</span>
              </div>
            </div>

            <div className="ai-recommendation-item">
              <div className="ai-item-label">Recommended Municipal Department</div>
              <div className="ai-item-value">
                <span>{complaint.departmentName || 'Auto-suggested from category'}</span>
              </div>
            </div>
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
