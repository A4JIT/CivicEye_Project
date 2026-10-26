import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import dashboardService from '../../services/dashboardService'
import Loader from '../../components/common/Loader'
import AuthenticatedImage from '../../components/common/AuthenticatedImage'

const AdminDashboard = () => {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [previewImage, setPreviewImage] = useState(null)

  useEffect(() => {
    let isMounted = true
    const fetchData = async () => {
      try {
        const result = await dashboardService.getAdminDashboard()
        if (isMounted) setData(result)
      } catch (err) {
        console.error('Failed to load admin metrics', err)
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
      <DashboardLayout title="System Command Center" subtitle="Aggregating municipal telemetry...">
        <div className="dashboard-loading-box">
          <Loader size="lg" message="Loading citywide operations and department workloads..." />
        </div>
      </DashboardLayout>
    )
  }

  const deptMap = data?.complaintsByDepartment || {}
  const total = data?.totalComplaints || 1
  const recentComplaints = data?.recentComplaints || []

  return (
    <DashboardLayout
      title="City Operations Command Center"
      subtitle="Live oversight of municipal grievance dispatches, department workloads, and resolution performance."
    >
      {/* Top System Health Bar */}
      <div className="admin-status-banner">
        <div className="status-item">
          <span className="dot dot-green"></span>
          <span>AI Categorization Engine: <strong>Operational (99.4%)</strong></span>
        </div>
        <div className="status-item">
          <span className="dot dot-blue"></span>
          <span>Active Departments: <strong>{data?.totalDepartments || 4} Municipal Units</strong></span>
        </div>
        <div className="status-item">
          <span className="dot dot-amber"></span>
          <span>Registered Citizens: <strong>{data?.totalCitizens || 0} Accounts</strong></span>
        </div>
        <Link
          to="/admin/staff"
          className="status-item"
          style={{ textDecoration: 'none', color: '#38bdf8', fontWeight: 600, marginLeft: 'auto' }}
        >
          <span>👥 Manage Department Staff →</span>
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="stats-grid" data-testid="admin-stats-grid">
        <div className="stat-card">
          <div className="stat-card-header">
            <span className="stat-label">Total Complaints</span>
            <span className="stat-icon">🏛️</span>
          </div>
          <div className="stat-value">{data?.totalComplaints || 0}</div>
          <div className="stat-footer text-muted">Citywide lifetime tickets</div>
        </div>

        <div className="stat-card warning-accent">
          <div className="stat-card-header">
            <span className="stat-label">Pending Triage</span>
            <span className="stat-icon">⏳</span>
          </div>
          <div className="stat-value">{data?.pendingComplaints || 0}</div>
          <div className="stat-footer text-warning">Requires assignment</div>
        </div>

        <div className="stat-card primary-accent">
          <div className="stat-card-header">
            <span className="stat-label">In Progress</span>
            <span className="stat-icon">🚜</span>
          </div>
          <div className="stat-value">{data?.inProgressComplaints || 0}</div>
          <div className="stat-footer text-primary">Field operations active</div>
        </div>

        <div className="stat-card error-accent">
          <div className="stat-card-header">
            <span className="stat-label">Critical Urgent</span>
            <span className="stat-icon">🚨</span>
          </div>
          <div className="stat-value">{data?.criticalComplaints || 0}</div>
          <div className="stat-footer text-danger">Immediate safety hazard</div>
        </div>
      </div>

      {/* Analytics Row: Department Workload + Category Distribution */}
      <div className="analytics-split-row">
        <div className="analytics-panel" data-testid="department-workload-panel">
          <div className="panel-header">
            <h4>Department Workload Distribution</h4>
            <span className="badge badge-info">{Object.keys(deptMap).length} Active</span>
          </div>
          <div className="workload-list">
            {Object.entries(deptMap).map(([dept, count]) => {
              const pct = Math.round((count / total) * 100)
              return (
                <div key={dept} className="workload-item">
                  <div className="workload-info">
                    <span className="dept-name">{dept}</span>
                    <span className="dept-count">{count} issues ({pct}%)</span>
                  </div>
                  <div className="progress-bar-bg">
                    <div className="progress-bar-fill" style={{ width: `${Math.max(pct, 5)}%` }}></div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        <div className="analytics-panel" data-testid="category-distribution-panel">
          <div className="panel-header">
            <h4>Issue Categories Breakdown</h4>
            <span className="badge badge-category">AI Classifications</span>
          </div>
          <div className="category-tags-grid">
            {Object.entries(data?.complaintsByCategory || {}).map(([cat, count]) => (
              <div key={cat} className="category-chip">
                <span className="cat-title">{cat.replace('_', ' ')}</span>
                <span className="cat-badge">{count}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Citywide Live Queue */}
      <div className="dashboard-section" data-testid="admin-complaints-section">
        <div className="section-header">
          <div>
            <h3 className="section-title">Latest Citywide Submissions</h3>
            <p className="section-subtitle">Real-time incoming complaints stream</p>
          </div>
          <Link to="/admin/complaints" className="section-link">
            Open Full Grievance Queue →
          </Link>
        </div>

        <div className="table-responsive">
          <table className="custom-table" data-testid="admin-complaints-table">
            <thead>
              <tr>
                <th>Tracking ID</th>
                <th>Issue Summary</th>
                <th>Citizen</th>
                <th>Department</th>
                <th>Priority</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {recentComplaints.map((item) => (
                <tr key={item.id || item.trackingNumber}>
                  <td className="tracking-cell">
                    <span className="tracking-code">{item.trackingNumber}</span>
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
                          title="Click to view citizen uploaded photo proof"
                          onClick={() =>
                            setPreviewImage({
                              url: item.imageUrl,
                              title: item.title,
                              complaintNumber: item.trackingNumber,
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
                  <td>{item.citizenName || 'Verified Citizen'}</td>
                  <td>{item.departmentName || 'Unassigned'}</td>
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
                </tr>
              ))}
            </tbody>
          </table>
        </div>
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
                  📸 Reported Defect Photo Proof
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

export default AdminDashboard
