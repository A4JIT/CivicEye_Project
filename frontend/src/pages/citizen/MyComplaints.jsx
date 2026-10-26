import { useState, useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import { complaintService } from '../../services/complaintService'
import { useAuth } from '../../context/AuthContext'
import Loader from '../../components/common/Loader'
import AuthenticatedImage from '../../components/common/AuthenticatedImage'
import ErrorMessage from '../../components/common/ErrorMessage'

const CATEGORY_MAP = {
  ROADS_AND_FOOTPATHS: { label: 'Roads & Footpaths', icon: '🕳️' },
  GARBAGE_AND_CLEANLINESS: { label: 'Garbage & Waste', icon: '🗑️' },
  WATER_SUPPLY_AND_LEAKAGE: { label: 'Water Supply', icon: '🚰' },
  DRAINAGE_AND_SEWAGE: { label: 'Drainage & Sewage', icon: '🌊' },
  STREETLIGHTS_AND_ELECTRICITY: { label: 'Streetlights & Power', icon: '💡' },
  PUBLIC_HEALTH_HAZARDS: { label: 'Public Health', icon: '☣️' },
  OTHER: { label: 'Other Civic Defect', icon: '⚠️' },
}

const getCategoryMeta = (catKey) => {
  if (!catKey) return { label: 'Civic Defect', icon: '📍' }
  return CATEGORY_MAP[catKey] || { label: catKey.replace(/_/g, ' '), icon: '📌' }
}

const MyComplaints = () => {
  const { user } = useAuth()
  const [complaints, setComplaints] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Filtering, Searching & View Mode
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [categoryFilter, setCategoryFilter] = useState('ALL')
  const [priorityFilter, setPriorityFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('NEWEST')
  const [viewMode, setViewMode] = useState('CARDS') // 'CARDS' or 'TABLE'

  // Requirement: First show 5 complaints, and provide button to see rest
  const [visibleCount, setVisibleCount] = useState(5)

  // Photo modal preview
  const [previewImage, setPreviewImage] = useState(null)
  const [copiedId, setCopiedId] = useState(null)

  useEffect(() => {
    let isMounted = true
    const fetchComplaints = async () => {
      try {
        setLoading(true)
        setError(null)
        const response = await complaintService.getMyComplaints()
        if (isMounted) {
          if (response && response.data) {
            setComplaints(response.data)
          } else if (Array.isArray(response)) {
            setComplaints(response)
          } else {
            setComplaints([])
          }
        }
      } catch (err) {
        console.error('Failed to load citizen complaints:', err)
        if (isMounted) {
          setError(err.response?.data?.message || err.message || 'Unable to load your complaints.')
        }
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    fetchComplaints()
    return () => {
      isMounted = false
    }
  }, [user?.id])

  const handleCopyId = (number) => {
    navigator.clipboard.writeText(number)
    setCopiedId(number)
    setTimeout(() => setCopiedId(null), 2000)
  }

  // Summary counts for filter chips
  const counts = useMemo(() => {
    const total = complaints.length
    const pending = complaints.filter((c) =>
      ['SUBMITTED', 'UNDER_REVIEW', 'NEEDS_INFORMATION', 'ASSIGNED'].includes(c.status)
    ).length
    const inProgress = complaints.filter((c) => c.status === 'IN_PROGRESS').length
    const resolved = complaints.filter((c) => ['RESOLVED', 'CLOSED'].includes(c.status)).length
    const rejected = complaints.filter((c) => c.status === 'REJECTED').length
    return { total, pending, inProgress, resolved, rejected }
  }, [complaints])

  // Filter and sort complaints
  const filteredComplaints = useMemo(() => {
    return complaints
      .filter((item) => {
        // Status filter
        if (statusFilter !== 'ALL') {
          if (statusFilter === 'PENDING') {
            if (!['SUBMITTED', 'UNDER_REVIEW', 'NEEDS_INFORMATION', 'ASSIGNED'].includes(item.status)) {
              return false
            }
          } else if (statusFilter === 'RESOLVED_GROUP') {
            if (!['RESOLVED', 'CLOSED'].includes(item.status)) {
              return false
            }
          } else if (item.status !== statusFilter) {
            return false
          }
        }

        // Category filter
        if (categoryFilter !== 'ALL' && item.category !== categoryFilter) {
          return false
        }

        // Priority filter
        if (priorityFilter !== 'ALL' && item.priority !== priorityFilter) {
          return false
        }

        // Search query
        if (searchQuery.trim()) {
          const q = searchQuery.toLowerCase()
          const number = (item.complaintNumber || item.trackingNumber || '').toLowerCase()
          const title = (item.title || '').toLowerCase()
          const desc = (item.description || '').toLowerCase()
          const location = (item.location || item.addressText || '').toLowerCase()
          const dept = (item.departmentName || '').toLowerCase()

          return (
            number.includes(q) ||
            title.includes(q) ||
            desc.includes(q) ||
            location.includes(q) ||
            dept.includes(q)
          )
        }

        return true
      })
      .sort((a, b) => {
        if (sortBy === 'NEWEST') {
          return new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
        }
        if (sortBy === 'OLDEST') {
          return new Date(a.createdAt || 0) - new Date(b.createdAt || 0)
        }
        if (sortBy === 'PRIORITY') {
          const weights = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 }
          return (weights[b.priority] || 0) - (weights[a.priority] || 0)
        }
        return 0
      })
  }, [complaints, statusFilter, categoryFilter, priorityFilter, searchQuery, sortBy])

  // Complaints currently visible according to visibleCount limit
  const displayedComplaints = useMemo(() => {
    return filteredComplaints.slice(0, visibleCount)
  }, [filteredComplaints, visibleCount])

  const handleResetFilters = () => {
    setSearchQuery('')
    setStatusFilter('ALL')
    setCategoryFilter('ALL')
    setPriorityFilter('ALL')
    setVisibleCount(5)
  }

  if (loading) {
    return (
      <DashboardLayout
        title="My Submitted Grievances"
        subtitle="Retrieving your complete municipal defect history and repair records..."
      >
        <div className="dashboard-loading-box">
          <Loader size="lg" message="Loading your civic complaints list..." />
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout
      title="My Filed Grievances & Defect Log"
      subtitle="Comprehensive log of all neighborhood defect reports, inspection dispatches, and resolution proof."
    >
      <div className="my-complaints-container" data-testid="my-complaints-page">
        {/* Top Header Controls with Lodge New Defect CTA */}
        <div className="my-complaints-header-actions">
          <div className="header-breadcrumbs">
            <span className="badge badge-accent">Citizen Portal</span>
            <span className="breadcrumb-count">
              <strong>{complaints.length}</strong> Total Grievances Registered
            </span>
          </div>
          <Link to="/complaints/new" className="btn btn-primary" id="btn-lodge-new-defect">
            ➕ Report Another Defect
          </Link>
        </div>

        {error && (
          <div style={{ marginBottom: '1.5rem' }}>
            <ErrorMessage message={error} onDismiss={() => setError(null)} />
          </div>
        )}

        {/* Interactive KPI Quick-Filter Cards */}
        <div className="my-complaints-kpi-grid">
          <button
            type="button"
            className={`my-kpi-card ${statusFilter === 'ALL' ? 'active' : ''}`}
            onClick={() => {
              setStatusFilter('ALL')
              setVisibleCount(5)
            }}
          >
            <div className="my-kpi-top">
              <span className="my-kpi-label">All Submissions</span>
              <span className="my-kpi-icon">📋</span>
            </div>
            <div className="my-kpi-value">{counts.total}</div>
            <div className="my-kpi-desc">Every issue filed</div>
          </button>

          <button
            type="button"
            className={`my-kpi-card warning-card ${statusFilter === 'PENDING' ? 'active' : ''}`}
            onClick={() => {
              setStatusFilter('PENDING')
              setVisibleCount(5)
            }}
          >
            <div className="my-kpi-top">
              <span className="my-kpi-label">In Triage / Queue</span>
              <span className="my-kpi-icon">⏳</span>
            </div>
            <div className="my-kpi-value">{counts.pending}</div>
            <div className="my-kpi-desc">Awaiting field dispatch</div>
          </button>

          <button
            type="button"
            className={`my-kpi-card primary-card ${statusFilter === 'IN_PROGRESS' ? 'active' : ''}`}
            onClick={() => {
              setStatusFilter('IN_PROGRESS')
              setVisibleCount(5)
            }}
          >
            <div className="my-kpi-top">
              <span className="my-kpi-label">Field Squads Active</span>
              <span className="my-kpi-icon">⚙️</span>
            </div>
            <div className="my-kpi-value">{counts.inProgress}</div>
            <div className="my-kpi-desc">Crews on-site repairing</div>
          </button>

          <button
            type="button"
            className={`my-kpi-card success-card ${statusFilter === 'RESOLVED_GROUP' ? 'active' : ''}`}
            onClick={() => {
              setStatusFilter('RESOLVED_GROUP')
              setVisibleCount(5)
            }}
          >
            <div className="my-kpi-top">
              <span className="my-kpi-label">Resolved & Closed</span>
              <span className="my-kpi-icon">✅</span>
            </div>
            <div className="my-kpi-value">{counts.resolved}</div>
            <div className="my-kpi-desc">Repairs completed</div>
          </button>
        </div>

        {/* Toolbar: Search, Filters & View Mode */}
        <div className="my-complaints-toolbar">
          <div className="toolbar-search-wrapper">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              className="toolbar-search-input"
              placeholder="Search by ID, keywords, street, or department..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value)
                setVisibleCount(5)
              }}
              data-testid="input-search-complaints"
            />
            {searchQuery && (
              <button
                type="button"
                className="toolbar-clear-btn"
                onClick={() => {
                  setSearchQuery('')
                  setVisibleCount(5)
                }}
              >
                ✕
              </button>
            )}
          </div>

          <div className="toolbar-filters-row">
            <select
              className="toolbar-select"
              value={categoryFilter}
              onChange={(e) => {
                setCategoryFilter(e.target.value)
                setVisibleCount(5)
              }}
              aria-label="Filter by Category"
            >
              <option value="ALL">All Categories</option>
              <option value="ROADS_AND_FOOTPATHS">🕳️ Roads & Footpaths</option>
              <option value="GARBAGE_AND_CLEANLINESS">🗑️ Garbage & Cleanliness</option>
              <option value="WATER_SUPPLY_AND_LEAKAGE">🚰 Water Supply & Leakage</option>
              <option value="DRAINAGE_AND_SEWAGE">🌊 Drainage & Sewage</option>
              <option value="STREETLIGHTS_AND_ELECTRICITY">💡 Streetlights & Electricity</option>
              <option value="PUBLIC_HEALTH_HAZARDS">☣️ Public Health Hazards</option>
              <option value="OTHER">⚠️ Other Civic Defect</option>
            </select>

            <select
              className="toolbar-select"
              value={priorityFilter}
              onChange={(e) => {
                setPriorityFilter(e.target.value)
                setVisibleCount(5)
              }}
              aria-label="Filter by Priority"
            >
              <option value="ALL">All Priorities</option>
              <option value="CRITICAL">🔥 Critical Urgency</option>
              <option value="HIGH">⚡ High Priority</option>
              <option value="MEDIUM">⚖️ Medium Priority</option>
              <option value="LOW">🌱 Low Priority</option>
            </select>

            <select
              className="toolbar-select"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              aria-label="Sort Complaints"
            >
              <option value="NEWEST">🕒 Newest First</option>
              <option value="OLDEST">⏳ Oldest First</option>
              <option value="PRIORITY">🔥 Highest Priority</option>
            </select>

            <div className="view-mode-toggle-group">
              <button
                type="button"
                className={`btn-view-toggle ${viewMode === 'CARDS' ? 'active' : ''}`}
                onClick={() => setViewMode('CARDS')}
                title="Switch to Card Grid View"
                data-testid="toggle-view-cards"
              >
                🎴 Cards
              </button>
              <button
                type="button"
                className={`btn-view-toggle ${viewMode === 'TABLE' ? 'active' : ''}`}
                onClick={() => setViewMode('TABLE')}
                title="Switch to Tabular View"
                data-testid="toggle-view-table"
              >
                📋 Table
              </button>
            </div>
          </div>
        </div>

        {/* Complaints Listing */}
        {complaints.length === 0 ? (
          <div className="empty-state-card" data-testid="empty-all-complaints">
            <div className="empty-icon">🌱</div>
            <h3>No Complaints Filed Yet</h3>
            <p>You haven't reported any civic defects yet. Spot an issue in your area and report it!</p>
            <Link to="/complaints/new" className="btn btn-primary" style={{ marginTop: '1rem' }}>
              📸 Report Your First Defect
            </Link>
          </div>
        ) : filteredComplaints.length === 0 ? (
          <div className="empty-state-card" data-testid="empty-filtered-complaints">
            <div className="empty-icon">🔎</div>
            <h3>No Complaints Found</h3>
            <p>No civic grievances matched your active filters or search keyword.</p>
            <button
              type="button"
              className="btn btn-outline"
              style={{ marginTop: '1rem' }}
              onClick={handleResetFilters}
            >
              🔄 Reset All Filters
            </button>
          </div>
        ) : (
          <>
            {/* View Mode 1: Rich Cards Grid */}
            {viewMode === 'CARDS' ? (
              <div className="my-complaints-cards-grid" data-testid="complaints-card-grid">
                {displayedComplaints.map((item) => {
                  const number = item.complaintNumber || item.trackingNumber
                  const catMeta = getCategoryMeta(item.category)
                  const isResolved = item.status === 'RESOLVED' || item.status === 'CLOSED'

                  return (
                    <div
                      key={item.id || number}
                      className={`complaint-grievance-card ${
                        isResolved ? 'resolved-card' : ''
                      }`}
                      data-testid={`complaint-card-${item.id || number}`}
                    >
                      {/* Card Header */}
                      <div className="grievance-card-header">
                        <div className="tracking-badge-group">
                          <span className="tracking-badge">{number}</span>
                          <button
                            type="button"
                            className="btn-copy-chip"
                            onClick={() => handleCopyId(number)}
                            title="Copy Tracking ID"
                          >
                            {copiedId === number ? '✓' : '📋'}
                          </button>
                        </div>
                        <div className="header-status-pills">
                          <span className={`badge badge-priority priority-${item.priority?.toLowerCase()}`}>
                            {item.priority}
                          </span>
                          <span className={`badge badge-status status-${item.status?.toLowerCase()}`}>
                            {item.status?.replace('_', ' ')}
                          </span>
                        </div>
                      </div>

                      {/* Card Body */}
                      <div className="grievance-card-body">
                        <h4 className="grievance-title">{item.title}</h4>
                        {item.description && (
                          <p className="grievance-description">{item.description}</p>
                        )}

                        <div className="grievance-meta-tags">
                          <span className="meta-tag category-meta">
                            <span className="meta-icon">{catMeta.icon}</span>
                            <span>{catMeta.label}</span>
                          </span>
                          <span className="meta-tag department-meta">
                            🏢 {item.departmentName || 'Triage Assigned'}
                          </span>
                          {(item.location || item.addressText) && (
                            <span className="meta-tag location-meta">
                              📍 {item.location || item.addressText}
                            </span>
                          )}
                        </div>

                        {/* Defect Photo Preview Button */}
                        {item.imageUrl && (
                          <div className="grievance-photo-row">
                            <button
                              type="button"
                              className="btn-view-evidence-photo"
                              onClick={() =>
                                setPreviewImage({
                                  url: item.imageUrl,
                                  title: item.title,
                                  complaintNumber: number,
                                })
                              }
                            >
                              📸 Defect Photo Proof
                            </button>
                          </div>
                        )}

                        {/* Resolution note if closed/resolved */}
                        {item.resolutionNotes && (
                          <div className="grievance-resolution-note">
                            <strong>✓ Crew Resolution:</strong> {item.resolutionNotes}
                          </div>
                        )}
                      </div>

                      {/* Card Footer */}
                      <div className="grievance-card-footer">
                        <span className="grievance-date">
                          📅 {new Date(item.createdAt || Date.now()).toLocaleDateString(undefined, {
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                          })}
                        </span>
                        <div className="grievance-footer-actions">
                          {item.status === 'RESOLVED' && (
                            <Link
                              to={`/complaints/track/${number}`}
                              className="btn btn-sm btn-success"
                            >
                              ⭐ Rate Resolution
                            </Link>
                          )}
                          <Link
                            to={`/complaints/track/${number}`}
                            className="btn btn-sm btn-outline"
                          >
                            🛰️ Live Track ↗
                          </Link>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            ) : (
              /* View Mode 2: Responsive Tabular Grid */
              <div className="table-responsive my-complaints-table-wrapper">
                <table className="custom-table" data-testid="citizen-complaints-table">
                  <thead>
                    <tr>
                      <th>Tracking ID</th>
                      <th>Title & Location</th>
                      <th>Category</th>
                      <th>Department</th>
                      <th>Priority</th>
                      <th>Status</th>
                      <th>Submitted</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {displayedComplaints.map((item) => {
                      const number = item.complaintNumber || item.trackingNumber
                      const catMeta = getCategoryMeta(item.category)
                      return (
                        <tr key={item.id || number}>
                          <td className="tracking-cell">
                            <Link to={`/complaints/track/${number}`} className="tracking-code-link">
                              <span className="tracking-code">{number}</span>
                            </Link>
                          </td>
                          <td className="title-cell">
                            <div className="complaint-title-text">{item.title}</div>
                            {(item.location || item.addressText) && (
                              <div className="complaint-location">
                                📍 {item.location || item.addressText}
                              </div>
                            )}
                            {item.imageUrl && (
                              <button
                                type="button"
                                className="photo-proof-badge"
                                onClick={() =>
                                  setPreviewImage({
                                    url: item.imageUrl,
                                    title: item.title,
                                    complaintNumber: number,
                                  })
                                }
                              >
                                📸 View Photo
                              </button>
                            )}
                          </td>
                          <td>
                            <span className="badge badge-category">
                              {catMeta.icon} {catMeta.label}
                            </span>
                          </td>
                          <td>{item.departmentName || 'Triage Assigned'}</td>
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
                            <span className="text-muted text-sm">
                              {new Date(item.createdAt || Date.now()).toLocaleDateString()}
                            </span>
                          </td>
                          <td>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                              {item.status === 'RESOLVED' && (
                                <Link
                                  to={`/complaints/track/${number}`}
                                  className="btn btn-xs btn-success"
                                >
                                  ⭐ Rate
                                </Link>
                              )}
                              <Link
                                to={`/complaints/track/${number}`}
                                className="btn btn-xs btn-outline"
                              >
                                🛰️ Track
                              </Link>
                            </div>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            )}

            {/* REQUIREMENT: Show first 5 complaints and create a button to see the rest */}
            <div className="complaints-pagination-container" data-testid="pagination-controls">
              <div className="pagination-info">
                Showing <strong>{Math.min(visibleCount, filteredComplaints.length)}</strong> of{' '}
                <strong>{filteredComplaints.length}</strong> complaints
                <div className="pagination-progress-bar">
                  <div
                    className="pagination-progress-fill"
                    style={{
                      width: `${Math.min(
                        100,
                        Math.round((Math.min(visibleCount, filteredComplaints.length) / filteredComplaints.length) * 100)
                      )}%`,
                    }}
                  />
                </div>
              </div>

              <div className="pagination-actions">
                {filteredComplaints.length > visibleCount && (
                  <>
                    <button
                      type="button"
                      className="btn btn-primary btn-load-more"
                      onClick={() => setVisibleCount((prev) => prev + 5)}
                      data-testid="btn-load-more"
                    >
                      📥 Load Next 5 Complaints ({filteredComplaints.length - visibleCount} more)
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline btn-show-all"
                      onClick={() => setVisibleCount(filteredComplaints.length)}
                      data-testid="btn-show-all"
                    >
                      👁️ Show All ({filteredComplaints.length})
                    </button>
                  </>
                )}

                {visibleCount > 5 && (
                  <button
                    type="button"
                    className="btn btn-ghost btn-collapse"
                    onClick={() => setVisibleCount(5)}
                    data-testid="btn-collapse"
                  >
                    ▲ Show First 5 Only
                  </button>
                )}
              </div>
            </div>
          </>
        )}
      </div>

      {/* Modal for Defect Photo Inspection */}
      {previewImage && (
        <div
          className="photo-preview-backdrop"
          onClick={() => setPreviewImage(null)}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.85)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '1rem',
          }}
        >
          <div
            className="photo-preview-card"
            onClick={(e) => e.stopPropagation()}
            style={{
              backgroundColor: '#0f172a',
              border: '1px solid #334155',
              borderRadius: '16px',
              maxWidth: '650px',
              width: '100%',
              overflow: 'hidden',
              boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7)',
            }}
          >
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '1rem 1.25rem',
                borderBottom: '1px solid #334155',
              }}
            >
              <div>
                <strong style={{ color: '#38bdf8' }}>{previewImage.complaintNumber}</strong>
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#94a3b8' }}>{previewImage.title}</p>
              </div>
              <button
                type="button"
                onClick={() => setPreviewImage(null)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: '#94a3b8',
                  fontSize: '1.25rem',
                  cursor: 'pointer',
                }}
              >
                ✕
              </button>
            </div>
            <div
              style={{
                padding: '1rem',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                maxHeight: '65vh',
                overflow: 'hidden',
                backgroundColor: '#020617',
              }}
            >
              <AuthenticatedImage
                imageUrl={previewImage.url}
                alt={previewImage.title}
                style={{
                  maxHeight: '60vh',
                  maxWidth: '100%',
                  objectFit: 'contain',
                  borderRadius: '8px',
                }}
              />
            </div>
            <div
              style={{
                padding: '0.75rem 1.25rem',
                borderTop: '1px solid #334155',
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

export default MyComplaints
