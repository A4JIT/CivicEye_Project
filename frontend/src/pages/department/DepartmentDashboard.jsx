import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import dashboardService from '../../services/dashboardService'
import Loader from '../../components/common/Loader'

const DepartmentDashboard = () => {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let isMounted = true
    const fetchData = async () => {
      try {
        const result = await dashboardService.getDepartmentDashboard(1)
        if (isMounted) setData(result)
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
  const complaints = data?.recentComplaints || []

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
        <div className="severity-badges-row">
          <div className="severity-item critical">
            <span className="sev-label">Critical</span>
            <span className="sev-count">{data?.criticalCount || 0}</span>
          </div>
          <div className="severity-item high">
            <span className="sev-label">High</span>
            <span className="sev-count">{data?.highCount || 0}</span>
          </div>
          <div className="severity-item medium">
            <span className="sev-label">Medium</span>
            <span className="sev-count">{data?.mediumCount || 0}</span>
          </div>
          <div className="severity-item low">
            <span className="sev-label">Low</span>
            <span className="sev-count">{data?.lowCount || 0}</span>
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
          <Link to="/department/assigned" className="section-link">
            All Assigned Tasks →
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
                    <span className="tracking-code">{item.trackingNumber}</span>
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
                    <span className={`badge badge-status status-${item.status?.toLowerCase()}`}>
                      {item.status?.replace('_', ' ')}
                    </span>
                  </td>
                  <td>
                    <button className="btn btn-sm btn-secondary" title="Update status">
                      Update ↗
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </DashboardLayout>
  )
}

export default DepartmentDashboard
