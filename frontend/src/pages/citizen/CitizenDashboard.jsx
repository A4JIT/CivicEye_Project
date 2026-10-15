import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import dashboardService from '../../services/dashboardService'
import { useAuth } from '../../context/AuthContext'
import Loader from '../../components/common/Loader'

const CitizenDashboard = () => {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let isMounted = true
    const fetchData = async () => {
      try {
        const result = await dashboardService.getCitizenDashboard(user?.id)
        if (isMounted) {
          setData(result)
        }
      } catch (err) {
        console.error('Failed to load citizen metrics', err)
      } finally {
        if (isMounted) setLoading(false)
      }
    }
    fetchData()
    return () => {
      isMounted = false
    }
  }, [user?.id])

  if (loading) {
    return (
      <DashboardLayout title="Citizen Dashboard" subtitle="Loading your civic portal...">
        <div className="dashboard-loading-box">
          <Loader size="lg" message="Loading your civic requests and resolution tracking..." />
        </div>
      </DashboardLayout>
    )
  }

  const complaints = data?.recentComplaints || []
  const resolvedItems = complaints.filter(
    (c) => c.status === 'RESOLVED' || c.status === 'CLOSED'
  )

  return (
    <DashboardLayout
      title={`Welcome, ${user?.fullName || user?.username || 'Citizen'}`}
      subtitle="Track your reported municipal complaints, monitor dispatch progress, and submit new civic requests."
    >
      {/* Quick Action Hero Card */}
      <div className="citizen-hero-card">
        <div className="hero-content">
          <span className="hero-tag">AI Powered Reporting</span>
          <h2>Spot an Issue in Your Neighborhood?</h2>
          <p>
            Report potholes, broken streetlights, water leaks, or garbage overflows in seconds.
            Our AI automatically tags priority, assigns the proper department, and notifies you of updates.
          </p>
          <div className="hero-buttons">
            <Link to="/complaints/new" className="btn btn-primary" id="btn-report-issue">
              📸 Report New Civic Issue
            </Link>
            <Link to="/complaints/my" className="btn btn-secondary" id="btn-view-all">
              View All Submissions
            </Link>
          </div>
        </div>
      </div>

      {/* Phase 8: Resolution Verification Alert Banner if complaints are resolved */}
      {(data?.resolvedComplaints > 0 || resolvedItems.length > 0) && (
        <div className="citizen-resolution-banner" data-testid="citizen-resolved-notice">
          <div className="resolution-banner-left">
            <span className="resolution-seal-icon">⭐</span>
            <div>
              <strong>Resolution Verification & Rating Available</strong>
              <p>
                Municipal maintenance crews have completed work on your reported issue.
                Review before-and-after photo proof, rate your satisfaction, and confirm closure.
              </p>
            </div>
          </div>
          {resolvedItems.length > 0 && (
            <Link
              to={`/complaints/track/${resolvedItems[0].trackingNumber || resolvedItems[0].complaintNumber}`}
              className="btn btn-sm btn-success"
              data-testid="btn-verify-top-resolved"
            >
              Verify Proof & Rate ↗
            </Link>
          )}
        </div>
      )}

      {/* KPI Stats Grid */}
      <div className="stats-grid" data-testid="citizen-stats-grid">
        <div className="stat-card">
          <div className="stat-card-header">
            <span className="stat-label">Total Reported</span>
            <span className="stat-icon">📝</span>
          </div>
          <div className="stat-value">{data?.totalComplaints || 0}</div>
          <div className="stat-footer text-muted">All-time submissions</div>
        </div>

        <div className="stat-card warning-accent">
          <div className="stat-card-header">
            <span className="stat-label">Pending Triage</span>
            <span className="stat-icon">⏳</span>
          </div>
          <div className="stat-value">{data?.pendingComplaints || 0}</div>
          <div className="stat-footer text-warning">Awaiting department dispatch</div>
        </div>

        <div className="stat-card primary-accent">
          <div className="stat-card-header">
            <span className="stat-label">In Progress</span>
            <span className="stat-icon">⚙️</span>
          </div>
          <div className="stat-value">{data?.inProgressComplaints || 0}</div>
          <div className="stat-footer text-primary">Field team actively working</div>
        </div>

        <div className="stat-card success-accent">
          <div className="stat-card-header">
            <span className="stat-label">Resolved</span>
            <span className="stat-icon">✅</span>
          </div>
          <div className="stat-value">{data?.resolvedComplaints || 0}</div>
          <div className="stat-footer text-success">
            {data?.totalComplaints > 0
              ? `${Math.round(((data?.resolvedComplaints || 0) / data?.totalComplaints) * 100)}% resolution rate`
              : 'Resolved tickets'}
          </div>
        </div>
      </div>

      {/* Recent Submissions Section */}
      <div className="dashboard-section" data-testid="recent-complaints-section">
        <div className="section-header">
          <div>
            <h3 className="section-title">Recent Submissions</h3>
            <p className="section-subtitle">Real-time status of your latest neighborhood reports</p>
          </div>
          <Link to="/complaints/my" className="section-link">
            See all ({data?.totalComplaints || 0}) →
          </Link>
        </div>

        {complaints.length === 0 ? (
          <div className="empty-state-card" data-testid="empty-complaints">
            <div className="empty-icon">🌱</div>
            <h4>No Civic Issues Reported Yet</h4>
            <p>Your neighborhood is looking great! If you notice any civic defect, tap Report Issue above.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="custom-table" data-testid="citizen-complaints-table">
              <thead>
                <tr>
                  <th>Tracking ID</th>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Department</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {complaints.map((item) => (
                  <tr key={item.id || item.trackingNumber}>
                    <td className="tracking-cell">
                      <Link
                        to={`/complaints/track/${item.trackingNumber || item.complaintNumber}`}
                        className="tracking-code-link"
                      >
                        <span className="tracking-code">{item.trackingNumber || item.complaintNumber}</span>
                      </Link>
                    </td>
                    <td className="title-cell">
                      <span className="complaint-title-text">{item.title}</span>
                      {item.addressText && (
                        <span className="complaint-location">📍 {item.addressText}</span>
                      )}
                    </td>
                    <td>
                      <span className="badge badge-category">{item.category}</span>
                    </td>
                    <td>{item.departmentName || 'Triage Queue'}</td>
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
                      {item.status === 'RESOLVED' ? (
                        <Link
                          to={`/complaints/track/${item.trackingNumber || item.complaintNumber}`}
                          className="btn btn-xs btn-success"
                          data-testid={`rate-resolution-${item.trackingNumber || item.complaintNumber}`}
                        >
                          ⭐ Rate & Close
                        </Link>
                      ) : (
                        <Link
                          to={`/complaints/track/${item.trackingNumber || item.complaintNumber}`}
                          className="btn btn-xs btn-secondary"
                        >
                          🔍 Track
                        </Link>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}

export default CitizenDashboard
