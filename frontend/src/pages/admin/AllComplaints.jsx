import { useState, useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import adminService from '../../services/adminService'
import feedbackService from '../../services/feedbackService'
import ComplaintStatusBadge from '../../components/complaints/ComplaintStatusBadge'
import ReviewComplaint from './ReviewComplaint'
import Loader from '../../components/common/Loader'
import Modal from '../../components/common/Modal'
import ComplaintAuditTimeline from '../../components/complaints/ComplaintAuditTimeline'
import historyService from '../../services/historyService'
import AuthenticatedImage from '../../components/common/AuthenticatedImage'

const STATUS_TABS = [
  { id: 'ALL', label: 'All Complaints', icon: '📑' },
  { id: 'SUBMITTED', label: 'Pending Review', icon: '📥' },
  { id: 'UNDER_REVIEW', label: 'Under Review', icon: '🔍' },
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

const PRIORITY_WEIGHTS = {
  CRITICAL: 4,
  HIGH: 3,
  MEDIUM: 2,
  LOW: 1,
}

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

  // Phase 9: Date Range & Sorting States
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortDir, setSortDir] = useState('desc')
  const [auditModalComplaint, setAuditModalComplaint] = useState(null)

  // Selected Complaint for Drawer Review
  const [selectedComplaint, setSelectedComplaint] = useState(null)
  const [previewImage, setPreviewImage] = useState(null)

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
      setComplaints(data?.complaints || (Array.isArray(data) ? data : []))
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

  // Filtered and sorted complaint list
  const filteredComplaints = useMemo(() => {
    const list = complaints.filter((item) => {
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

      // 5. Date Range Filter
      if (startDate && item.createdAt) {
        const itemDate = new Date(item.createdAt)
        const filterStart = new Date(startDate + 'T00:00:00')
        if (itemDate < filterStart) return false
      }
      if (endDate && item.createdAt) {
        const itemDate = new Date(item.createdAt)
        const filterEnd = new Date(endDate + 'T23:59:59')
        if (itemDate > filterEnd) return false
      }

      // 6. Search Query
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

    return list.sort((a, b) => {
      let comparison = 0
      if (sortBy === 'priority') {
        const wa = PRIORITY_WEIGHTS[a.priority] || 0
        const wb = PRIORITY_WEIGHTS[b.priority] || 0
        comparison = wa - wb
      } else if (sortBy === 'updatedAt') {
        comparison = new Date(a.updatedAt || 0) - new Date(b.updatedAt || 0)
      } else if (sortBy === 'status') {
        comparison = (a.status || '').localeCompare(b.status || '')
      } else {
        // Default: createdAt
        comparison = new Date(a.createdAt || 0) - new Date(b.createdAt || 0)
      }
      return sortDir === 'asc' ? comparison : -comparison
    })
  }, [
    complaints,
    activeStatusTab,
    categoryFilter,
    priorityFilter,
    deptFilter,
    startDate,
    endDate,
    searchQuery,
    sortBy,
    sortDir,
  ])

  // Live KPI Summary Metrics for Monitoring Console
  const kpiMetrics = useMemo(() => {
    return {
      total: complaints.length,
      filtered: filteredComplaints.length,
      critical: complaints.filter((c) => c.priority === 'CRITICAL').length,
      unassigned: complaints.filter(
        (c) => !c.departmentId && c.status !== 'REJECTED' && c.status !== 'CLOSED'
      ).length,
      inProgress: complaints.filter(
        (c) => c.status === 'IN_PROGRESS' || c.status === 'ASSIGNED'
      ).length,
      resolvedClosed: complaints.filter(
        (c) => c.status === 'RESOLVED' || c.status === 'CLOSED'
      ).length,
    }
  }, [complaints, filteredComplaints])

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

        {/* Phase 9: Municipal Monitoring Executive KPI Summary Bar */}
        <div className="monitoring-summary-grid" data-testid="monitoring-summary-bar">
          <div className="monitoring-kpi-card">
            <span className="monitoring-kpi-icon">📊</span>
            <div className="monitoring-kpi-details">
              <span className="monitoring-kpi-count" data-testid="kpi-total-count">{kpiMetrics.total}</span>
              <span className="monitoring-kpi-label">Total Grievances ({kpiMetrics.filtered} matching)</span>
            </div>
          </div>
          <div className="monitoring-kpi-card">
            <span className="monitoring-kpi-icon">⚠️</span>
            <div className="monitoring-kpi-details">
              <span className="monitoring-kpi-count" data-testid="kpi-critical-count">{kpiMetrics.critical}</span>
              <span className="monitoring-kpi-label">Critical Urgency</span>
            </div>
          </div>
          <div className="monitoring-kpi-card">
            <span className="monitoring-kpi-icon">🏢</span>
            <div className="monitoring-kpi-details">
              <span className="monitoring-kpi-count" data-testid="kpi-unassigned-count">{kpiMetrics.unassigned}</span>
              <span className="monitoring-kpi-label">Unassigned Route</span>
            </div>
          </div>
          <div className="monitoring-kpi-card">
            <span className="monitoring-kpi-icon">⚡</span>
            <div className="monitoring-kpi-details">
              <span className="monitoring-kpi-count" data-testid="kpi-active-count">{kpiMetrics.inProgress}</span>
              <span className="monitoring-kpi-label">Active in Field</span>
            </div>
          </div>
          <div className="monitoring-kpi-card">
            <span className="monitoring-kpi-icon">✅</span>
            <div className="monitoring-kpi-details">
              <span className="monitoring-kpi-count" data-testid="kpi-resolved-count">{kpiMetrics.resolvedClosed}</span>
              <span className="monitoring-kpi-label">Resolved / Closed</span>
            </div>
          </div>
        </div>

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

            {/* Date Range Filtering */}
            <div className="filter-date-group" data-testid="filter-date-group">
              <input
                type="date"
                className="filter-date-input"
                aria-label="Start date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                data-testid="filter-start-date"
                title="Filter complaints filed from date"
              />
              <span style={{ color: '#64748b' }}>–</span>
              <input
                type="date"
                className="filter-date-input"
                aria-label="End date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                data-testid="filter-end-date"
                title="Filter complaints filed to date"
              />
            </div>

            {/* Sorting Dropdown */}
            <select
              className="filter-select"
              value={`${sortBy}_${sortDir}`}
              onChange={(e) => {
                const [field, dir] = e.target.value.split('_')
                setSortBy(field)
                setSortDir(dir)
              }}
              data-testid="filter-select-sort"
            >
              <option value="createdAt_desc">Date: Newest First</option>
              <option value="createdAt_asc">Date: Oldest First</option>
              <option value="priority_desc">Priority: Critical to Low</option>
              <option value="priority_asc">Priority: Low to Critical</option>
              <option value="status_asc">Status (A-Z)</option>
              <option value="updatedAt_desc">Last Updated</option>
            </select>

            {(searchQuery || categoryFilter !== 'ALL' || priorityFilter !== 'ALL' || deptFilter !== 'ALL' || activeStatusTab !== 'ALL' || startDate || endDate) && (
              <button
                type="button"
                className="btn-reset-filters"
                onClick={() => {
                  setSearchQuery('')
                  setCategoryFilter('ALL')
                  setPriorityFilter('ALL')
                  setDeptFilter('ALL')
                  setActiveStatusTab('ALL')
                  setStartDate('')
                  setEndDate('')
                }}
                data-testid="btn-reset-filters"
              >
                Reset Filters
              </button>
            )}
          </div>
        </div>

        {/* Phase 9: Active Filter Badges with 1-Click Removal */}
        {(searchQuery || categoryFilter !== 'ALL' || priorityFilter !== 'ALL' || deptFilter !== 'ALL' || activeStatusTab !== 'ALL' || startDate || endDate) && (
          <div className="active-filter-chips-row" data-testid="active-filter-chips">
            <span style={{ fontSize: '0.8rem', color: '#94a3b8', fontWeight: 600 }}>Active Filters:</span>
            {activeStatusTab !== 'ALL' && (
              <span className="filter-chip" data-testid="chip-filter-status">
                Status: {activeStatusTab}
                <button type="button" className="chip-remove-btn" onClick={() => setActiveStatusTab('ALL')}>×</button>
              </span>
            )}
            {categoryFilter !== 'ALL' && (
              <span className="filter-chip" data-testid="chip-filter-category">
                Category: {categoryFilter.replace(/_/g, ' ')}
                <button type="button" className="chip-remove-btn" onClick={() => setCategoryFilter('ALL')}>×</button>
              </span>
            )}
            {priorityFilter !== 'ALL' && (
              <span className="filter-chip" data-testid="chip-filter-priority">
                Priority: {priorityFilter}
                <button type="button" className="chip-remove-btn" onClick={() => setPriorityFilter('ALL')}>×</button>
              </span>
            )}
            {deptFilter !== 'ALL' && (
              <span className="filter-chip" data-testid="chip-filter-dept">
                Dept: {departments.find(d => String(d.id) === String(deptFilter))?.name || deptFilter}
                <button type="button" className="chip-remove-btn" onClick={() => setDeptFilter('ALL')}>×</button>
              </span>
            )}
            {(startDate || endDate) && (
              <span className="filter-chip" data-testid="chip-filter-date">
                Date: {startDate || 'Start'} to {endDate || 'Now'}
                <button type="button" className="chip-remove-btn" onClick={() => { setStartDate(''); setEndDate(''); }}>×</button>
              </span>
            )}
            {searchQuery && (
              <span className="filter-chip" data-testid="chip-filter-search">
                Search: "{searchQuery}"
                <button type="button" className="chip-remove-btn" onClick={() => setSearchQuery('')}>×</button>
              </span>
            )}
            <button
              type="button"
              className="clear-all-chips-btn"
              onClick={() => {
                setSearchQuery('')
                setCategoryFilter('ALL')
                setPriorityFilter('ALL')
                setDeptFilter('ALL')
                setActiveStatusTab('ALL')
                setStartDate('')
                setEndDate('')
              }}
              data-testid="btn-clear-all-chips"
            >
              Clear All
            </button>
          </div>
        )}

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
                        {item.imageUrl && (
                          <button
                            type="button"
                            className="photo-proof-badge"
                            title="Click to view citizen uploaded photo proof"
                            onClick={(e) => {
                              e.stopPropagation()
                              setPreviewImage({ url: item.imageUrl, title: item.title, complaintNumber: item.complaintNumber })
                            }}
                            style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '4px',
                              background: 'rgba(56, 189, 248, 0.15)',
                              color: '#38bdf8',
                              border: '1px solid rgba(56, 189, 248, 0.4)',
                              borderRadius: '6px',
                              padding: '2px 8px',
                              fontSize: '0.75rem',
                              cursor: 'pointer',
                              fontWeight: 600,
                            }}
                          >
                            📸 View Photo Proof
                          </button>
                        )}
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

                        <button
                          type="button"
                          className="btn-action-pill btn-row-audit"
                          title="View complete chronological audit trail and assignment history"
                          onClick={() => setAuditModalComplaint(item)}
                          data-testid={`action-audit-${item.id}`}
                        >
                          ⏱️ History
                        </button>
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

        {/* Phase 9: Standalone 1-Click Grievance Audit Trail Modal */}
        {auditModalComplaint && (
          <Modal
            isOpen={Boolean(auditModalComplaint)}
            onClose={() => setAuditModalComplaint(null)}
            title={`Grievance Audit Trail: ${auditModalComplaint.complaintNumber} — ${auditModalComplaint.title}`}
            size="lg"
            footer={
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setAuditModalComplaint(null)}
                data-testid="btn-close-audit-modal"
              >
                Close Audit Log
              </button>
            }
          >
            <div className="audit-modal-body" data-testid="audit-modal-content">
              <ComplaintAuditTimeline complaintId={auditModalComplaint.id} />
            </div>
          </Modal>
        )}

        {/* Standalone Evidence Photo Preview Modal */}
        {previewImage && (
          <Modal
            isOpen={Boolean(previewImage)}
            onClose={() => setPreviewImage(null)}
            title={`📸 Citizen Photo Evidence: ${previewImage.complaintNumber}`}
            maxWidth="700px"
            footer={
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setPreviewImage(null)}
              >
                Close Preview
              </button>
            }
          >
            <div style={{ textAlign: 'center', padding: '1rem' }}>
              <div style={{ marginBottom: '1rem', color: '#94a3b8', fontSize: '0.9rem', textAlign: 'left' }}>
                <strong>Defect:</strong> {previewImage.title}
              </div>
              <AuthenticatedImage
                src={previewImage.url}
                alt="Citizen Defect Evidence Proof"
                style={{
                  maxWidth: '100%',
                  maxHeight: '65vh',
                  borderRadius: '10px',
                  boxShadow: '0 8px 30px rgba(0, 0, 0, 0.4)',
                  objectFit: 'contain',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                }}
                allowZoom={true}
              />
            </div>
          </Modal>
        )}
      </div>
    </DashboardLayout>
  )
}

export default AllComplaints
