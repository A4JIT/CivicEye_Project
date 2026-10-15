import { useState, useEffect, useMemo } from 'react'
import DashboardLayout from '../../components/layout/DashboardLayout'
import departmentService from '../../services/departmentService'
import ComplaintStatusBadge from '../../components/complaints/ComplaintStatusBadge'
import Loader from '../../components/common/Loader'
import Modal from '../../components/common/Modal'
import ComplaintAuditTimeline from '../../components/complaints/ComplaintAuditTimeline'

const STATUS_TABS = [
  { id: 'ALL', label: 'All Assigned', icon: '📋' },
  { id: 'ASSIGNED', label: 'New Tasks (ASSIGNED)', icon: '📥' },
  { id: 'IN_PROGRESS', label: 'In Progress', icon: '⚡' },
  { id: 'RESOLVED', label: 'Resolved', icon: '✅' },
]

const PRIORITY_OPTIONS = [
  { value: 'ALL', label: 'All Priorities' },
  { value: 'CRITICAL', label: 'Critical / Hazard' },
  { value: 'HIGH', label: 'High Urgency' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low Urgency' },
]

const DepartmentComplaints = () => {
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('ALL')
  const [priorityFilter, setPriorityFilter] = useState('ALL')
  const [searchQuery, setSearchQuery] = useState('')
  const [feedbackNotice, setFeedbackNotice] = useState('')
  const [viewMode, setViewMode] = useState('table') // 'table' | 'cards'

  // Detail Drawer / Modal state
  const [detailComplaint, setDetailComplaint] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)

  // Start Work Action State
  const [actionLoadingId, setActionLoadingId] = useState(null)

  // Progress Note Modal State
  const [progressModal, setProgressModal] = useState({ open: false, complaint: null })
  const [progressNoteText, setProgressNoteText] = useState('')
  const [progressSubmitting, setProgressSubmitting] = useState(false)
  const [progressError, setProgressError] = useState('')

  // Resolve Modal State
  const [resolveModal, setResolveModal] = useState({ open: false, complaint: null })
  const [resolutionNotes, setResolutionNotes] = useState('')
  const [resolutionPhoto, setResolutionPhoto] = useState(null)
  const [resolutionPhotoPreview, setResolutionPhotoPreview] = useState(null)
  const [resolveSubmitting, setResolveSubmitting] = useState(false)
  const [resolveError, setResolveError] = useState('')

  const fetchDepartmentTasks = async () => {
    setLoading(true)
    try {
      const data = await departmentService.getDepartmentComplaints()
      setComplaints(data?.complaints || [])
    } catch (err) {
      console.error('Failed to load department tasks', err)
      setFeedbackNotice('Failed to load department tasks. Using local view.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDepartmentTasks()
  }, [])

  // Live Tab Counters
  const tabCounts = useMemo(() => {
    return {
      ALL: complaints.length,
      ASSIGNED: complaints.filter((c) => c.status === 'ASSIGNED').length,
      IN_PROGRESS: complaints.filter((c) => c.status === 'IN_PROGRESS').length,
      RESOLVED: complaints.filter((c) => c.status === 'RESOLVED').length,
    }
  }, [complaints])

  // Filtered List
  const filteredComplaints = useMemo(() => {
    return complaints.filter((item) => {
      // 1. Status Filter
      if (activeTab !== 'ALL' && item.status !== activeTab) {
        return false
      }

      // 2. Priority Filter
      if (priorityFilter !== 'ALL' && item.priority !== priorityFilter) {
        return false
      }

      // 3. Search Query
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim()
        const matchNumber = item.complaintNumber?.toLowerCase().includes(q)
        const matchTitle = item.title?.toLowerCase().includes(q)
        const matchDesc = item.description?.toLowerCase().includes(q)
        const matchLoc = item.location?.toLowerCase().includes(q)
        const matchCitizen = item.citizenName?.toLowerCase().includes(q)
        if (!matchNumber && !matchTitle && !matchDesc && !matchLoc && !matchCitizen) {
          return false
        }
      }

      return true
    })
  }, [complaints, activeTab, priorityFilter, searchQuery])

  // Quick Notification Helper
  const showNotice = (msg) => {
    setFeedbackNotice(msg)
    setTimeout(() => setFeedbackNotice(''), 4500)
  }

  const handleComplaintUpdated = (updated) => {
    setComplaints((prev) =>
      prev.map((c) => (c.id === updated.id ? { ...c, ...updated } : c))
    )
    if (detailComplaint && detailComplaint.id === updated.id) {
      setDetailComplaint((prev) => ({ ...prev, ...updated }))
    }
  }

  // Open Detail Drawer
  const handleOpenDetail = async (complaint) => {
    setDetailComplaint(complaint)
    setDetailLoading(true)
    try {
      const detailed = await departmentService.getDepartmentComplaintById(complaint.id)
      if (detailed) {
        setDetailComplaint(detailed)
      }
    } catch (err) {
      console.warn('Could not fetch full complaint timeline, displaying basic details', err)
    } finally {
      setDetailLoading(false)
    }
  }

  // 1-Click "Start Work"
  const handleStartWork = async (complaint) => {
    setActionLoadingId(complaint.id)
    try {
      const res = await departmentService.startWork(complaint.id, {
        remarks: 'Field maintenance crew mobilized to site with repair equipment',
      })
      const updated = res.data || res
      handleComplaintUpdated(updated)
      showNotice(`Work commenced on ${complaint.complaintNumber}. Status is now IN_PROGRESS.`)
    } catch (err) {
      showNotice(err.response?.data?.message || err.message || 'Failed to start work.')
    } finally {
      setActionLoadingId(null)
    }
  }

  // Open Progress Modal
  const handleOpenProgressModal = (complaint) => {
    setProgressModal({ open: true, complaint })
    setProgressNoteText(complaint.progressNotes || '')
    setProgressError('')
  }

  // Submit Progress Note
  const handleSubmitProgress = async (e) => {
    e.preventDefault()
    if (!progressNoteText.trim()) {
      setProgressError('Progress note cannot be blank.')
      return
    }

    setProgressSubmitting(true)
    setProgressError('')
    try {
      const res = await departmentService.addProgressNote(progressModal.complaint.id, {
        notes: progressNoteText.trim(),
      })
      const updated = res.data || res
      handleComplaintUpdated(updated)
      setProgressModal({ open: false, complaint: null })
      setProgressNoteText('')
      showNotice(`Progress note recorded for ${progressModal.complaint.complaintNumber}.`)
    } catch (err) {
      setProgressError(err.response?.data?.message || err.message || 'Failed to save progress note.')
    } finally {
      setProgressSubmitting(false)
    }
  }

  // Open Resolve Modal
  const handleOpenResolveModal = (complaint) => {
    setResolveModal({ open: true, complaint })
    setResolutionNotes(complaint.resolutionNotes || '')
    setResolutionPhoto(null)
    setResolutionPhotoPreview(null)
    setResolveError('')
  }

  // Handle Resolution Photo Selection
  const handlePhotoChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      setResolutionPhoto(file)
      const previewUrl = URL.createObjectURL(file)
      setResolutionPhotoPreview(previewUrl)
    }
  }

  // Remove Photo Preview
  const handleRemovePhoto = () => {
    setResolutionPhoto(null)
    if (resolutionPhotoPreview) {
      URL.revokeObjectURL(resolutionPhotoPreview)
      setResolutionPhotoPreview(null)
    }
  }

  // Submit Resolution
  const handleSubmitResolve = async (e) => {
    e.preventDefault()
    if (!resolutionNotes.trim()) {
      setResolveError('Resolution notes are mandatory and cannot be empty.')
      return
    }

    setResolveSubmitting(true)
    setResolveError('')
    try {
      let payload
      if (resolutionPhoto) {
        payload = new FormData()
        payload.append('resolutionNotes', resolutionNotes.trim())
        payload.append('image', resolutionPhoto)
      } else {
        payload = {
          resolutionNotes: resolutionNotes.trim(),
        }
      }

      const res = await departmentService.resolveComplaint(resolveModal.complaint.id, payload)
      const updated = res.data || res
      handleComplaintUpdated(updated)
      setResolveModal({ open: false, complaint: null })
      setResolutionNotes('')
      handleRemovePhoto()
      showNotice(`Complaint ${resolveModal.complaint.complaintNumber} marked as RESOLVED with evidence.`)
    } catch (err) {
      setResolveError(err.response?.data?.message || err.message || 'Failed to resolve complaint.')
    } finally {
      setResolveSubmitting(false)
    }
  }

  return (
    <DashboardLayout>
      <div className="dept-console-container" data-testid="department-console-page">
        {/* Header Section */}
        <header className="dept-console-header">
          <div className="dept-header-info">
            <div className="dept-header-badge">
              <span className="badge-pulse-dot"></span>
              <span>Municipal Operations Console</span>
            </div>
            <h1 className="dept-console-title">Department Work Console</h1>
            <p className="dept-console-subtitle">
              Manage field tasks, acknowledge assignments, record real-time progress updates, and resolve complaints with verified photographic evidence.
            </p>
          </div>

          {/* Quick Metrics Bar */}
          <div className="dept-metrics-pills" data-testid="dept-kpi-summary">
            <div className="metric-pill">
              <span className="metric-pill-label">Total Assigned</span>
              <span className="metric-pill-value" data-testid="metric-total">{tabCounts.ALL}</span>
            </div>
            <div className="metric-pill highlight-blue">
              <span className="metric-pill-label">New Tasks</span>
              <span className="metric-pill-value" data-testid="metric-assigned">{tabCounts.ASSIGNED}</span>
            </div>
            <div className="metric-pill highlight-yellow">
              <span className="metric-pill-label">In Progress</span>
              <span className="metric-pill-value" data-testid="metric-in-progress">{tabCounts.IN_PROGRESS}</span>
            </div>
            <div className="metric-pill highlight-green">
              <span className="metric-pill-label">Resolved</span>
              <span className="metric-pill-value" data-testid="metric-resolved">{tabCounts.RESOLVED}</span>
            </div>
          </div>
        </header>

        {/* Global Feedback Alert */}
        {feedbackNotice && (
          <div className="alert-notice alert-notice-success" role="alert" data-testid="feedback-notice">
            <span className="alert-notice-icon">✨</span>
            <span>{feedbackNotice}</span>
            <button
              type="button"
              className="alert-notice-close"
              onClick={() => setFeedbackNotice('')}
              aria-label="Dismiss alert"
            >
              ✕
            </button>
          </div>
        )}

        {/* Live Filter Tabs */}
        <div className="dept-filter-tabs" data-testid="department-filter-tabs">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={`dept-tab-btn ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
              data-testid={`dept-tab-${tab.id.toLowerCase()}`}
            >
              <span className="tab-icon">{tab.icon}</span>
              <span className="tab-label">{tab.label}</span>
              <span className="tab-counter-badge">{tabCounts[tab.id] || 0}</span>
            </button>
          ))}
        </div>

        {/* Search & Priority Controls */}
        <div className="dept-controls-row">
          <div className="search-input-wrapper">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              className="dept-search-input"
              placeholder="Search by ID, keywords, citizen name, address..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              data-testid="dept-search-input"
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

          <div className="dept-filters-right">
            <select
              className="filter-select"
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
              data-testid="filter-select-priority"
            >
              {PRIORITY_OPTIONS.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>

            <div className="view-toggle-group">
              <button
                type="button"
                className={`btn-view-toggle ${viewMode === 'table' ? 'active' : ''}`}
                onClick={() => setViewMode('table')}
                title="Table View"
                data-testid="view-toggle-table"
              >
                📑 Table
              </button>
              <button
                type="button"
                className={`btn-view-toggle ${viewMode === 'cards' ? 'active' : ''}`}
                onClick={() => setViewMode('cards')}
                title="Card Grid View"
                data-testid="view-toggle-cards"
              >
                🗂️ Cards
              </button>
            </div>

            {(searchQuery || priorityFilter !== 'ALL' || activeTab !== 'ALL') && (
              <button
                type="button"
                className="btn-reset-filters"
                onClick={() => {
                  setSearchQuery('')
                  setPriorityFilter('ALL')
                  setActiveTab('ALL')
                }}
                data-testid="btn-reset-dept-filters"
              >
                Reset Filters
              </button>
            )}
          </div>
        </div>

        {/* Complaints Listing Content */}
        {loading ? (
          <div className="dept-loading-card">
            <Loader size="lg" message="Loading department work queue..." />
          </div>
        ) : filteredComplaints.length === 0 ? (
          <div className="dept-empty-card" data-testid="dept-empty-state">
            <div className="empty-icon">📂</div>
            <h3>No Tasks Found</h3>
            <p>No complaints match your active filter criteria.</p>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setActiveTab('ALL')
                setPriorityFilter('ALL')
                setSearchQuery('')
              }}
            >
              Clear Filters
            </button>
          </div>
        ) : viewMode === 'table' ? (
          /* Table View */
          <div className="dept-table-wrapper" data-testid="dept-tasks-table">
            <table className="dept-table">
              <thead>
                <tr>
                  <th>Task & Title</th>
                  <th>Priority</th>
                  <th>Location & Details</th>
                  <th>Status</th>
                  <th>Progress & Notes</th>
                  <th style={{ textAlign: 'right' }}>Workflow Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredComplaints.map((item) => {
                  const isActionLoading = actionLoadingId === item.id
                  return (
                    <tr key={item.id} data-testid={`complaint-row-${item.id}`}>
                      <td>
                        <div className="task-id-badge">{item.complaintNumber}</div>
                        <div className="task-title-text" title={item.title}>
                          {item.title}
                        </div>
                        <div className="task-meta-sub">
                          <span>Reported by: {item.citizenName || 'Citizen'}</span>
                        </div>
                      </td>
                      <td>
                        <span className={`priority-chip priority-${(item.priority || 'medium').toLowerCase()}`}>
                          {item.priority || 'MEDIUM'}
                        </span>
                      </td>
                      <td>
                        <div className="task-location-text">📍 {item.location || 'Municipal Area'}</div>
                        <div className="task-category-pill">{item.category?.replace(/_/g, ' ') || 'General'}</div>
                      </td>
                      <td>
                        <ComplaintStatusBadge status={item.status} />
                      </td>
                      <td>
                        <div className="task-progress-snippet">
                          {item.progressNotes ? (
                            <span className="progress-note-preview" title={item.progressNotes}>
                              💬 {item.progressNotes}
                            </span>
                          ) : item.resolutionNotes ? (
                            <span className="resolution-note-preview" title={item.resolutionNotes}>
                              ✅ {item.resolutionNotes}
                            </span>
                          ) : (
                            <span className="text-dim">No updates recorded yet</span>
                          )}
                        </div>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div className="dept-actions-cluster">
                          {/* 1-Click Start Work Button */}
                          {item.status === 'ASSIGNED' && (
                            <button
                              type="button"
                              className="btn-dept-action btn-start-work"
                              onClick={() => handleStartWork(item)}
                              disabled={isActionLoading}
                              data-testid={`btn-start-work-${item.id}`}
                              title="Advance to IN_PROGRESS"
                            >
                              {isActionLoading ? 'Starting...' : '▶ Start Work'}
                            </button>
                          )}

                          {/* Add Progress Note Button */}
                          {item.status === 'IN_PROGRESS' && (
                            <button
                              type="button"
                              className="btn-dept-action btn-progress-note"
                              onClick={() => handleOpenProgressModal(item)}
                              data-testid={`btn-add-progress-${item.id}`}
                              title="Log Field Progress Note"
                            >
                              💬 Add Note
                            </button>
                          )}

                          {/* Resolve Issue Button */}
                          {item.status === 'IN_PROGRESS' && (
                            <button
                              type="button"
                              className="btn-dept-action btn-resolve-task"
                              onClick={() => handleOpenResolveModal(item)}
                              data-testid={`btn-resolve-issue-${item.id}`}
                              title="Resolve and Upload Proof Photo"
                            >
                              ✅ Resolve
                            </button>
                          )}

                          {/* View Detail Drawer Button */}
                          <button
                            type="button"
                            className="btn-dept-action btn-view-details"
                            onClick={() => handleOpenDetail(item)}
                            data-testid={`btn-view-detail-${item.id}`}
                            title="Inspect Details and Stepper"
                          >
                            👁️ Details
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : (
          /* Cards View */
          <div className="dept-card-grid" data-testid="dept-tasks-grid">
            {filteredComplaints.map((item) => {
              const isActionLoading = actionLoadingId === item.id
              return (
                <div key={item.id} className="dept-task-card" data-testid={`complaint-card-${item.id}`}>
                  <div className="dept-card-top">
                    <span className="task-id-badge">{item.complaintNumber}</span>
                    <span className={`priority-chip priority-${(item.priority || 'medium').toLowerCase()}`}>
                      {item.priority || 'MEDIUM'}
                    </span>
                  </div>

                  <h3 className="dept-card-title">{item.title}</h3>
                  <p className="dept-card-desc">{item.description}</p>

                  <div className="dept-card-meta">
                    <div>📍 {item.location}</div>
                    <div>👤 {item.citizenName || 'Citizen'}</div>
                    <div style={{ marginTop: '0.5rem' }}>
                      <ComplaintStatusBadge status={item.status} />
                    </div>
                  </div>

                  {item.imageUrl && (
                    <div className="dept-card-thumbnail">
                      <img src={item.imageUrl} alt="Citizen Reported Defect" />
                    </div>
                  )}

                  {item.progressNotes && (
                    <div className="dept-card-note-box">
                      <span className="note-box-title">Latest Note:</span>
                      <p>{item.progressNotes}</p>
                    </div>
                  )}

                  <div className="dept-card-actions">
                    {item.status === 'ASSIGNED' && (
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={() => handleStartWork(item)}
                        disabled={isActionLoading}
                        data-testid={`card-start-work-${item.id}`}
                      >
                        {isActionLoading ? 'Starting...' : '▶ Start Work'}
                      </button>
                    )}

                    {item.status === 'IN_PROGRESS' && (
                      <>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleOpenProgressModal(item)}
                          data-testid={`card-add-progress-${item.id}`}
                        >
                          💬 Note
                        </button>
                        <button
                          type="button"
                          className="btn btn-primary btn-sm"
                          onClick={() => handleOpenResolveModal(item)}
                          data-testid={`card-resolve-issue-${item.id}`}
                        >
                          ✅ Resolve
                        </button>
                      </>
                    )}

                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleOpenDetail(item)}
                      data-testid={`card-view-detail-${item.id}`}
                    >
                      👁️ Details
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}

        {/* Department Complaint Detail & Action Drawer/Modal */}
        {detailComplaint && (
          <Modal
            isOpen={Boolean(detailComplaint)}
            onClose={() => setDetailComplaint(null)}
            title={`Complaint Detail - ${detailComplaint.complaintNumber}`}
            size="lg"
          >
            <div className="dept-detail-modal" data-testid="dept-detail-drawer">
              {detailLoading && (
                <div style={{ padding: '0.5rem 0' }}>
                  <Loader size="sm" message="Syncing timeline events..." />
                </div>
              )}

              {/* Status Stepper: ASSIGNED -> IN_PROGRESS -> RESOLVED */}
              <div className="task-workflow-stepper" data-testid="workflow-stepper">
                <div className={`stepper-node completed`}>
                  <div className="node-icon">📥</div>
                  <div className="node-info">
                    <span className="node-title">ASSIGNED</span>
                    <span className="node-sub">Dispatched to team</span>
                  </div>
                </div>

                <div className={`stepper-line ${detailComplaint.status === 'IN_PROGRESS' || detailComplaint.status === 'RESOLVED' ? 'active' : ''}`} />

                <div className={`stepper-node ${detailComplaint.status === 'IN_PROGRESS' || detailComplaint.status === 'RESOLVED' ? (detailComplaint.status === 'RESOLVED' ? 'completed' : 'active') : ''}`}>
                  <div className="node-icon">⚡</div>
                  <div className="node-info">
                    <span className="node-title">IN PROGRESS</span>
                    <span className="node-sub">
                      {detailComplaint.startedWorkingAt ? 'Field crew mobilized' : 'Work not started'}
                    </span>
                  </div>
                </div>

                <div className={`stepper-line ${detailComplaint.status === 'RESOLVED' ? 'active' : ''}`} />

                <div className={`stepper-node ${detailComplaint.status === 'RESOLVED' ? 'completed active' : ''}`}>
                  <div className="node-icon">✅</div>
                  <div className="node-info">
                    <span className="node-title">RESOLVED</span>
                    <span className="node-sub">
                      {detailComplaint.resolvedAt ? 'Completed with proof' : 'Pending resolution'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Action Buttons Inside Stepper */}
              <div className="drawer-actions-bar">
                {detailComplaint.status === 'ASSIGNED' && (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => {
                      handleStartWork(detailComplaint)
                    }}
                    data-testid="drawer-btn-start-work"
                  >
                    ▶ Start Work Now
                  </button>
                )}

                {detailComplaint.status === 'IN_PROGRESS' && (
                  <>
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => handleOpenProgressModal(detailComplaint)}
                      data-testid="drawer-btn-add-progress"
                    >
                      💬 Add Progress Note
                    </button>
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => handleOpenResolveModal(detailComplaint)}
                      data-testid="drawer-btn-resolve"
                    >
                      ✅ Resolve & Upload Proof
                    </button>
                  </>
                )}
              </div>

              {/* Defect Overview */}
              <div className="detail-section">
                <h4 className="detail-section-title">Defect Overview</h4>
                <div className="detail-grid">
                  <div className="detail-item">
                    <span className="detail-label">Title</span>
                    <span className="detail-val">{detailComplaint.title}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Location</span>
                    <span className="detail-val">📍 {detailComplaint.location}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Category</span>
                    <span className="detail-val">{detailComplaint.category?.replace(/_/g, ' ')}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Priority</span>
                    <span className={`priority-chip priority-${(detailComplaint.priority || 'medium').toLowerCase()}`}>
                      {detailComplaint.priority}
                    </span>
                  </div>
                </div>
                <div style={{ marginTop: '0.8rem' }}>
                  <span className="detail-label">Citizen Description:</span>
                  <p className="detail-desc-box">{detailComplaint.description}</p>
                </div>
              </div>

              {/* Before & After Evidence Comparison */}
              <div className="detail-section">
                <h4 className="detail-section-title">Photo Evidence & Verification</h4>
                <div className="evidence-compare-row" data-testid="evidence-inspection">
                  {/* Reported Defect Photo */}
                  <div className="evidence-card">
                    <div className="evidence-card-header">
                      <span className="evidence-pill pill-reported">Reported Defect Photo</span>
                    </div>
                    {detailComplaint.imageUrl ? (
                      <div className="evidence-img-container">
                        <img
                          src={detailComplaint.imageUrl}
                          alt="Citizen Defect Evidence"
                          className="evidence-img"
                        />
                      </div>
                    ) : (
                      <div className="evidence-placeholder">
                        <span>📷 No citizen defect photo submitted</span>
                      </div>
                    )}
                  </div>

                  {/* Resolution Proof Photo */}
                  <div className="evidence-card">
                    <div className="evidence-card-header">
                      <span className="evidence-pill pill-resolved">Department Resolution Proof</span>
                    </div>
                    {detailComplaint.resolutionImageUrl ? (
                      <div className="evidence-img-container">
                        <img
                          src={detailComplaint.resolutionImageUrl}
                          alt="Department Resolution Proof"
                          className="evidence-img"
                        />
                      </div>
                    ) : (
                      <div className="evidence-placeholder">
                        <span>⏳ Resolution proof pending completion</span>
                      </div>
                    )}
                  </div>
                </div>

                {detailComplaint.resolutionNotes && (
                  <div className="resolution-notes-summary">
                    <span className="detail-label">Resolution Notes:</span>
                    <p className="detail-desc-box">{detailComplaint.resolutionNotes}</p>
                  </div>
                )}
              </div>

              {/* Phase 9: Chronological Audit Trail & Assignment History */}
              <div className="detail-section">
                <ComplaintAuditTimeline
                  complaintId={detailComplaint.id}
                  initialEvents={detailComplaint.timeline}
                />
              </div>
            </div>
          </Modal>
        )}

        {/* Modal: Add Progress Update */}
        {progressModal.open && (
          <Modal
            isOpen={progressModal.open}
            onClose={() => setProgressModal({ open: false, complaint: null })}
            title={`Add Progress Update - ${progressModal.complaint?.complaintNumber}`}
          >
            <form onSubmit={handleSubmitProgress} data-testid="form-progress-notes">
              <p className="modal-lead-text">
                Record interim field updates, dispatch logistics, or field team remarks to the complaint timeline.
              </p>

              {progressError && (
                <div className="form-error-banner" role="alert" data-testid="progress-error-banner">
                  {progressError}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="progressNotesInput">Field Remarks & Work Notes *</label>
                <textarea
                  id="progressNotesInput"
                  rows={4}
                  className="form-textarea"
                  placeholder="e.g. Surface graded and rolled; awaiting bitumen temperature stabilization..."
                  value={progressNoteText}
                  onChange={(e) => setProgressNoteText(e.target.value)}
                  data-testid="progress-notes-input"
                  required
                />
              </div>

              <div className="modal-actions-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setProgressModal({ open: false, complaint: null })}
                  disabled={progressSubmitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={progressSubmitting}
                  data-testid="btn-save-progress"
                >
                  {progressSubmitting ? 'Saving...' : 'Save Progress Note'}
                </button>
              </div>
            </form>
          </Modal>
        )}

        {/* Modal: Resolve Complaint */}
        {resolveModal.open && (
          <Modal
            isOpen={resolveModal.open}
            onClose={() => setResolveModal({ open: false, complaint: null })}
            title={`Resolve Complaint - ${resolveModal.complaint?.complaintNumber}`}
            size="lg"
          >
            <form onSubmit={handleSubmitResolve} data-testid="form-resolve-complaint">
              <p className="modal-lead-text">
                To resolve this complaint, explain the actions taken by municipal crews and attach verified resolution photo proof.
              </p>

              {resolveError && (
                <div className="form-error-banner" role="alert" data-testid="resolve-error-banner">
                  {resolveError}
                </div>
              )}

              {/* Side-by-Side Comparison Preview in Modal */}
              <div className="resolve-evidence-preview-row">
                <div className="resolve-preview-col">
                  <span className="resolve-preview-label">Citizen Reported Defect</span>
                  <div className="preview-box">
                    {resolveModal.complaint?.imageUrl ? (
                      <img
                        src={resolveModal.complaint.imageUrl}
                        alt="Reported Defect"
                        className="preview-image"
                      />
                    ) : (
                      <div className="preview-empty">📷 No initial photo</div>
                    )}
                  </div>
                </div>

                <div className="resolve-preview-col">
                  <span className="resolve-preview-label">Your Resolution Proof Photo</span>
                  <div className="preview-box proof-preview-box">
                    {resolutionPhotoPreview ? (
                      <div className="uploaded-preview-container">
                        <img
                          src={resolutionPhotoPreview}
                          alt="Resolution Proof Preview"
                          className="preview-image"
                          data-testid="resolution-photo-preview"
                        />
                        <button
                          type="button"
                          className="btn-remove-preview"
                          onClick={handleRemovePhoto}
                          title="Remove photo"
                        >
                          ✕ Remove
                        </button>
                      </div>
                    ) : (
                      <div className="preview-empty upload-prompt">
                        <span>Drag & drop or select proof photo below</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* File Upload Input */}
              <div className="form-group" style={{ marginTop: '1rem' }}>
                <label htmlFor="proofPhotoInput">Upload Resolution Proof Photograph (Optional / Recommended)</label>
                <div className="file-upload-dropzone">
                  <input
                    type="file"
                    id="proofPhotoInput"
                    accept="image/png, image/jpeg, image/jpg, image/webp"
                    onChange={handlePhotoChange}
                    className="file-input-hidden"
                    data-testid="input-resolution-proof"
                  />
                  <label htmlFor="proofPhotoInput" className="file-upload-label">
                    <span>📁 Choose Image or Drag File Here</span>
                    <span className="file-upload-help">Supported formats: JPG, PNG, WEBP (Max 10 MB)</span>
                  </label>
                </div>
              </div>

              {/* Required Resolution Notes */}
              <div className="form-group">
                <label htmlFor="resolutionNotesInput">Resolution & Completion Notes *</label>
                <textarea
                  id="resolutionNotesInput"
                  rows={4}
                  className="form-textarea"
                  placeholder="Detail the repairs, equipment used, and inspection sign-off before closing..."
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  data-testid="resolution-notes-input"
                  required
                />
              </div>

              <div className="modal-actions-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setResolveModal({ open: false, complaint: null })}
                  disabled={resolveSubmitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={resolveSubmitting}
                  data-testid="btn-confirm-resolve"
                >
                  {resolveSubmitting ? 'Submitting...' : 'Confirm Resolution'}
                </button>
              </div>
            </form>
          </Modal>
        )}
      </div>
    </DashboardLayout>
  )
}

export default DepartmentComplaints
