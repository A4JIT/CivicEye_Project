import { useState, useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import adminService from '../../services/adminService'
import feedbackService from '../../services/feedbackService'
import ComplaintStatusBadge from '../../components/complaints/ComplaintStatusBadge'
import ReviewComplaint from './ReviewComplaint'
import Loader from '../../components/common/Loader'
import Modal from '../../components/common/Modal'

const STATUS_TABS = [
  { id: 'ALL', label: 'All Complaints', icon: '📑' },
  { id: 'SUBMITTED', label: 'Pending Review', icon: '📥' },
  { id: 'UNDER_REVIEW', label: 'Under Review', icon: '🔍' },
  { id: 'NEEDS_INFORMATION', label: 'Needs Info', icon: '❓' },
  { id: 'ASSIGNED', label: 'Assigned', icon: '🏢' },
  { id: 'IN_PROGRESS', label: 'In Progress', icon: '⚡' },
  { id: 'RESOLVED', label: 'Resolved', icon: '✅' },
  { id: 'CLOSED', label: 'Closed', icon: '🔒' },
  { id: 'REJECTED', label: 'Rejected', icon: '❌' },
]

const CATEGORY_FILTER_OPTIONS = [
  { value: 'ALL', label: 'All Categories' },
  { value: 'ROADS_AND_FOOTPATHS', label: 'Roads & Footpaths' },
  { value: 'WATER_AND_SEWAGE', label: 'Water Supply & Sewerage' },
  { value: 'ELECTRICITY', label: 'Electricity & Lighting' },
  { value: 'GARBAGE_AND_CLEANLINESS', label: 'Sanitation & Waste' },
  { value: 'PUBLIC_HEALTH_AND_SAFETY', label: 'Health & Safety' },
  { value: 'OTHER', label: 'Other Grievances' },
]

const PRIORITY_FILTER_OPTIONS = [
  { value: 'ALL', label: 'All Priorities' },
  { value: 'CRITICAL', label: 'Critical / Hazard' },
  { value: 'HIGH', label: 'High Urgency' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low Urgency' },
]

const AllComplaints = () => {
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [departments, setDepartments] = useState([])
  const [feedbackNotice, setFeedbackNotice] = useState('')

  // Filter States
  const [activeStatusTab, setActiveStatusTab] = useState('ALL')
  const [searchQuery, setSearchQuery] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('ALL')
  const [priorityFilter, setPriorityFilter] = useState('ALL')
  const [deptFilter, setDeptFilter] = useState('ALL')

  // Selected Complaint for Drawer Review
  const [selectedComplaint, setSelectedComplaint] = useState(null)

  // Direct Action Modals from table
  const [quickActionModal, setQuickActionModal] = useState({
    type: null, // 'REJECT', 'ASSIGN', 'INFO'
    complaint: null,
  })
  const [quickRejectReason, setQuickRejectReason] = useState('')
  const [quickRejectCategory, setQuickRejectCategory] = useState('INVALID')
  const [quickInfoRemarks, setQuickInfoRemarks] = useState('')
  const [quickAssignDeptId, setQuickAssignDeptId] = useState('')
  const [quickAssignPriority, setQuickAssignPriority] = useState('MEDIUM')
  const [quickCloseRemarks, setQuickCloseRemarks] = useState('')
  const [quickActionLoading, setQuickActionLoading] = useState(false)
  const [modalError, setModalError] = useState('')

  const fetchAllComplaints = async () => {
    setLoading(true)
    try {
      const [deptList, data] = await Promise.all([
        adminService.getDepartments(),
        adminService.getAllComplaints(),
      ])
      setDepartments(deptList || [])
      setComplaints(data?.complaints || [])
    } catch (err) {
      console.error('Failed to load complaints in triage console', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAllComplaints()
  }, [])

  // Live Counts for Tabs
  const statusCounts = useMemo(() => {
    const counts = { ALL: complaints.length }
    STATUS_TABS.forEach((tab) => {
      if (tab.id !== 'ALL') {
        counts[tab.id] = complaints.filter((c) => c.status === tab.id).length
      }
    })
    return counts
  }, [complaints])

  // Filtered complaint list
  const filteredComplaints = useMemo(() => {
    return complaints.filter((item) => {
      // 1. Status Filter
      if (activeStatusTab !== 'ALL' && item.status !== activeStatusTab) {
        return false
      }

      // 2. Category Filter
      if (categoryFilter !== 'ALL' && item.category !== categoryFilter) {
        return false
      }

      // 3. Priority Filter
      if (priorityFilter !== 'ALL' && item.priority !== priorityFilter) {
        return false
      }

      // 4. Department Filter
      if (deptFilter !== 'ALL' && String(item.departmentId) !== String(deptFilter)) {
        return false
      }

      // 5. Search Query
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim()
        const matchNumber = item.complaintNumber?.toLowerCase().includes(q)
        const matchTitle = item.title?.toLowerCase().includes(q)
        const matchLocation = item.location?.toLowerCase().includes(q)
        const matchCitizen = item.citizenName?.toLowerCase().includes(q)
        const matchDesc = item.description?.toLowerCase().includes(q)
        if (!matchNumber && !matchTitle && !matchLocation && !matchCitizen && !matchDesc) {
          return false
        }
      }

      return true
    })
  }, [complaints, activeStatusTab, categoryFilter, priorityFilter, deptFilter, searchQuery])

  const handleComplaintUpdated = (updated) => {
    setComplaints((prev) =>
      prev.map((c) => (c.id === updated.id ? { ...c, ...updated } : c))
    )
    if (selectedComplaint && selectedComplaint.id === updated.id) {
      setSelectedComplaint({ ...selectedComplaint, ...updated })
    }
    setFeedbackNotice(`Complaint ${updated.complaintNumber} updated successfully.`)
    setTimeout(() => setFeedbackNotice(''), 4000)
  }

  // Quick Reject Submit
  const handleQuickReject = async () => {
    if (!quickRejectReason.trim()) {
      setModalError('Rejection reason cannot be blank.')
      return
    }
    setQuickActionLoading(true)
    setModalError('')
    try {
      const res = await adminService.rejectComplaint(quickActionModal.complaint.id, {
        rejectionReason: quickRejectReason.trim(),
        rejectionCategory: quickRejectCategory,
      })
      const updated = res.data || res
      handleComplaintUpdated(updated)
      setQuickActionModal({ type: null, complaint: null })
      setQuickRejectReason('')
    } catch (err) {
      setModalError(err.response?.data?.message || err.message || 'Failed to reject complaint.')
    } finally {
      setQuickActionLoading(false)
    }
  }

  // Quick Info Submit
  const handleQuickInfo = async () => {
    if (!quickInfoRemarks.trim()) {
      setModalError('Information request message cannot be blank.')
      return
    }
    setQuickActionLoading(true)
    setModalError('')
    try {
      const res = await adminService.requestInformation(quickActionModal.complaint.id, {
        remarks: quickInfoRemarks.trim(),
      })
      const updated = res.data || res
      handleComplaintUpdated(updated)
      setQuickActionModal({ type: null, complaint: null })
      setQuickInfoRemarks('')
    } catch (err) {
      setModalError(err.response?.data?.message || err.message || 'Failed to request info.')
    } finally {
      setQuickActionLoading(false)
    }
  }

  // Quick Assign Submit
  const handleQuickAssign = async () => {
    if (!quickAssignDeptId) {
      setModalError('Please select a department.')
      return
    }
    setQuickActionLoading(true)
    setModalError('')
    try {
      const res = await adminService.assignDepartment(quickActionModal.complaint.id, {
        departmentId: Number(quickAssignDeptId),
        priority: quickAssignPriority,
      })
      const updated = res.data || res
      handleComplaintUpdated(updated)
      setQuickActionModal({ type: null, complaint: null })
    } catch (err) {
      setModalError(err.response?.data?.message || err.message || 'Failed to assign department.')
    } finally {
      setQuickActionLoading(false)
    }
  }

  // Quick Close Submit (Phase 8)
  const handleQuickClose = async () => {
    const comp = quickActionModal.complaint
    if (!comp) return
    setQuickActionLoading(true)
    setModalError('')
    try {
      const res = await feedbackService.closeComplaint(comp.id, {
        closureRemarks: quickCloseRemarks.trim() || undefined,
      })
      const updated = res.data || res || {
        ...comp,
        status: 'CLOSED',
        closedAt: new Date().toISOString(),
        closureRemarks: quickCloseRemarks.trim(),
      }
      handleComplaintUpdated(updated)
      setQuickActionModal({ type: null, complaint: null })
      setQuickCloseRemarks('')
      setFeedbackNotice(`Complaint ${comp.complaintNumber} has been officially closed.`)
    } catch (err) {
      setModalError(err.response?.data?.message || err.message || 'Failed to close complaint.')
    } finally {
      setQuickActionLoading(false)
    }
  }

  return (
    <DashboardLayout
      title="Admin Grievance Triage Console"
      subtitle="Verify, triage, request additional information, reject, and dispatch citizen grievances across municipal departments."
    >
      <div className="triage-console-container" data-testid="admin-triage-console">
        {feedbackNotice && (
          <div className="alert alert-success" role="alert" data-testid="triage-feedback-notice">
            <span>✓ {feedbackNotice}</span>
          </div>
        )}

        {/* Status Filter Tab Pills */}
        <div className="triage-filter-tabs" data-testid="status-filter-tabs">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={`triage-tab-btn ${activeStatusTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveStatusTab(tab.id)}
              data-testid={`tab-${tab.id.toLowerCase()}`}
            >
              <span className="tab-icon">{tab.icon}</span>
              <span className="tab-label">{tab.label}</span>
              <span className="tab-counter-badge">{statusCounts[tab.id] || 0}</span>
            </button>
          ))}
        </div>

        {/* Controls & Search Bar */}
        <div className="triage-controls-row">
          <div className="search-input-wrapper">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              className="triage-search-input"
              placeholder="Search by ID, keyword, address, citizen name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              data-testid="triage-search-input"
            />
            {searchQuery && (
              <button
                type="button"
                className="btn-clear-search"
                onClick={() => setSearchQuery('')}
                aria-label="Clear search"
              >
                ✕
              </button>
            )}
          </div>

          <div className="filter-dropdown-group">
            <select
              className="filter-select"
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              data-testid="filter-select-category"
            >
              {CATEGORY_FILTER_OPTIONS.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>

            <select
              className="filter-select"
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
              data-testid="filter-select-priority"
            >
              {PRIORITY_FILTER_OPTIONS.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>

            <select
              className="filter-select"
              value={deptFilter}
              onChange={(e) => setDeptFilter(e.target.value)}
              data-testid="filter-select-dept"
            >
              <option value="ALL">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>

            {(searchQuery || categoryFilter !== 'ALL' || priorityFilter !== 'ALL' || deptFilter !== 'ALL' || activeStatusTab !== 'ALL') && (
              <button
                type="button"
                className="btn-reset-filters"
                onClick={() => {
                  setSearchQuery('')
                  setCategoryFilter('ALL')
                  setPriorityFilter('ALL')
                  setDeptFilter('ALL')
                  setActiveStatusTab('ALL')
                }}
                data-testid="btn-reset-filters"
              >
                Reset Filters
              </button>
            )}
          </div>
        </div>

        {/* Complaints Table & Grid */}
        {loading ? (
          <div className="triage-loading-card">
            <Loader size="lg" message="Loading citywide grievance records..." />
          </div>
        ) : filteredComplaints.length === 0 ? (
          <div className="triage-empty-card" data-testid="triage-empty-state">
            <div className="empty-icon">📭</div>
            <h3>No Complaints Found</h3>
            <p>No complaints match your active filter criteria.</p>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setActiveStatusTab('ALL')
                setSearchQuery('')
                setCategoryFilter('ALL')
                setPriorityFilter('ALL')
                setDeptFilter('ALL')
              }}
            >
              Clear All Filters
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="custom-table triage-table" data-testid="triage-complaints-table">
              <thead>
                <tr>
                  <th>Tracking ID</th>
                  <th>Issue & AI Recommendation</th>
                  <th>Citizen</th>
                  <th>Department</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Triage Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredComplaints.map((item) => (
                  <tr key={item.id} data-testid={`complaint-row-${item.id}`}>
                    <td className="tracking-cell">
                      <button
                        type="button"
                        className="tracking-code-link btn-link"
                        onClick={() => setSelectedComplaint(item)}
                        data-testid={`open-review-${item.id}`}
                      >
                        {item.complaintNumber}
                      </button>
                    </td>

                    <td className="title-cell">
                      <div className="complaint-title-text">{item.title}</div>
                      <div className="complaint-submeta">
                        <span className="category-tag-pill">
                          {item.category?.replace(/_/g, ' ')}
                        </span>
                        {item.aiCategoryOverridden || item.isOverridden || item.aiRecommendation?.isOverridden ? (
                          <span className="ai-override-pill" title="Category / Priority modified by Admin" data-testid="chip-ai-overridden">
                            Overridden
                          </span>
                        ) : (
                          <span
                            className="ai-suggested-pill"
                            title={item.aiRecommendation?.explanation || "AI rule recommendation"}
                            data-testid="chip-ai-suggestion"
                          >
                            ✨ AI: {item.aiRecommendation?.suggestedDepartmentCode || (item.departmentName ? item.departmentName.split(' ')[0] : (item.category?.replace(/_/g, ' ') || 'Civic'))} | {item.aiRecommendation?.suggestedPriority || item.priority} ({Math.round(((item.aiRecommendation?.confidenceScore ?? 0.88) * 100))}%)
                          </span>
                        )}
                        {item.feedbackRating && (
                          <span
                            className="citizen-rating-pill"
                            title={`Citizen Rating: ${item.feedbackRating}/5 stars. ${item.feedbackComments ? `"${item.feedbackComments}"` : ''}`}
                            data-testid={`rating-badge-${item.id}`}
                          >
                            ⭐ {item.feedbackRating}/5
                          </span>
                        )}
                        <span className="complaint-location">📍 {item.location}</span>
                      </div>
                    </td>

                    <td>
                      <div className="citizen-info-cell">
                        <strong>{item.citizenName || 'Citizen'}</strong>
                        <span className="citizen-email">{item.citizenEmail}</span>
                      </div>
                    </td>

                    <td>
                      {item.departmentName ? (
                        <span className="dept-badge">{item.departmentName}</span>
                      ) : (
                        <span className="dept-unassigned">⚠️ Unassigned</span>
                      )}
                    </td>

                    <td>
                      <span className={`badge badge-priority priority-${item.priority?.toLowerCase()}`}>
                        {item.priority}
                      </span>
                    </td>

                    <td>
                      <ComplaintStatusBadge status={item.status} />
                    </td>

                    <td style={{ textAlign: 'right' }}>
                      <div className="action-button-group justify-end">
                        <button
                          type="button"
                          className="btn-action-pill btn-review"
                          title="Open detailed review & AI override panel"
                          onClick={() => setSelectedComplaint(item)}
                          data-testid={`action-review-${item.id}`}
                        >
                          🔍 Review
                        </button>

                        <button
                          type="button"
                          className="btn-action-pill btn-assign"
                          title="Assign to municipal department"
                          onClick={() => {
                            setQuickAssignDeptId(item.departmentId || departments[0]?.id || '')
                            setQuickAssignPriority(item.priority || 'MEDIUM')
                            setModalError('')
                            setQuickActionModal({ type: 'ASSIGN', complaint: item })
                          }}
                          data-testid={`action-assign-${item.id}`}
                        >
                          🏢 Assign
                        </button>

                        <button
                          type="button"
                          className="btn-action-pill btn-info"
                          title="Request additional information from citizen"
                          onClick={() => {
                            setQuickInfoRemarks('')
                            setModalError('')
                            setQuickActionModal({ type: 'INFO', complaint: item })
                          }}
                          data-testid={`action-info-${item.id}`}
                        >
                          ❓ Info
                        </button>

                        <button
                          type="button"
                          className="btn-action-pill btn-reject"
                          title="Reject complaint with explanation"
                          onClick={() => {
                            setQuickRejectReason('')
                            setQuickRejectCategory('INVALID')
                            setModalError('')
                            setQuickActionModal({ type: 'REJECT', complaint: item })
                          }}
                          data-testid={`action-reject-${item.id}`}
                        >
                          ❌ Reject
                        </button>

                        {item.status === 'RESOLVED' && (
                          <button
                            type="button"
                            className="btn-action-pill btn-close-table"
                            title="Close resolved complaint with remarks"
                            onClick={() => {
                              setQuickCloseRemarks('')
                              setModalError('')
                              setQuickActionModal({ type: 'CLOSE', complaint: item })
                            }}
                            data-testid={`action-close-${item.id}`}
                          >
                            🔒 Close
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Slide-in Review Drawer / Modal for Inspection */}
        {selectedComplaint && (
          <div className="triage-drawer-backdrop" onClick={() => setSelectedComplaint(null)}>
            <div className="triage-drawer-container" onClick={(e) => e.stopPropagation()}>
              <ReviewComplaint
                complaintProp={selectedComplaint}
                isDrawer={true}
                onClose={() => setSelectedComplaint(null)}
                onUpdated={(updated) => handleComplaintUpdated(updated)}
              />
            </div>
          </div>
        )}

        {/* Quick Reject Modal */}
        <Modal
          isOpen={quickActionModal.type === 'REJECT'}
          onClose={() => setQuickActionModal({ type: null, complaint: null })}
          title={`Reject Grievance ${quickActionModal.complaint?.complaintNumber}`}
          footer={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setQuickActionModal({ type: null, complaint: null })}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger"
                disabled={quickActionLoading || !quickRejectReason.trim()}
                onClick={handleQuickReject}
                data-testid="btn-confirm-quick-reject"
              >
                {quickActionLoading ? 'Rejecting...' : 'Confirm Rejection'}
              </button>
            </>
          }
        >
          <div className="modal-inner-form">
            {modalError && <div className="alert alert-error">{modalError}</div>}
            <div className="form-group">
              <label className="form-label">Rejection Classification</label>
              <select
                className="form-select"
                value={quickRejectCategory}
                onChange={(e) => setQuickRejectCategory(e.target.value)}
                data-testid="select-quick-reject-category"
              >
                <option value="INVALID">Invalid / Incomplete Information</option>
                <option value="DUPLICATE">Duplicate Grievance</option>
                <option value="OUT_OF_JURISDICTION">Outside Municipal Jurisdiction</option>
                <option value="CIVIL_DISPUTE">Private Dispute</option>
                <option value="ABUSIVE">Abusive Content</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">
                Rejection Justification <span className="text-danger">*</span>
              </label>
              <textarea
                className="form-textarea"
                rows={3}
                placeholder="Enter mandatory rejection reason..."
                value={quickRejectReason}
                onChange={(e) => setQuickRejectReason(e.target.value)}
                data-testid="textarea-quick-reject-reason"
              />
            </div>
          </div>
        </Modal>

        {/* Quick Info Modal */}
        <Modal
          isOpen={quickActionModal.type === 'INFO'}
          onClose={() => setQuickActionModal({ type: null, complaint: null })}
          title={`Request Details for ${quickActionModal.complaint?.complaintNumber}`}
          footer={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setQuickActionModal({ type: null, complaint: null })}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary"
                disabled={quickActionLoading || !quickInfoRemarks.trim()}
                onClick={handleQuickInfo}
                data-testid="btn-confirm-quick-info"
              >
                {quickActionLoading ? 'Sending...' : 'Send Request'}
              </button>
            </>
          }
        >
          <div className="modal-inner-form">
            {modalError && <div className="alert alert-error">{modalError}</div>}
            <div className="form-group">
              <label className="form-label">
                Information Needed <span className="text-danger">*</span>
              </label>
              <textarea
                className="form-textarea"
                rows={3}
                placeholder="Ask citizen for missing photos, landmarks, or details..."
                value={quickInfoRemarks}
                onChange={(e) => setQuickInfoRemarks(e.target.value)}
                data-testid="textarea-quick-info-remarks"
              />
            </div>
          </div>
        </Modal>

        {/* Quick Assign Modal */}
        <Modal
          isOpen={quickActionModal.type === 'ASSIGN'}
          onClose={() => setQuickActionModal({ type: null, complaint: null })}
          title={`Assign Department for ${quickActionModal.complaint?.complaintNumber}`}
          footer={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setQuickActionModal({ type: null, complaint: null })}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-success"
                disabled={quickActionLoading || !quickAssignDeptId}
                onClick={handleQuickAssign}
                data-testid="btn-confirm-quick-assign"
              >
                {quickActionLoading ? 'Assigning...' : 'Dispatch'}
              </button>
            </>
          }
        >
          <div className="modal-inner-form">
            {modalError && <div className="alert alert-error">{modalError}</div>}
            <div className="form-group">
              <label className="form-label">Municipal Department</label>
              <select
                className="form-select"
                value={quickAssignDeptId}
                onChange={(e) => setQuickAssignDeptId(e.target.value)}
                data-testid="select-quick-assign-dept"
              >
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Priority</label>
              <select
                className="form-select"
                value={quickAssignPriority}
                onChange={(e) => setQuickAssignPriority(e.target.value)}
                data-testid="select-quick-assign-priority"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>
        </Modal>

        {/* Quick Close Modal (Phase 8) */}
        <Modal
          isOpen={quickActionModal.type === 'CLOSE'}
          onClose={() => setQuickActionModal({ type: null, complaint: null })}
          title={`Close Grievance ${quickActionModal.complaint?.complaintNumber}`}
          footer={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setQuickActionModal({ type: null, complaint: null })}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-confirm-close"
                disabled={quickActionLoading}
                onClick={handleQuickClose}
                data-testid="btn-confirm-quick-close"
              >
                {quickActionLoading ? 'Closing...' : 'Confirm Close Grievance'}
              </button>
            </>
          }
        >
          <div className="modal-inner-form">
            {modalError && <div className="alert alert-error">{modalError}</div>}
            <p className="modal-instruction-text">
              Close this resolved grievance and verify departmental execution.
            </p>
            <div className="form-group">
              <label className="form-label">Administrative Closure Remarks (Optional)</label>
              <textarea
                className="form-textarea"
                rows={3}
                placeholder="Record administrative closure remarks or verification notes..."
                value={quickCloseRemarks}
                onChange={(e) => setQuickCloseRemarks(e.target.value)}
                data-testid="textarea-quick-close-remarks"
              />
            </div>
          </div>
        </Modal>
      </div>
    </DashboardLayout>
  )
}

export default AllComplaints
