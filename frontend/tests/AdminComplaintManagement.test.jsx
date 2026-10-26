import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import AllComplaints from '../src/pages/admin/AllComplaints'
import ReviewComplaint from '../src/pages/admin/ReviewComplaint'
import adminService from '../src/services/adminService'
import { AuthContext } from '../src/context/AuthContext'

const mockAdminUser = {
  id: 1,
  username: 'admin',
  email: 'admin@civiceye.com',
  fullName: 'Administrator',
  role: 'ADMIN',
}

const renderWithProviders = (ui) => {
  return render(
    <AuthContext.Provider value={{ user: mockAdminUser, logout: vi.fn() }}>
      <BrowserRouter>{ui}</BrowserRouter>
    </AuthContext.Provider>
  )
}

describe('Admin Complaint Management & Triage Suite (Phase 5)', () => {
  const sampleComplaints = [
    {
      id: 1,
      complaintNumber: 'CE-2026-0001',
      title: 'Massive crater pothole on MG Road',
      description: 'Dangerous pothole damaging vehicles and causing traffic jams.',
      category: 'ROADS_AND_FOOTPATHS',
      status: 'SUBMITTED',
      priority: 'HIGH',
      location: 'MG Road Junction',
      citizenName: 'Jane Citizen',
      citizenEmail: 'citizen@civiceye.com',
      departmentId: 1,
      departmentName: 'Public Works & Roads',
      aiCategoryOverridden: false,
      aiPriorityOverridden: false,
      adminNotes: null,
      needsInfoRemarks: null,
      rejectionReason: null,
      imageUrl: null,
      createdAt: '2026-10-12T10:00:00Z',
    },
    {
      id: 2,
      complaintNumber: 'CE-2026-0002',
      title: 'Dark street lighting on 5th Avenue',
      description: 'Streetlamps off since yesterday.',
      category: 'ELECTRICITY',
      status: 'UNDER_REVIEW',
      priority: 'MEDIUM',
      location: '5th Avenue Sector 9',
      citizenName: 'Jane Citizen',
      citizenEmail: 'citizen@civiceye.com',
      departmentId: 2,
      departmentName: 'Electricity & Street Lighting',
      aiCategoryOverridden: false,
      aiPriorityOverridden: false,
      adminNotes: 'Field check scheduled.',
      needsInfoRemarks: null,
      rejectionReason: null,
      imageUrl: null,
      createdAt: '2026-10-11T14:00:00Z',
    },
    {
      id: 3,
      complaintNumber: 'CE-2026-0003',
      title: 'Unclear property dispute',
      description: 'Neighbor argument on parking space.',
      category: 'OTHER',
      status: 'REJECTED',
      priority: 'LOW',
      location: 'Green Avenue Flat 4',
      citizenName: 'Jane Citizen',
      citizenEmail: 'citizen@civiceye.com',
      departmentId: null,
      departmentName: null,
      aiCategoryOverridden: false,
      aiPriorityOverridden: false,
      adminNotes: null,
      needsInfoRemarks: null,
      rejectionReason: '[CIVIL_DISPUTE] Private property parking matter.',
      imageUrl: null,
      createdAt: '2026-10-10T12:00:00Z',
    },
  ]

  const sampleDepartments = [
    { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
    { id: 2, name: 'Electricity & Street Lighting', code: 'ELECTRICITY' },
    { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
  ]

  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(adminService, 'getDepartments').mockResolvedValue(sampleDepartments)
    vi.spyOn(adminService, 'getAllComplaints').mockResolvedValue({
      complaints: sampleComplaints,
      totalItems: sampleComplaints.length,
      totalPages: 1,
      currentPage: 0,
    })
  })

  it('renders Admin Triage Console with table, tabs, and search bar', async () => {
    renderWithProviders(<AllComplaints />)

    expect(screen.getByText('Admin Grievance Triage Console')).toBeDefined()
    await waitFor(() => {
      expect(screen.getByTestId('triage-complaints-table')).toBeDefined()
    })

    expect(screen.getByText('Massive crater pothole on MG Road')).toBeDefined()
    expect(screen.getByText('Dark street lighting on 5th Avenue')).toBeDefined()
    expect(screen.getByTestId('triage-search-input')).toBeDefined()
    expect(screen.getByTestId('status-filter-tabs')).toBeDefined()
  })

  it('filters complaints by status tab when clicked', async () => {
    renderWithProviders(<AllComplaints />)

    await waitFor(() => {
      expect(screen.getByText('Massive crater pothole on MG Road')).toBeDefined()
    })

    // Click "Pending Review" (SUBMITTED)
    const pendingTab = screen.getByTestId('tab-submitted')
    fireEvent.click(pendingTab)

    expect(screen.getByText('Massive crater pothole on MG Road')).toBeDefined()
    expect(screen.queryByText('Dark street lighting on 5th Avenue')).toBeNull()
    expect(screen.queryByText('Unclear property dispute')).toBeNull()

    // Click "Rejected" tab
    const rejectedTab = screen.getByTestId('tab-rejected')
    fireEvent.click(rejectedTab)

    expect(screen.getByText('Unclear property dispute')).toBeDefined()
    expect(screen.queryByText('Massive crater pothole on MG Road')).toBeNull()
  })

  it('searches complaints by keyword in search input', async () => {
    renderWithProviders(<AllComplaints />)

    await waitFor(() => {
      expect(screen.getByText('Massive crater pothole on MG Road')).toBeDefined()
    })

    const searchInput = screen.getByTestId('triage-search-input')
    fireEvent.change(searchInput, { target: { value: 'pothole' } })

    expect(screen.getByText('Massive crater pothole on MG Road')).toBeDefined()
    expect(screen.queryByText('Dark street lighting on 5th Avenue')).toBeNull()
  })

  it('opens Review & Triage drawer when clicking Review button', async () => {
    renderWithProviders(<AllComplaints />)

    await waitFor(() => {
      expect(screen.getByTestId('action-review-1')).toBeDefined()
    })

    fireEvent.click(screen.getByTestId('action-review-1'))

    await waitFor(() => {
      expect(screen.getByTestId('triage-review-container')).toBeDefined()
      expect(screen.getByTestId('ai-recommendation-panel')).toBeDefined()
      expect(screen.getByTestId('btn-save-review')).toBeDefined()
    })
  })

  it('opens Quick Reject modal and submits rejection reason', async () => {
    const rejectSpy = vi.spyOn(adminService, 'rejectComplaint').mockResolvedValue({
      data: {
        ...sampleComplaints[0],
        status: 'REJECTED',
        rejectionReason: '[INVALID] Private land area not maintained by municipality.',
      },
    })

    renderWithProviders(<AllComplaints />)

    await waitFor(() => {
      expect(screen.getByTestId('action-reject-1')).toBeDefined()
    })

    fireEvent.click(screen.getByTestId('action-reject-1'))

    await waitFor(() => {
      expect(screen.getByTestId('textarea-quick-reject-reason')).toBeDefined()
    })

    const reasonInput = screen.getByTestId('textarea-quick-reject-reason')
    fireEvent.change(reasonInput, {
      target: { value: 'Private land area not maintained by municipality.' },
    })

    fireEvent.click(screen.getByTestId('btn-confirm-quick-reject'))

    await waitFor(() => {
      expect(rejectSpy).toHaveBeenCalledWith(1, {
        rejectionReason: 'Private land area not maintained by municipality.',
        rejectionCategory: 'INVALID',
      })
    })
  })

  it('does not display Information Needed option in admin triage actions', async () => {
    renderWithProviders(<AllComplaints />)

    await waitFor(() => {
      expect(screen.getByTestId('action-assign-1')).toBeDefined()
    })

    expect(screen.queryByTestId('action-info-1')).toBeNull()
    expect(screen.queryByText(/Information Needed/i)).toBeNull()
  })

  it('opens Quick Assign modal and dispatches to department', async () => {
    const assignSpy = vi.spyOn(adminService, 'assignDepartment').mockResolvedValue({
      data: {
        ...sampleComplaints[0],
        status: 'ASSIGNED',
        departmentId: 2,
        departmentName: 'Electricity & Street Lighting',
        priority: 'HIGH',
      },
    })

    renderWithProviders(<AllComplaints />)

    await waitFor(() => {
      expect(screen.getByTestId('action-assign-1')).toBeDefined()
    })

    fireEvent.click(screen.getByTestId('action-assign-1'))

    await waitFor(() => {
      expect(screen.getByTestId('btn-confirm-quick-assign')).toBeDefined()
    })

    fireEvent.click(screen.getByTestId('btn-confirm-quick-assign'))

    await waitFor(() => {
      expect(assignSpy).toHaveBeenCalled()
    })
  })

  it('ReviewComplaint component saves review notes and AI category/priority overrides', async () => {
    const reviewSpy = vi.spyOn(adminService, 'reviewComplaint').mockResolvedValue({
      data: {
        ...sampleComplaints[0],
        status: 'UNDER_REVIEW',
        priority: 'CRITICAL',
        adminNotes: 'Confirmed severe cavity by site officer',
      },
    })

    renderWithProviders(<ReviewComplaint complaintProp={sampleComplaints[0]} />)

    await waitFor(() => {
      expect(screen.getByTestId('select-override-priority')).toBeDefined()
    })

    fireEvent.change(screen.getByTestId('select-override-priority'), {
      target: { value: 'CRITICAL' },
    })

    fireEvent.change(screen.getByTestId('textarea-admin-notes'), {
      target: { value: 'Confirmed severe cavity by site officer' },
    })

    fireEvent.click(screen.getByTestId('btn-save-review'))

    await waitFor(() => {
      expect(reviewSpy).toHaveBeenCalledWith(1, {
        notes: 'Confirmed severe cavity by site officer',
        category: 'ROADS_AND_FOOTPATHS',
        priority: 'CRITICAL',
      })
    })
  })
})
