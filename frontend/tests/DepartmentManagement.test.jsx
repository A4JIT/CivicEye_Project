import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import DepartmentComplaints from '../src/pages/department/DepartmentComplaints'
import departmentService from '../src/services/departmentService'
import { AuthContext } from '../src/context/AuthContext'

const mockDeptUser = {
  id: 20,
  username: 'officer_sharma',
  fullName: 'Rajesh Sharma',
  email: 'dept_roads@civiceye.com',
  role: 'DEPARTMENT_USER',
  departmentId: 1,
  departmentName: 'Public Works & Roads',
}

const renderWithProviders = (ui) => {
  return render(
    <AuthContext.Provider value={{ user: mockDeptUser, logout: vi.fn() }}>
      <BrowserRouter>{ui}</BrowserRouter>
    </AuthContext.Provider>
  )
}

describe('Department Management & Work Console Suite (Phase 6)', () => {
  const sampleTasks = [
    {
      id: 100,
      complaintNumber: 'CE-2026-0001',
      title: 'Massive crater pothole on MG Road near Metro Station',
      description: 'Deep road cavity causing vehicle rim damage and severe traffic slowdown.',
      category: 'ROADS_AND_FOOTPATHS',
      status: 'ASSIGNED',
      priority: 'HIGH',
      location: 'MG Road Junction, Pillar 42',
      citizenName: 'Jane Citizen',
      citizenEmail: 'citizen@civiceye.com',
      departmentId: 1,
      departmentName: 'Public Works & Roads',
      startedWorkingAt: null,
      progressNotes: null,
      resolutionNotes: null,
      resolutionImageUrl: null,
      imageUrl: '/api/files/reported-pothole.jpg',
      createdAt: '2026-10-12T10:00:00Z',
      updatedAt: '2026-10-12T10:00:00Z',
    },
    {
      id: 101,
      complaintNumber: 'CE-2026-0002',
      title: 'Damaged sidewalk curbing with exposed steel rods',
      description: 'Pedestrian walkway damaged during fiber cable trenching.',
      category: 'ROADS_AND_FOOTPATHS',
      status: 'IN_PROGRESS',
      priority: 'CRITICAL',
      location: '5th Main Avenue Sector 9',
      citizenName: 'John Doe',
      citizenEmail: 'john@civiceye.com',
      departmentId: 1,
      departmentName: 'Public Works & Roads',
      startedWorkingAt: '2026-10-13T08:30:00Z',
      progressNotes: 'Excavation completed; sub-base stone aggregates compacted.',
      resolutionNotes: null,
      resolutionImageUrl: null,
      imageUrl: null,
      createdAt: '2026-10-11T14:00:00Z',
      updatedAt: '2026-10-13T08:30:00Z',
    },
    {
      id: 102,
      complaintNumber: 'CE-2026-0003',
      title: 'Broken stormwater drain grate repaired',
      description: 'Grate fallen in sewer line creating danger for cyclists.',
      category: 'ROADS_AND_FOOTPATHS',
      status: 'RESOLVED',
      priority: 'MEDIUM',
      location: 'Ring Road Service Lane',
      citizenName: 'Alice Green',
      citizenEmail: 'alice@civiceye.com',
      departmentId: 1,
      departmentName: 'Public Works & Roads',
      startedWorkingAt: '2026-10-10T09:00:00Z',
      progressNotes: 'Replacement cast iron grate installed.',
      resolutionNotes: 'New heavy-duty ductile iron grate welded and secured in place flush with curb.',
      resolutionImageUrl: '/api/files/resolved-grate.jpg',
      imageUrl: '/api/files/broken-grate.jpg',
      createdAt: '2026-10-09T12:00:00Z',
      updatedAt: '2026-10-11T16:00:00Z',
      resolvedAt: '2026-10-11T16:00:00Z',
    },
  ]

  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(departmentService, 'getDepartmentComplaints').mockResolvedValue({
      complaints: [...sampleTasks],
      totalItems: sampleTasks.length,
      totalPages: 1,
      currentPage: 0,
    })
    vi.spyOn(departmentService, 'startWork').mockResolvedValue({
      success: true,
      data: sampleTasks[0],
    })
    vi.spyOn(departmentService, 'addProgressNote').mockResolvedValue({
      success: true,
      data: sampleTasks[1],
    })
    vi.spyOn(departmentService, 'resolveComplaint').mockResolvedValue({
      success: true,
      data: sampleTasks[1],
    })
    vi.spyOn(departmentService, 'getDepartmentComplaintById').mockImplementation((id) => {
      const item = sampleTasks.find((t) => t.id === Number(id)) || sampleTasks[0]
      return Promise.resolve({
        ...item,
        timeline: [
          {
            id: 1,
            fromStatus: 'SUBMITTED',
            toStatus: 'ASSIGNED',
            changedByName: 'Admin Dispatcher',
            remarks: 'Assigned to Public Works & Roads',
            createdAt: '2026-10-12T10:15:00Z',
          },
        ],
      })
    })
  })

  it('renders Department Work Console with title, live filter tabs, search bar, and KPI metrics', async () => {
    renderWithProviders(<DepartmentComplaints />)

    expect(await screen.findByText(/Department Work Console/i)).toBeDefined()
    expect(await screen.findByTestId('complaint-row-100')).toBeDefined()
    expect(screen.getByTestId('dept-search-input')).toBeDefined()
    expect(screen.getByTestId('department-filter-tabs')).toBeDefined()

    // Metrics summary
    expect(screen.getByTestId('metric-total').textContent).toBe('3')
    expect(screen.getByTestId('metric-assigned').textContent).toBe('1')
    expect(screen.getByTestId('metric-in-progress').textContent).toBe('1')
    expect(screen.getByTestId('metric-resolved').textContent).toBe('1')
  })

  it('filters complaints using live filter tabs (New Tasks, In Progress, Resolved)', async () => {
    renderWithProviders(<DepartmentComplaints />)

    // Wait for table rows
    expect(await screen.findByTestId('complaint-row-100')).toBeDefined()
    expect(screen.getByTestId('complaint-row-101')).toBeDefined()
    expect(screen.getByTestId('complaint-row-102')).toBeDefined()

    // Switch to "New Tasks (ASSIGNED)" tab
    fireEvent.click(screen.getByTestId('dept-tab-assigned'))
    expect(screen.getByTestId('complaint-row-100')).toBeDefined()
    expect(screen.queryByTestId('complaint-row-101')).toBeNull()
    expect(screen.queryByTestId('complaint-row-102')).toBeNull()

    // Switch to "In Progress" tab
    fireEvent.click(screen.getByTestId('dept-tab-in_progress'))
    expect(screen.queryByTestId('complaint-row-100')).toBeNull()
    expect(screen.getByTestId('complaint-row-101')).toBeDefined()
    expect(screen.queryByTestId('complaint-row-102')).toBeNull()

    // Switch to "Resolved" tab
    fireEvent.click(screen.getByTestId('dept-tab-resolved'))
    expect(screen.queryByTestId('complaint-row-100')).toBeNull()
    expect(screen.queryByTestId('complaint-row-101')).toBeNull()
    expect(screen.getByTestId('complaint-row-102')).toBeDefined()
  })

  it('filters complaints by search input', async () => {
    renderWithProviders(<DepartmentComplaints />)

    expect(await screen.findByTestId('complaint-row-100')).toBeDefined()

    const searchInput = screen.getByTestId('dept-search-input')
    fireEvent.change(searchInput, { target: { value: 'sidewalk' } })

    expect(screen.queryByTestId('complaint-row-100')).toBeNull()
    expect(screen.getByTestId('complaint-row-101')).toBeDefined()
    expect(screen.queryByTestId('complaint-row-102')).toBeNull()
  })

  it('executes 1-click "Start Work" action, transitioning status from ASSIGNED to IN_PROGRESS', async () => {
    const updatedTask = {
      ...sampleTasks[0],
      status: 'IN_PROGRESS',
      progressNotes: 'Field maintenance crew mobilized to site',
      startedWorkingAt: new Date().toISOString(),
    }
    vi.spyOn(departmentService, 'startWork').mockResolvedValue({
      success: true,
      data: updatedTask,
    })

    renderWithProviders(<DepartmentComplaints />)

    const startWorkBtn = await screen.findByTestId('btn-start-work-100')
    expect(startWorkBtn).toBeDefined()

    fireEvent.click(startWorkBtn)

    await waitFor(() => {
      expect(departmentService.startWork).toHaveBeenCalledWith(100, expect.any(Object))
    })

    // Notice shown
    expect(await screen.findByText(/Work commenced on CE-2026-0001/i)).toBeDefined()
  })

  it('opens "Add Progress Update" modal and successfully logs field progress note', async () => {
    const updatedTask = {
      ...sampleTasks[1],
      progressNotes: 'Surface layer leveled with bituminous asphalt mix.',
    }
    vi.spyOn(departmentService, 'addProgressNote').mockResolvedValue({
      success: true,
      data: updatedTask,
    })

    renderWithProviders(<DepartmentComplaints />)

    const addNoteBtn = await screen.findByTestId('btn-add-progress-101')
    fireEvent.click(addNoteBtn)

    // Modal opens
    expect(await screen.findByTestId('form-progress-notes')).toBeDefined()

    const textarea = screen.getByTestId('progress-notes-input')
    fireEvent.change(textarea, {
      target: { value: 'Surface layer leveled with bituminous asphalt mix.' },
    })

    fireEvent.click(screen.getByTestId('btn-save-progress'))

    await waitFor(() => {
      expect(departmentService.addProgressNote).toHaveBeenCalledWith(101, {
        notes: 'Surface layer leveled with bituminous asphalt mix.',
      })
    })

    expect(await screen.findByText(/Progress note recorded for CE-2026-0002/i)).toBeDefined()
  })

  it('enforces non-empty resolution notes in "Resolve Complaint" modal before submission', async () => {
    renderWithProviders(<DepartmentComplaints />)

    const resolveBtn = await screen.findByTestId('btn-resolve-issue-101')
    fireEvent.click(resolveBtn)

    expect(await screen.findByTestId('form-resolve-complaint')).toBeDefined()

    // Clear resolution notes input and attempt to submit
    const textarea = screen.getByTestId('resolution-notes-input')
    fireEvent.change(textarea, { target: { value: '   ' } })

    const confirmBtn = screen.getByTestId('btn-confirm-resolve')
    fireEvent.click(confirmBtn)

    // Error banner displayed
    expect(
      await screen.findByText(/Resolution notes are mandatory and cannot be empty/i)
    ).toBeDefined()
    expect(departmentService.resolveComplaint).not.toHaveBeenCalled()
  })

  it('resolves complaint with notes & proof photo, updating status to RESOLVED', async () => {
    const resolvedTask = {
      ...sampleTasks[1],
      status: 'RESOLVED',
      resolutionNotes: 'Curbing rebuilt with reinforced concrete and smoothed flush with road.',
      resolutionImageUrl: '/api/files/resolved-curb.jpg',
      resolvedAt: new Date().toISOString(),
    }
    vi.spyOn(departmentService, 'resolveComplaint').mockResolvedValue({
      success: true,
      data: resolvedTask,
    })

    renderWithProviders(<DepartmentComplaints />)

    const resolveBtn = await screen.findByTestId('btn-resolve-issue-101')
    fireEvent.click(resolveBtn)

    expect(await screen.findByTestId('form-resolve-complaint')).toBeDefined()

    const textarea = screen.getByTestId('resolution-notes-input')
    fireEvent.change(textarea, {
      target: { value: 'Curbing rebuilt with reinforced concrete and smoothed flush with road.' },
    })

    const confirmBtn = screen.getByTestId('btn-confirm-resolve')
    fireEvent.click(confirmBtn)

    await waitFor(() => {
      expect(departmentService.resolveComplaint).toHaveBeenCalledWith(101, expect.any(Object))
    })

    expect(
      await screen.findByText(/Complaint CE-2026-0002 marked as RESOLVED with evidence/i)
    ).toBeDefined()
  })

  it('opens Department Complaint Detail Drawer with workflow stepper and evidence inspection', async () => {
    renderWithProviders(<DepartmentComplaints />)

    const detailBtn = await screen.findByTestId('btn-view-detail-100')
    fireEvent.click(detailBtn)

    expect(await screen.findByTestId('dept-detail-drawer')).toBeDefined()
    expect(screen.getByTestId('workflow-stepper')).toBeDefined()
    expect(screen.getByTestId('evidence-inspection')).toBeDefined()
    expect(screen.getByText(/Defect Overview/i)).toBeDefined()
    expect(screen.getByTestId('drawer-btn-start-work')).toBeDefined()
  })
})
