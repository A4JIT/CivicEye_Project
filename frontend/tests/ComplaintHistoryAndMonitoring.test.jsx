import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import ComplaintAuditTimeline from '../src/components/complaints/ComplaintAuditTimeline'
import AllComplaints from '../src/pages/admin/AllComplaints'
import historyService from '../src/services/historyService'
import adminService from '../src/services/adminService'
import { AuthContext } from '../src/context/AuthContext'

const mockAdminUser = {
  id: 1,
  username: 'admin',
  email: 'admin@civiceye.com',
  fullName: 'Administrator',
  role: 'ADMIN',
}

const renderWithRouter = (ui) => {
  return render(
    <AuthContext.Provider value={{ user: mockAdminUser, logout: vi.fn() }}>
      <BrowserRouter>{ui}</BrowserRouter>
    </AuthContext.Provider>
  )
}

describe('Phase 9: Complaint Audit Timeline & Admin Monitoring Suite', () => {
  const mockTimelineEvents = [
    {
      id: 1,
      timestamp: '2026-10-10T09:30:00Z',
      eventType: 'SUBMITTED',
      actorRole: 'CITIZEN',
      actorName: 'Aarav Patel',
      title: 'Complaint Filed by Citizen',
      description: 'Dangerous pothole reported on Central Ring Road with photograph.',
      fromStatus: null,
      toStatus: 'SUBMITTED',
      evidenceUrl: 'http://localhost/evidence1.jpg',
    },
    {
      id: 2,
      timestamp: '2026-10-10T11:00:00Z',
      eventType: 'UNDER_REVIEW',
      actorRole: 'ADMIN',
      actorName: 'Control Desk Officer',
      title: 'Triage & Severity Assessment',
      description: 'Verified as critical road defect; priority elevated to HIGH.',
      fromStatus: 'SUBMITTED',
      toStatus: 'UNDER_REVIEW',
    },
    {
      id: 3,
      timestamp: '2026-10-10T14:15:00Z',
      eventType: 'ASSIGNED',
      actorRole: 'ADMIN',
      actorName: 'Control Desk Officer',
      title: 'Assigned to Public Works & Roads',
      description: 'Transferred to PWD Division 4 with dispatch instructions.',
      fromStatus: 'UNDER_REVIEW',
      toStatus: 'ASSIGNED',
    },
    {
      id: 4,
      timestamp: '2026-10-11T08:00:00Z',
      eventType: 'IN_PROGRESS',
      actorRole: 'DEPARTMENT_USER',
      actorName: 'Engineer Ramesh Kumar',
      title: 'Field Squad Mobilized',
      description: 'Road repair team arrived at site; asphalt cold-mix applied.',
      fromStatus: 'ASSIGNED',
      toStatus: 'IN_PROGRESS',
    },
    {
      id: 5,
      timestamp: '2026-10-11T16:45:00Z',
      eventType: 'RESOLVED',
      actorRole: 'DEPARTMENT_USER',
      actorName: 'Engineer Ramesh Kumar',
      title: 'Road Surface Fully Repaired',
      description: 'Pothole patch compacted and traffic flow restored.',
      fromStatus: 'IN_PROGRESS',
      toStatus: 'RESOLVED',
      evidenceUrl: 'http://localhost/resolved_proof.jpg',
    },
    {
      id: 6,
      timestamp: '2026-10-12T10:00:00Z',
      eventType: 'CLOSED',
      actorRole: 'CITIZEN',
      actorName: 'Aarav Patel',
      title: 'Citizen Confirmed & Officially Closed',
      description: 'Citizen rated repair 5 stars and confirmed defect resolution.',
      fromStatus: 'RESOLVED',
      toStatus: 'CLOSED',
    },
  ]

  const mockAdminComplaints = [
    {
      id: 101,
      complaintNumber: 'CE-2026-0101',
      title: 'Severe crater pothole on MG Road',
      description: 'Deep road crater causing vehicle rim damage and severe traffic slowdown.',
      category: 'ROADS_AND_FOOTPATHS',
      status: 'SUBMITTED',
      priority: 'CRITICAL',
      location: 'MG Road Metro Station',
      citizenName: 'Aarav Patel',
      citizenEmail: 'aarav@civiceye.com',
      departmentId: null,
      departmentName: null,
      createdAt: '2026-10-10T08:00:00Z',
      updatedAt: '2026-10-10T08:00:00Z',
    },
    {
      id: 102,
      complaintNumber: 'CE-2026-0102',
      title: 'Street lights flickering and failing on 5th Avenue',
      description: 'Series of 4 LED street lamp posts completely dark since Monday.',
      category: 'ELECTRICITY',
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      location: '5th Main Avenue, Sector 9',
      citizenName: 'Bhavna Sharma',
      citizenEmail: 'bhavna@civiceye.com',
      departmentId: 2,
      departmentName: 'Electricity & Street Lighting',
      createdAt: '2026-10-12T10:00:00Z',
      updatedAt: '2026-10-12T12:00:00Z',
    },
    {
      id: 103,
      complaintNumber: 'CE-2026-0103',
      title: 'Sewage drain backflow into residential lane',
      description: 'Drain blocked causing contaminated overflow near school gate.',
      category: 'WATER_AND_SEWAGE',
      status: 'RESOLVED',
      priority: 'MEDIUM',
      location: 'Lane 4, Model Colony',
      citizenName: 'Chetan Verma',
      citizenEmail: 'chetan@civiceye.com',
      departmentId: 3,
      departmentName: 'Water Supply & Sewerage',
      createdAt: '2026-10-05T09:00:00Z',
      updatedAt: '2026-10-08T15:00:00Z',
    },
  ]

  const mockDepartments = [
    { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
    { id: 2, name: 'Electricity & Street Lighting', code: 'ELECTRICITY' },
    { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(adminService, 'getDepartments').mockResolvedValue(mockDepartments)
    vi.spyOn(adminService, 'getAllComplaints').mockResolvedValue({
      complaints: mockAdminComplaints,
    })
    vi.spyOn(historyService, 'getComplaintHistory').mockResolvedValue({
      success: true,
      data: {
        complaintId: 101,
        complaintNumber: 'CE-2026-0101',
        title: 'Severe crater pothole on MG Road',
        events: mockTimelineEvents,
      },
    })
  })

  describe('Part 1: ComplaintAuditTimeline Component', () => {
    it('renders audit timeline with all events, actor roles, and status shifts', () => {
      renderWithRouter(
        <ComplaintAuditTimeline initialEvents={mockTimelineEvents} complaintId={101} />
      )

      expect(screen.getByTestId('timeline-audit-card')).toBeDefined()
      expect(screen.getByText(/Audit Trail & Activity Log/i)).toBeDefined()

      const nodes = screen.getAllByTestId('timeline-node')
      expect(nodes.length).toBe(6)

      // Verify Actor badges
      expect(screen.getAllByText(/Aarav Patel \(Citizen\)/i).length).toBe(2)
      expect(screen.getAllByText(/Control Desk Officer \(Admin Triage\)/i).length).toBe(2)
      expect(screen.getAllByText(/Engineer Ramesh Kumar \(Department Staff\)/i).length).toBe(2)

      // Verify Status Shifts
      expect(screen.getByText(/SUBMITTED ➔ UNDER_REVIEW/i)).toBeDefined()
      expect(screen.getByText(/UNDER_REVIEW ➔ ASSIGNED/i)).toBeDefined()
      expect(screen.getByText(/IN_PROGRESS ➔ RESOLVED/i)).toBeDefined()
    })

    it('filters timeline events by category pills (Status Shifts, Assignments, Resolution)', () => {
      renderWithRouter(
        <ComplaintAuditTimeline initialEvents={mockTimelineEvents} complaintId={101} />
      )

      // Filter by Assignments only
      const assignmentFilterBtn = screen.getByRole('button', { name: /^Assignments/i })
      fireEvent.click(assignmentFilterBtn)

      let nodes = screen.getAllByTestId('timeline-node')
      expect(nodes.length).toBe(1)
      expect(screen.getByText(/Assigned to Public Works & Roads/i)).toBeDefined()

      // Filter by Resolution & Proof
      const resolutionFilterBtn = screen.getByRole('button', { name: /^Resolution & Proof/i })
      fireEvent.click(resolutionFilterBtn)

      nodes = screen.getAllByTestId('timeline-node')
      expect(nodes.length).toBe(2)
      expect(screen.getByText(/Road Surface Fully Repaired/i)).toBeDefined()

      // Reset to All Events
      const allFilterBtn = screen.getByRole('button', { name: /All Events/i })
      fireEvent.click(allFilterBtn)
      nodes = screen.getAllByTestId('timeline-node')
      expect(nodes.length).toBe(6)
    })

    it('toggles chronological order from oldest to newest and newest first', () => {
      renderWithRouter(
        <ComplaintAuditTimeline initialEvents={mockTimelineEvents} complaintId={101} />
      )

      const sortBtn = screen.getByRole('button', { name: /Chronological/i })
      expect(sortBtn).toBeDefined()

      // Initial oldest event is first
      let eventTitles = screen.getAllByText(/Complaint Filed by Citizen|Officially Closed/i)
      expect(eventTitles[0].textContent).toContain('Complaint Filed by Citizen')

      // Click to invert order (latest first)
      fireEvent.click(sortBtn)
      eventTitles = screen.getAllByText(/Complaint Filed by Citizen|Officially Closed/i)
      expect(eventTitles[0].textContent).toContain('Officially Closed')
    })

    it('opens evidence photo preview modal when attached thumbnail is clicked', () => {
      renderWithRouter(
        <ComplaintAuditTimeline initialEvents={mockTimelineEvents} complaintId={101} />
      )

      const thumbs = screen.getAllByAltText('Audit Event Attachment')
      expect(thumbs.length).toBe(2)

      // Click first thumbnail to inspect full size
      fireEvent.click(thumbs[0])
      expect(screen.getByText(/Inspection Evidence Full View/i)).toBeDefined()
      expect(screen.getByAltText('Enlarged Evidence')).toBeDefined()

      // Close modal
      const closeBtn = screen.getByRole('button', { name: '✕' })
      fireEvent.click(closeBtn)
      expect(screen.queryByText(/Inspection Evidence Full View/i)).toBeNull()
    })

    it('displays empty state when no events exist', () => {
      renderWithRouter(
        <ComplaintAuditTimeline initialEvents={[]} complaintId={999} />
      )

      expect(screen.getByText(/No Milestones Recorded/i)).toBeDefined()
      expect(screen.queryByTestId('timeline-node')).toBeNull()
    })
  })

  describe('Part 2: Advanced Admin Monitoring & Filtering Console (AllComplaints)', () => {
    it('renders municipal KPI summary bar with correct aggregate counts', async () => {
      renderWithRouter(<AllComplaints />)

      await waitFor(() => {
        expect(screen.getByTestId('monitoring-summary-bar')).toBeDefined()
        expect(screen.getByTestId('kpi-total-count').textContent).toBe('3')
        expect(screen.getByTestId('kpi-critical-count').textContent).toBe('1') // CE-2026-0101
        expect(screen.getByTestId('kpi-unassigned-count').textContent).toBe('1') // CE-2026-0101 has null dept
        expect(screen.getByTestId('kpi-active-count').textContent).toBe('1') // CE-2026-0102 is IN_PROGRESS
        expect(screen.getByTestId('kpi-resolved-count').textContent).toBe('1') // CE-2026-0103 is RESOLVED
      })
    })

    it('filters complaints by category and priority dropdowns with active filter chips', async () => {
      renderWithRouter(<AllComplaints />)

      await waitFor(() => {
        expect(screen.getByText('Severe crater pothole on MG Road')).toBeDefined()
      })

      // Select category ROADS_AND_FOOTPATHS
      const categorySelect = screen.getByTestId('filter-select-category')
      fireEvent.change(categorySelect, { target: { value: 'ROADS_AND_FOOTPATHS' } })

      expect(screen.getByTestId('chip-filter-category')).toBeDefined()
      expect(screen.getByText('Severe crater pothole on MG Road')).toBeDefined()
      expect(screen.queryByText('Street lights flickering')).toBeNull()

      // Remove category chip via "×" button
      const removeCategoryBtn = screen.getByTestId('chip-filter-category').querySelector('button')
      fireEvent.click(removeCategoryBtn)

      // Both should appear again
      expect(screen.getByText('Street lights flickering and failing on 5th Avenue')).toBeDefined()
    })

    it('filters complaints by date range (From Date - To Date)', async () => {
      renderWithRouter(<AllComplaints />)

      await waitFor(() => {
        expect(screen.getByText('Severe crater pothole on MG Road')).toBeDefined()
      })

      const startDateInput = screen.getByTestId('filter-start-date')
      const endDateInput = screen.getByTestId('filter-end-date')

      // Set range to 2026-10-11 to 2026-10-13 (only CE-2026-0102 from 2026-10-12 falls in range)
      fireEvent.change(startDateInput, { target: { value: '2026-10-11' } })
      fireEvent.change(endDateInput, { target: { value: '2026-10-13' } })

      expect(screen.getByTestId('chip-filter-date')).toBeDefined()
      expect(screen.getByText('Street lights flickering and failing on 5th Avenue')).toBeDefined()
      expect(screen.queryByText('Severe crater pothole on MG Road')).toBeNull() // filed 2026-10-10
      expect(screen.queryByText('Sewage drain backflow into residential lane')).toBeNull() // filed 2026-10-05

      // Clear all chips
      const clearAllBtn = screen.getByTestId('btn-clear-all-chips')
      fireEvent.click(clearAllBtn)

      expect(screen.getByText('Severe crater pothole on MG Road')).toBeDefined()
    })

    it('searches complaints dynamically by search input keyword and clears on demand', async () => {
      renderWithRouter(<AllComplaints />)

      await waitFor(() => {
        expect(screen.getByText('Severe crater pothole on MG Road')).toBeDefined()
      })

      const searchInput = screen.getByTestId('triage-search-input')
      fireEvent.change(searchInput, { target: { value: 'Sewage' } })

      expect(screen.getByTestId('chip-filter-search')).toBeDefined()
      expect(screen.getByText('Sewage drain backflow into residential lane')).toBeDefined()
      expect(screen.queryByText('Severe crater pothole on MG Road')).toBeNull()

      // Reset filters button
      const resetBtn = screen.getByTestId('btn-reset-filters')
      fireEvent.click(resetBtn)

      expect(screen.getByText('Severe crater pothole on MG Road')).toBeDefined()
    })

    it('opens 1-click View Full History / Audit Log modal for any selected complaint', async () => {
      renderWithRouter(<AllComplaints />)

      await waitFor(() => {
        expect(screen.getByTestId('action-audit-101')).toBeDefined()
      })

      // Click "History" button on first complaint row
      const auditBtn = screen.getByTestId('action-audit-101')
      fireEvent.click(auditBtn)

      await waitFor(() => {
        expect(screen.getByTestId('audit-modal-content')).toBeDefined()
        expect(screen.getByText(/Grievance Audit Trail: CE-2026-0101/i)).toBeDefined()
        expect(historyService.getComplaintHistory).toHaveBeenCalledWith(101)
      })

      // Close audit modal
      const closeAuditBtn = screen.getByTestId('btn-close-audit-modal')
      fireEvent.click(closeAuditBtn)

      expect(screen.queryByTestId('audit-modal-content')).toBeNull()
    })
  })
})
