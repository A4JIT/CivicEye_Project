import { useState, useEffect } from 'react'
import DashboardLayout from '../../components/layout/DashboardLayout'
import adminService from '../../services/adminService'
import Loader from '../../components/common/Loader'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Button from '../../components/common/Button'
import ErrorMessage from '../../components/common/ErrorMessage'

const DEFAULT_DEPARTMENTS = [
  { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
  { id: 2, name: 'Electricity & Street Lighting', code: 'ELECTRICITY' },
  { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
  { id: 4, name: 'Sanitation & Waste Management', code: 'SOLID_WASTE' },
  { id: 5, name: 'Public Health & Vector Control', code: 'HEALTH' },
]

const DepartmentStaffManagement = () => {
  const [staffList, setStaffList] = useState([])
  const [departments, setDepartments] = useState(DEFAULT_DEPARTMENTS)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedDeptFilter, setSelectedDeptFilter] = useState('ALL')
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [notification, setNotification] = useState({ message: '', type: 'info' })

  // Form State
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    departmentId: '1',
  })
  const [formErrors, setFormErrors] = useState({})
  const [formSubmitError, setFormSubmitError] = useState('')

  const fetchStaffAndDepts = async () => {
    try {
      const [staffData, deptsData] = await Promise.all([
        adminService.getDepartmentStaff(),
        adminService.getDepartments(),
      ])
      if (staffData) setStaffList(staffData)
      if (deptsData && deptsData.length > 0) setDepartments(deptsData)
    } catch (err) {
      console.error('Failed to load department staff', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStaffAndDepts()
  }, [])

  const validateForm = () => {
    const errs = {}
    if (!formData.fullName.trim()) errs.fullName = 'Full name is required'
    if (!formData.username.trim()) {
      errs.username = 'Username is required'
    } else if (formData.username.trim().length < 3) {
      errs.username = 'Username must be at least 3 characters'
    }
    if (!formData.email.trim()) {
      errs.email = 'Email address is required'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errs.email = 'Please provide a valid email address'
    }
    if (!formData.departmentId) errs.departmentId = 'Department selection is required'
    if (!formData.password) {
      errs.password = 'Password is required'
    } else if (formData.password.length < 6) {
      errs.password = 'Password must be at least 6 characters'
    }
    if (!formData.confirmPassword) {
      errs.confirmPassword = 'Confirm password is required'
    } else if (formData.password !== formData.confirmPassword) {
      errs.confirmPassword = 'Passwords do not match'
    }
    setFormErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (formErrors[name]) {
      setFormErrors((prev) => ({ ...prev, [name]: '' }))
    }
    if (formSubmitError) setFormSubmitError('')
  }

  const handleCreateStaff = async (e) => {
    e.preventDefault()
    setFormSubmitError('')

    if (!validateForm()) return

    setCreating(true)
    try {
      const payload = {
        fullName: formData.fullName.trim(),
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
        departmentId: Number(formData.departmentId),
      }

      const created = await adminService.createDepartmentStaff(payload)
      setStaffList((prev) => [created, ...prev])
      setNotification({
        message: `Department officer ${created.fullName} (${created.username}) created successfully!`,
        type: 'success',
      })
      setIsModalOpen(false)
      setFormData({
        fullName: '',
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        departmentId: departments[0]?.id ? String(departments[0].id) : '1',
      })
    } catch (err) {
      setFormSubmitError(
        err.response?.data?.message || err.message || 'Failed to create department staff account'
      )
    } finally {
      setCreating(false)
    }
  }

  const handleToggleStatus = async (staffId, currentStatus) => {
    try {
      const updated = await adminService.toggleStaffStatus(staffId)
      setStaffList((prev) =>
        prev.map((s) => (s.id === staffId ? { ...s, isActive: updated.isActive ?? !currentStatus } : s))
      )
      setNotification({
        message: `Staff status updated to ${!currentStatus ? 'Active' : 'Inactive'}`,
        type: 'info',
      })
    } catch (err) {
      setNotification({
        message: err.message || 'Failed to update staff status',
        type: 'error',
      })
    }
  }

  const filteredStaff = staffList.filter((staff) => {
    const matchesSearch =
      staff.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      staff.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      staff.email?.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesDept =
      selectedDeptFilter === 'ALL' || String(staff.departmentId) === String(selectedDeptFilter)
    return matchesSearch && matchesDept
  })

  const totalStaff = staffList.length
  const activeStaff = staffList.filter((s) => s.isActive).length
  const coveredDepts = new Set(staffList.map((s) => s.departmentId)).size

  const getInitials = (name) => {
    if (!name) return 'O'
    return name
      .split(' ')
      .map((part) => part[0])
      .join('')
      .substring(0, 2)
      .toUpperCase()
  }

  if (loading) {
    return (
      <DashboardLayout
        title="Department Staff Management"
        subtitle="Loading departmental officers and municipal personnel..."
      >
        <div className="dashboard-loading-box">
          <Loader size="lg" message="Loading department staff personnel records..." />
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout
      title="Municipal Department Staff Management"
      subtitle="Create and manage departmental officer accounts authorized to resolve citizen grievances."
    >
      {/* Top Banner KPI Summary */}
      <div className="stats-grid" style={{ marginBottom: '24px' }}>
        <div className="stat-card">
          <div className="stat-card-header">
            <span className="stat-label">Total Department Officers</span>
            <span className="stat-icon">👥</span>
          </div>
          <div className="stat-value">{totalStaff}</div>
          <div className="stat-footer text-muted">Authorized resolution staff</div>
        </div>

        <div className="stat-card primary-accent">
          <div className="stat-card-header">
            <span className="stat-label">Active Officers</span>
            <span className="stat-icon">⚡</span>
          </div>
          <div className="stat-value">{activeStaff}</div>
          <div className="stat-footer text-primary">Currently on active duty</div>
        </div>

        <div className="stat-card warning-accent">
          <div className="stat-card-header">
            <span className="stat-label">Departments Equipped</span>
            <span className="stat-icon">🏢</span>
          </div>
          <div className="stat-value">{coveredDepts} / {departments.length}</div>
          <div className="stat-footer text-warning">Municipal units staffed</div>
        </div>
      </div>

      {notification.message && (
        <div style={{ marginBottom: '16px' }}>
          <ErrorMessage
            message={notification.message}
            type={notification.type}
            onDismiss={() => setNotification({ message: '', type: 'info' })}
          />
        </div>
      )}

      {/* Action and Filter Toolbar */}
      <div
        className="analytics-panel"
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: '16px',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '20px',
          padding: '16px 20px',
        }}
      >
        <div style={{ display: 'flex', gap: '12px', flex: '1 1 300px', alignItems: 'center' }}>
          <div style={{ position: 'relative', flex: '1 1 200px' }}>
            <input
              type="text"
              placeholder="Search officer name, username, or email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="input-field"
              style={{
                width: '100%',
                padding: '10px 14px',
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid rgba(255, 255, 255, 0.15)',
                borderRadius: '8px',
                color: '#fff',
                fontSize: '0.9rem',
              }}
              data-testid="staff-search-input"
            />
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              value={selectedDeptFilter}
              onChange={(e) => setSelectedDeptFilter(e.target.value)}
              className="select-field"
              style={{
                width: '100%',
                padding: '10px 14px',
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid rgba(255, 255, 255, 0.15)',
                borderRadius: '8px',
                color: '#fff',
                fontSize: '0.9rem',
              }}
              data-testid="staff-dept-filter"
            >
              <option value="ALL">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={String(d.id)}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setIsModalOpen(true)}
            data-testid="btn-add-staff"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '8px',
              fontWeight: 600,
              boxShadow: '0 4px 14px rgba(37, 99, 235, 0.35)',
            }}
          >
            <span>➕</span>
            <span>Create Department Staff</span>
          </button>
        </div>
      </div>

      {/* Staff Table */}
      <div className="dashboard-section" data-testid="department-staff-section">
        <div className="section-header">
          <div>
            <h3 className="section-title">Departmental Personnel Directory</h3>
            <p className="section-subtitle">
              Authorized municipal officers assigned to departmental resolution queues
            </p>
          </div>
          <span className="badge badge-info">{filteredStaff.length} Records</span>
        </div>

        <div className="table-responsive">
          <table className="custom-table" data-testid="staff-table">
            <thead>
              <tr>
                <th>Officer</th>
                <th>Assigned Department</th>
                <th>Official Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredStaff.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '36px', color: '#94a3b8' }}>
                    No department staff members found matching your search.
                  </td>
                </tr>
              ) : (
                filteredStaff.map((staff) => (
                  <tr key={staff.id} data-testid={`staff-row-${staff.id}`}>
                    <td className="title-cell">
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div
                          style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: '50%',
                            background: 'linear-gradient(135deg, #0284c7 0%, #2563eb 100%)',
                            color: '#fff',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 700,
                            fontSize: '0.85rem',
                          }}
                        >
                          {getInitials(staff.fullName || staff.username)}
                        </div>
                        <div>
                          <div style={{ fontWeight: 600, color: '#f8fafc' }}>
                            {staff.fullName || staff.username}
                          </div>
                          <div style={{ fontSize: '0.8rem', color: '#94a3b8' }}>
                            @{staff.username}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span
                        className="badge"
                        style={{
                          background: 'rgba(56, 189, 248, 0.15)',
                          color: '#38bdf8',
                          border: '1px solid rgba(56, 189, 248, 0.3)',
                          fontWeight: 600,
                          padding: '4px 8px',
                          borderRadius: '6px',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '6px',
                        }}
                      >
                        🏛️ {staff.departmentName || 'Assigned Department'}
                        {staff.departmentCode && (
                          <span style={{ opacity: 0.7, fontSize: '0.75rem' }}>({staff.departmentCode})</span>
                        )}
                      </span>
                    </td>
                    <td style={{ color: '#cbd5e1' }}>{staff.email}</td>
                    <td>
                      <span className="badge badge-category">DEPARTMENT OFFICER</span>
                    </td>
                    <td>
                      <span
                        className={`badge ${staff.isActive ? 'badge-status status-resolved' : 'badge-status status-rejected'}`}
                        style={{ fontWeight: 600 }}
                      >
                        {staff.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        onClick={() => handleToggleStatus(staff.id, staff.isActive)}
                        className={`btn btn-sm ${staff.isActive ? 'btn-secondary' : 'btn-primary'}`}
                        style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                        data-testid={`btn-toggle-status-${staff.id}`}
                      >
                        {staff.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create Department Staff Modal */}
      {isModalOpen && (
        <div
          className="modal-backdrop"
          role="dialog"
          aria-modal="true"
          onClick={() => setIsModalOpen(false)}
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
              padding: '24px',
              maxWidth: '560px',
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
                paddingBottom: '12px',
              }}
            >
              <div>
                <h3 style={{ margin: 0, color: '#f8fafc', fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span>🏛️</span>
                  <span>Create Department Staff Account</span>
                </h3>
                <p style={{ margin: '4px 0 0', fontSize: '0.85rem', color: '#94a3b8' }}>
                  Authorize a staff officer to manage issues for a specific municipal department.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
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

            {formSubmitError && (
              <div style={{ marginBottom: '14px' }}>
                <ErrorMessage message={formSubmitError} onDismiss={() => setFormSubmitError('')} />
              </div>
            )}

            <form onSubmit={handleCreateStaff} noValidate>
              <Select
                id="staff-department"
                name="departmentId"
                label="Assign Municipal Department"
                value={formData.departmentId}
                onChange={handleInputChange}
                options={departments.map((d) => ({
                  value: String(d.id),
                  label: `${d.name} (${d.code})`,
                }))}
                error={formErrors.departmentId}
                required
              />

              <Input
                id="staff-fullName"
                name="fullName"
                label="Officer Full Name"
                type="text"
                value={formData.fullName}
                onChange={handleInputChange}
                placeholder="e.g. Sunil Patil"
                error={formErrors.fullName}
                required
              />

              <Input
                id="staff-username"
                name="username"
                label="Officer Username"
                type="text"
                value={formData.username}
                onChange={handleInputChange}
                placeholder="e.g. officer_patil"
                error={formErrors.username}
                required
              />

              <Input
                id="staff-email"
                name="email"
                label="Official Email Address"
                type="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="e.g. patil@pwd.gov.in"
                error={formErrors.email}
                required
              />

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <Input
                  id="staff-password"
                  name="password"
                  label="Initial Password"
                  type="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="••••••••"
                  error={formErrors.password}
                  required
                />

                <Input
                  id="staff-confirmPassword"
                  name="confirmPassword"
                  label="Confirm Password"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  placeholder="••••••••"
                  error={formErrors.confirmPassword}
                  required
                />
              </div>

              <div
                style={{
                  marginTop: '20px',
                  display: 'flex',
                  justifyContent: 'flex-end',
                  gap: '12px',
                }}
              >
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setIsModalOpen(false)}
                  disabled={creating}
                >
                  Cancel
                </button>
                <Button
                  type="submit"
                  variant="primary"
                  loading={creating}
                  data-testid="btn-submit-create-staff"
                >
                  Create Staff Account
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}

export default DepartmentStaffManagement
