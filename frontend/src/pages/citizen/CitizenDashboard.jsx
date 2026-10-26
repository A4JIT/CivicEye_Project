import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import dashboardService from '../../services/dashboardService'
import { useAuth } from '../../context/AuthContext'
import Loader from '../../components/common/Loader'
import AuthenticatedImage from '../../components/common/AuthenticatedImage'

const CitizenDashboard = () => {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [previewImage, setPreviewImage] = useState(null)

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

  const [visibleCount, setVisibleCount] = useState(5)

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
  const displayedComplaints = complaints.slice(0, visibleCount)
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
                {displayedComplaints.map((item) => (
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
                      {item.imageUrl && (
                        <div style={{ marginTop: '4px' }}>
                          <button
                            type="button"
                            className="photo-proof-badge"
                            title="Click to view submitted defect photo"
                            onClick={() =>
                              setPreviewImage({
                                url: item.imageUrl,
                                title: item.title,
                                complaintNumber: item.trackingNumber || item.complaintNumber,
                              })
                            }
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
                            📸 View Defect Photo
                          </button>
                        </div>
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

            {/* Dashboard Table Footer with Pagination / Show Rest */}
            <div
              className="dashboard-table-footer"
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '1rem',
                marginTop: '1.25rem',
                paddingTop: '1rem',
                borderTop: '1px solid rgba(255, 255, 255, 0.06)',
              }}
            >
              <div style={{ fontSize: '0.88rem', color: 'var(--text-muted)' }}>
                Showing <strong>{Math.min(visibleCount, complaints.length)}</strong> of <strong>{complaints.length}</strong> recent reports
              </div>
              <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
                {complaints.length > visibleCount && (
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => setVisibleCount((prev) => prev + 5)}
                    data-testid="btn-show-rest-dashboard"
                  >
                    📥 Show Next 5 ({complaints.length - visibleCount} more)
                  </button>
                )}
                <Link to="/complaints/my" className="btn btn-outline btn-sm" id="btn-see-all-complaints">
                  📋 View All ({data?.totalComplaints || complaints.length}) in Grievances Workspace →
                </Link>
              </div>
            </div>
          </div>
        )}
      </div>
      {/* Lightbox / Preview Modal for Table photo click */}
      {previewImage && (
        <div
          className="modal-backdrop"
          role="dialog"
          aria-modal="true"
          onClick={() => setPreviewImage(null)}
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '20px',
          }}
        >
          <div
            className="modal-container"
            onClick={(e) => e.stopPropagation()}
            style={{
              background: '#0f172a',
              border: '1px solid rgba(56, 189, 248, 0.3)',
              borderRadius: '12px',
              padding: '20px',
              maxWidth: '650px',
              width: '100%',
              boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7)',
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '16px',
                borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                paddingBottom: '10px',
              }}
            >
              <div>
                <h4 style={{ margin: 0, color: '#f8fafc', fontSize: '1.1rem' }}>
                  📸 Submitted Defect Photo Proof
                </h4>
                <span style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
                  {previewImage.complaintNumber} • {previewImage.title}
                </span>
              </div>
              <button
                type="button"
                onClick={() => setPreviewImage(null)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#94a3b8',
                  fontSize: '1.5rem',
                  cursor: 'pointer',
                  padding: '4px 8px',
                }}
              >
                ✕
              </button>
            </div>
            <div style={{ textAlign: 'center', minHeight: '260px' }}>
              <AuthenticatedImage
                src={previewImage.url}
                alt="Defect Photo Proof"
                style={{
                  maxWidth: '100%',
                  maxHeight: '450px',
                  borderRadius: '8px',
                  objectFit: 'contain',
                }}
                allowZoom={true}
              />
            </div>
            <div
              style={{
                marginTop: '16px',
                display: 'flex',
                justifyContent: 'flex-end',
              }}
            >
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setPreviewImage(null)}
              >
                Close Preview
              </button>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}

export default CitizenDashboard
