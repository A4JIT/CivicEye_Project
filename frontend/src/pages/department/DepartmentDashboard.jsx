import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import dashboardService from '../../services/dashboardService'
import complaintService from '../../services/complaintService'
import Loader from '../../components/common/Loader'

const DepartmentDashboard = () => {
  const [data, setData] = useState(null)
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)

  // Status update modal state
  const [selectedComplaint, setSelectedComplaint] = useState(null)
  const [newStatus, setNewStatus] = useState('IN_PROGRESS')
  const [remarks, setRemarks] = useState('')
  const [resolutionPhoto, setResolutionPhoto] = useState(null)
  const [photoPreview, setPhotoPreview] = useState(null)
  const [savingStatus, setSavingStatus] = useState(false)
  const [statusSuccessMsg, setStatusSuccessMsg] = useState(null)
  const [statusErrorMsg, setStatusErrorMsg] = useState(null)

  useEffect(() => {
    let isMounted = true
    const fetchData = async () => {
      try {
        const result = await dashboardService.getDepartmentDashboard(1)
        if (isMounted) {
          setData(result)
          setComplaints(result?.recentComplaints || [])
        }
      } catch (err) {
        console.error('Failed to load department metrics', err)
      } finally {
        if (isMounted) setLoading(false)
      }
    }
    fetchData()
    return () => {
      isMounted = false
    }
  }, [])

  const handleOpenModal = (item) => {
    setSelectedComplaint(item)
    // Default next status suggestion
    if (item.status === 'ASSIGNED' || item.status === 'SUBMITTED') {
      setNewStatus('IN_PROGRESS')
    } else if (item.status === 'IN_PROGRESS') {
      setNewStatus('RESOLVED')
    } else {
      setNewStatus('IN_PROGRESS')
    }
    setRemarks('')
    setResolutionPhoto(null)
    setPhotoPreview(null)
    setStatusSuccessMsg(null)
    setStatusErrorMsg(null)
  }

  const handleCloseModal = () => {
    setSelectedComplaint(null)
    setRemarks('')
    setResolutionPhoto(null)
    setPhotoPreview(null)
    setStatusSuccessMsg(null)
    setStatusErrorMsg(null)
  }

  const handlePhotoChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      setResolutionPhoto(file)
      const previewUrl = URL.createObjectURL(file)
      setPhotoPreview(previewUrl)
    }
  }

  const handleSaveStatus = async (e) => {
    e.preventDefault()
    if (!selectedComplaint) return

    setSavingStatus(true)
    setStatusErrorMsg(null)
    try {
      let payload
      if (resolutionPhoto) {
        payload = new FormData()
        payload.append('status', newStatus)
        if (remarks.trim()) payload.append('remarks', remarks.trim())
        payload.append('image', resolutionPhoto)
      } else {
        payload = {
          status: newStatus,
          remarks: remarks.trim() || undefined,
        }
      }

      const targetId = selectedComplaint.id || 1
      await complaintService.updateComplaintStatus(targetId, payload)

      // Update table state locally
      setComplaints((prev) =>
        prev.map((c) =>
          c.trackingNumber === selectedComplaint.trackingNumber
            ? { ...c, status: newStatus }
            : c
        )
      )

      setStatusSuccessMsg(`Successfully updated status to ${newStatus.replace(/_/g, ' ')}`)
      setTimeout(() => {
        handleCloseModal()
      }, 1500)
    } catch (err) {
      console.error('Update status error:', err)
      const msg = err.response?.data?.message || err.message || 'Failed to update status. Please try again.'
      setStatusErrorMsg(msg)
    } finally {
      setSavingStatus(false)
    }
  }

  if (loading) {
    return (
      <DashboardLayout title="Department Operations" subtitle="Connecting to unit dispatch stream...">
        <div className="dashboard-loading-box">
          <Loader size="lg" message="Loading department assigned tasks and priority queue..." />
        </div>
      </DashboardLayout>
    )
  }

  const deptName = data?.departmentName || 'Roads & Traffic'

  return (
    <DashboardLayout
      title={`${deptName} Console`}
      subtitle={`Unit Code: [${data?.departmentCode || 'DEPT'}] • Field workload queue and resolution compliance`}
    >
      {/* Priority Alert Box if Critical exists */}
      {(data?.criticalCount || 0) > 0 && (
        <div className="urgent-alert-banner" data-testid="critical-alert-banner">
          <span className="urgent-icon">🚨</span>
          <div className="urgent-text">
            <strong>URGENT SLA NOTICE:</strong> You have {data?.criticalCount} critical hazard complaint(s) requiring immediate dispatch within 2 hours.
          </div>
          <Link to="/department/urgent" className="btn btn-sm btn-danger">
            View Urgent ({data?.criticalCount})
          </Link>
        </div>
      )}

      {/* KPI Cards */}
      <div className="stats-grid" data-testid="dept-stats-grid">
        <div className="stat-card">
          <div className="stat-card-header">
            <span className="stat-label">Assigned Workload</span>
            <span className="stat-icon">📋</span>
          </div>
          <div className="stat-value">{data?.totalAssigned || 0}</div>
          <div className="stat-footer text-muted">Total tasks routed to unit</div>
        </div>

        <div className="stat-card warning-accent">
          <div className="stat-card-header">
            <span className="stat-label">Pending Action</span>
            <span className="stat-icon">⏳</span>
          </div>
          <div className="stat-value">{data?.pendingComplaints || 0}</div>
          <div className="stat-footer text-warning">Awaiting crew assignment</div>
        </div>

        <div className="stat-card primary-accent">
          <div className="stat-card-header">
            <span className="stat-label">In Progress</span>
            <span className="stat-icon">🛠️</span>
          </div>
          <div className="stat-value">{data?.inProgressComplaints || 0}</div>
          <div className="stat-footer text-primary">Crews currently on-site</div>
        </div>

        <div className="stat-card success-accent">
          <div className="stat-card-header">
            <span className="stat-label">Completed & Verified</span>
            <span className="stat-icon">✅</span>
          </div>
          <div className="stat-value">{data?.resolvedComplaints || 0}</div>
          <div className="stat-footer text-success">Resolved successfully</div>
        </div>
      </div>

      {/* Priority Distribution Matrix */}
      <div className="priority-matrix-card" data-testid="priority-matrix">
        <h4 className="matrix-title">Severity Breakdown</h4>
        <div className="priority-bars">
          <div className="priority-bar-item">
            <span className="badge badge-priority priority-critical">Critical</span>
            <span className="priority-count">{data?.criticalCount || 0}</span>
          </div>
          <div className="priority-bar-item">
            <span className="badge badge-priority priority-high">High</span>
            <span className="priority-count">{data?.highCount || 0}</span>
          </div>
          <div className="priority-bar-item">
            <span className="badge badge-priority priority-medium">Medium</span>
            <span className="priority-count">{data?.mediumCount || 0}</span>
          </div>
          <div className="priority-bar-item">
            <span className="badge badge-priority priority-low">Low</span>
            <span className="priority-count">{data?.lowCount || 0}</span>
          </div>
        </div>
      </div>

      {/* Assigned Tasks Feed */}
      <div className="dashboard-section" data-testid="dept-complaints-section">
        <div className="section-header">
          <div>
            <h3 className="section-title">Department Work Queue</h3>
            <p className="section-subtitle">Assigned citizen reports awaiting status updates</p>
          </div>
          <Link to="/complaints/track" className="section-link">
            Track Any Complaint 🔍
          </Link>
        </div>

        <div className="table-responsive">
          <table className="custom-table" data-testid="dept-complaints-table">
            <thead>
              <tr>
                <th>Tracking ID</th>
                <th>Issue Details</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {complaints.map((item) => (
                <tr key={item.id || item.trackingNumber}>
                  <td className="tracking-cell">
                    <Link
                      to={`/complaints/track/${item.trackingNumber}`}
                      className="tracking-code-link"
                      title="View live audit timeline"
                    >
                      {item.trackingNumber}
                    </Link>
                  </td>
                  <td className="title-cell">
                    <span className="complaint-title-text">{item.title}</span>
                    {item.addressText && (
                      <span className="complaint-location">📍 {item.addressText}</span>
                    )}
                  </td>
                  <td>
                    <span className={`badge badge-priority priority-${item.priority?.toLowerCase()}`}>
                      {item.priority}
                    </span>
                  </td>
                  <td>
                    <span className={`badge badge-status status-${item.status?.toLowerCase().replace(/_/g, '-')}`}>
                      {item.status?.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td>
                    <div className="action-button-group">
                      <button
                        type="button"
                        className="btn btn-sm btn-primary"
                        onClick={() => handleOpenModal(item)}
                        data-testid={`btn-update-${item.trackingNumber}`}
                      >
                        Update ↗
                      </button>
                      <Link
                        to={`/complaints/track/${item.trackingNumber}`}
                        className="btn btn-sm btn-secondary"
                        title="View live audit timeline"
                      >
                        Timeline
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Status Resolution Modal */}
      {selectedComplaint && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" data-testid="status-modal">
          <div className="modal-container">
            <div className="modal-header">
              <h3 className="modal-title">
                ⚙️ Update Workflow Status: <span className="text-cyan">{selectedComplaint.trackingNumber}</span>
              </h3>
              <button
                type="button"
                className="modal-close-btn"
                onClick={handleCloseModal}
                disabled={savingStatus}
              >
                ✕
              </button>
            </div>

            {statusSuccessMsg && (
              <div className="alert-success" role="alert">
                <span>✓</span> {statusSuccessMsg}
              </div>
            )}

            {statusErrorMsg && (
              <div className="alert-error" role="alert">
                <span>⚠️</span> {statusErrorMsg}
              </div>
            )}

            <form onSubmit={handleSaveStatus} className="status-modal-form">
              <div className="modal-complaint-info">
                <strong>Defect:</strong> {selectedComplaint.title}
                <br />
                <strong>Current Status:</strong>{' '}
                <span className="badge badge-status status-subtle">
                  {selectedComplaint.status?.replace(/_/g, ' ')}
                </span>
              </div>

              <div className="form-group">
                <label htmlFor="newStatusSelect" className="form-label">
                  Advance Status To <span className="required">*</span>
                </label>
                <select
                  id="newStatusSelect"
                  className="form-select"
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value)}
                  disabled={savingStatus}
                  data-testid="status-select"
                >
                  <option value="IN_PROGRESS">🛠️ IN_PROGRESS - Crew On Site</option>
                  <option value="RESOLVED">✅ RESOLVED - Work Completed</option>
                  <option value="NEEDS_INFORMATION">❓ NEEDS_INFORMATION - Query Citizen</option>
                  <option value="REJECTED">🛑 REJECTED - Outside Department Scope</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="statusRemarks" className="form-label">
                  Inspection / Resolution Remarks
                </label>
                <textarea
                  id="statusRemarks"
                  className="form-textarea"
                  rows={3}
                  placeholder="Record work done, asphalt tons used, team dispatched, or resolution notes..."
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  disabled={savingStatus}
                  data-testid="status-remarks-input"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Resolution Proof Photo (Optional)</label>
                <input
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  onChange={handlePhotoChange}
                  disabled={savingStatus}
                  className="form-input file-input-control"
                  data-testid="resolution-file-input"
                />
                {photoPreview && (
                  <div className="modal-photo-preview">
                    <img src={photoPreview} alt="Resolution preview" />
                    <button
                      type="button"
                      className="btn-remove-preview"
                      onClick={() => {
                        setResolutionPhoto(null)
                        setPhotoPreview(null)
                      }}
                    >
                      Remove Photo ✕
                    </button>
                  </div>
                )}
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleCloseModal}
                  disabled={savingStatus}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={savingStatus}
                  data-testid="btn-confirm-status"
                >
                  {savingStatus ? 'Saving...' : '💾 Save Status Transition'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}

export default DepartmentDashboard
