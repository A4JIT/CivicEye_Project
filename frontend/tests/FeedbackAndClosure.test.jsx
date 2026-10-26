import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import ComplaintTracking from '../src/pages/citizen/ComplaintTracking'
import CitizenDashboard from '../src/pages/citizen/CitizenDashboard'
import ReviewComplaint from '../src/pages/admin/ReviewComplaint'
import AllComplaints from '../src/pages/admin/AllComplaints'
import DepartmentDashboard from '../src/pages/department/DepartmentDashboard'
import complaintService from '../src/services/complaintService'
import feedbackService from '../src/services/feedbackService'
import adminService from '../src/services/adminService'
import dashboardService from '../src/services/dashboardService'
import { AuthContext } from '../src/context/AuthContext'

const mockCitizenUser = {
  id: 10,
  username: 'jane_citizen',
  fullName: 'Jane Citizen',
  email: 'citizen@civiceye.com',
  role: 'CITIZEN',
}

const mockAdminUser = {
  id: 1,
  username: 'admin',
  fullName: 'City Administrator',
  email: 'admin@civiceye.com',
  role: 'ADMIN',
}

const mockDeptUser = {
  id: 20,
  username: 'officer_sharma',
  fullName: 'Rajesh Sharma',
  email: 'dept_roads@civiceye.com',
  role: 'DEPARTMENT_USER',
  departmentId: 1,
  departmentName: 'Public Works & Roads',
}

const renderWithProviders = (ui, user = mockCitizenUser) => {
  return render(
    <AuthContext.Provider value={{ user, isAuthenticated: true, logout: vi.fn() }}>
      <BrowserRouter>{ui}</BrowserRouter>
    </AuthContext.Provider>
  )
}

describe('Phase 8 - Resolution Proof Showcase, Citizen Feedback & Closure Suite', () => {
  const mockResolvedComplaint = {
    id: 101,
    complaintNumber: 'CE-2026-0004',
    title: 'Deep road crater patched by municipal squad',
    description: 'Road surface restored with fresh bitumen.',
    category: 'ROADS_AND_FOOTPATHS',
    status: 'RESOLVED',
    priority: 'HIGH',
    location: 'Sector 4, Main Market Road',
    citizenId: 10,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: 1,
    departmentName: 'Public Works & Roads',
    assignedToName: 'Rajesh Sharma',
    imageUrl: 'http://localhost/crater-before.jpg',
    resolutionImageUrl: 'http://localhost/crater-after-resolved.jpg',
    resolutionNotes: 'Compacted cold mix asphalt applied and steamrolled to level grade.',
    createdAt: '2026-10-10T09:00:00Z',
    resolvedAt: '2026-10-12T14:30:00Z',
    closedAt: null,
    feedbackRating: null,
    feedbackComments: null,
    citizenConfirmedResolution: false,
  }

  const mockClosedWithFeedback = {
    ...mockResolvedComplaint,
    status: 'CLOSED',
    closedAt: '2026-10-13T10:00:00Z',
    feedbackRating: 5,
    feedbackComments: 'Super fast road repair! Highly impressed with the quality.',
    citizenConfirmedResolution: true,
  }

  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(complaintService, 'getComplaintByNumber').mockResolvedValue({
      success: true,
      data: mockResolvedComplaint,
    })
    vi.spyOn(complaintService, 'getComplaintTimeline').mockResolvedValue({
      success: true,
      data: [
        {
          id: 1,
          fromStatus: 'IN_PROGRESS',
          toStatus: 'RESOLVED',
          changedByName: 'Rajesh Sharma',
          changedByRole: 'DEPARTMENT_USER',
          remarks: 'Asphalt patched and compacted.',
          createdAt: '2026-10-12T14:30:00Z',
        },
      ],
    })
  })

  describe('ComplaintTracking Component (Phase 8)', () => {
    it('renders dedicated resolution evidence showcase box with remarks and proof photos', async () => {
      renderWithProviders(<ComplaintTracking />)

      const input = screen.getByTestId('tracking-search-input')
      fireEvent.change(input, { target: { value: 'CE-2026-0004' } })
      fireEvent.click(screen.getByTestId('btn-search-tracking'))

      await waitFor(() => {
        expect(screen.getByTestId('resolution-evidence-showcase')).toBeDefined()
        expect(screen.getByTestId('verified-seal')).toBeDefined()
        expect(screen.getByText(/Compacted cold mix asphalt applied/i)).toBeDefined()
        expect(screen.getByText(/Citizen Defect Evidence/i)).toBeDefined()
        expect(screen.getByText(/Field Resolution Proof/i)).toBeDefined()
      })
    })

    it('renders interactive 5-star citizen satisfaction rating selector and comments textarea', async () => {
      renderWithProviders(<ComplaintTracking />)

      const input = screen.getByTestId('tracking-search-input')
      fireEvent.change(input, { target: { value: 'CE-2026-0004' } })
      fireEvent.click(screen.getByTestId('btn-search-tracking'))

      await waitFor(() => {
        expect(screen.getByTestId('citizen-feedback-widget')).toBeDefined()
        expect(screen.getByTestId('star-rating-5')).toBeDefined()
        expect(screen.getByTestId('feedback-comments-input')).toBeDefined()
        expect(screen.getByTestId('btn-submit-feedback')).toBeDefined()
      })

      // Select 5-star rating
      const star5 = screen.getByTestId('star-rating-5')
      fireEvent.click(star5)

      expect(screen.getByTestId('rating-selected-label').textContent).toContain('Excellent')
    })

    it('submits citizen feedback and renders locked verified review badge', async () => {
      vi.spyOn(feedbackService, 'submitFeedback').mockResolvedValue({
        success: true,
        data: {
          id: 50,
          complaintId: 101,
          rating: 5,
          comments: 'Great repair work, thank you team!',
          createdAt: new Date().toISOString(),
        },
      })

      renderWithProviders(<ComplaintTracking />)

      const input = screen.getByTestId('tracking-search-input')
      fireEvent.change(input, { target: { value: 'CE-2026-0004' } })
      fireEvent.click(screen.getByTestId('btn-search-tracking'))

      await waitFor(() => {
        expect(screen.getByTestId('star-rating-5')).toBeDefined()
      })

      // Select 5 stars
      fireEvent.click(screen.getByTestId('star-rating-5'))

      // Enter comments
      const commentsInput = screen.getByTestId('feedback-comments-input')
      fireEvent.change(commentsInput, { target: { value: 'Great repair work, thank you team!' } })

      // Submit feedback
      fireEvent.click(screen.getByTestId('btn-submit-feedback'))

      await waitFor(() => {
        expect(feedbackService.submitFeedback).toHaveBeenCalledWith(101, {
          rating: 5,
          comments: 'Great repair work, thank you team!',
        })
        expect(screen.getByTestId('verified-feedback-badge')).toBeDefined()
        expect(screen.getByTestId('feedback-success-alert')).toBeDefined()
      })
    })

    it('confirms resolution in 1-click and transitions stepper to CLOSED seamlessly', async () => {
      vi.spyOn(feedbackService, 'confirmResolution').mockResolvedValue({
        success: true,
        data: {
          id: 101,
          status: 'CLOSED',
          closedAt: new Date().toISOString(),
          citizenConfirmedResolution: true,
        },
      })

      renderWithProviders(<ComplaintTracking />)

      const input = screen.getByTestId('tracking-search-input')
      fireEvent.change(input, { target: { value: 'CE-2026-0004' } })
      fireEvent.click(screen.getByTestId('btn-search-tracking'))

      await waitFor(() => {
        expect(screen.getByTestId('btn-confirm-resolution')).toBeDefined()
      })

      // Click Confirm Resolution & Close Issue
      fireEvent.click(screen.getByTestId('btn-confirm-resolution'))

      await waitFor(() => {
        expect(feedbackService.confirmResolution).toHaveBeenCalledWith(101, {})
        expect(screen.getByTestId('closure-success-alert')).toBeDefined()
        const closedStep = screen.getByTestId('stepper-step-CLOSED')
        expect(closedStep.className).toContain('completed')
      })
    })

    it('renders verified feedback badge directly when complaint already has feedback', async () => {
      vi.spyOn(complaintService, 'getComplaintByNumber').mockResolvedValue({
        success: true,
        data: mockClosedWithFeedback,
      })

      renderWithProviders(<ComplaintTracking />)

      const input = screen.getByTestId('tracking-search-input')
      fireEvent.change(input, { target: { value: 'CE-2026-0004' } })
      fireEvent.click(screen.getByTestId('btn-search-tracking'))

      await waitFor(() => {
        expect(screen.getByTestId('verified-feedback-badge')).toBeDefined()
        expect(screen.getByTestId('display-feedback-comment')).toBeDefined()
        expect(screen.getByText(/Super fast road repair!/i)).toBeDefined()
      })
    })
  })

  describe('ReviewComplaint Admin Drawer (Phase 8)', () => {
    beforeEach(() => {
      vi.spyOn(adminService, 'getDepartments').mockResolvedValue([
        { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
      ])
      vi.spyOn(adminService, 'getComplaintById').mockResolvedValue(mockClosedWithFeedback)
    })

    it('displays citizen satisfaction star badge and feedback comments in admin drawer', async () => {
      renderWithProviders(<ReviewComplaint complaintProp={mockClosedWithFeedback} isDrawer={true} />, mockAdminUser)

      await waitFor(() => {
        expect(screen.getByTestId('citizen-feedback-box')).toBeDefined()
        expect(screen.getByTestId('citizen-star-badge')).toBeDefined()
        expect(screen.getByTestId('citizen-feedback-comments')).toBeDefined()
        expect(screen.getByText(/Super fast road repair!/i)).toBeDefined()
      })
    })

    it('allows administrator to close resolved complaint with optional remarks', async () => {
      vi.spyOn(feedbackService, 'closeComplaint').mockResolvedValue({
        success: true,
        data: {
          ...mockResolvedComplaint,
          status: 'CLOSED',
          closedAt: new Date().toISOString(),
          closureRemarks: 'Verified field resolution photo and closed ticket.',
        },
      })

      renderWithProviders(<ReviewComplaint complaintProp={mockResolvedComplaint} isDrawer={true} />, mockAdminUser)

      await waitFor(() => {
        expect(screen.getByTestId('btn-open-close-modal')).toBeDefined()
      })

      // Open close modal
      fireEvent.click(screen.getByTestId('btn-open-close-modal'))

      expect(screen.getByTestId('input-closure-remarks')).toBeDefined()

      // Enter closure remarks
      fireEvent.change(screen.getByTestId('input-closure-remarks'), {
        target: { value: 'Verified field resolution photo and closed ticket.' },
      })

      // Confirm closure
      fireEvent.click(screen.getByTestId('btn-confirm-close-complaint'))

      await waitFor(() => {
        expect(feedbackService.closeComplaint).toHaveBeenCalledWith(101, {
          closureRemarks: 'Verified field resolution photo and closed ticket.',
        })
        expect(screen.getByTestId('complaint-closed-box')).toBeDefined()
      })
    })
  })

  describe('AllComplaints Admin Console (Phase 8)', () => {
    it('displays star rating badges and quick close button in table rows', async () => {
      vi.spyOn(adminService, 'getDepartments').mockResolvedValue([
        { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
      ])
      vi.spyOn(adminService, 'getAllComplaints').mockResolvedValue({
        complaints: [mockClosedWithFeedback, mockResolvedComplaint],
      })

      renderWithProviders(<AllComplaints />, mockAdminUser)

      await waitFor(() => {
        expect(screen.getByTestId(`rating-badge-${mockClosedWithFeedback.id}`)).toBeDefined()
        expect(screen.getByTestId(`action-close-${mockResolvedComplaint.id}`)).toBeDefined()
      })
    })
  })

  describe('DepartmentDashboard Satisfaction KPIs (Phase 8)', () => {
    it('renders department satisfaction KPI cards and citizen testimonials feed', async () => {
      vi.spyOn(dashboardService, 'getDepartmentDashboard').mockResolvedValue({
        departmentId: 1,
        departmentName: 'Public Works & Roads',
        departmentCode: 'ROAD_WORKS',
        totalAssigned: 12,
        pendingComplaints: 2,
        inProgressComplaints: 4,
        resolvedComplaints: 6,
        criticalCount: 0,
        highCount: 3,
        mediumCount: 2,
        lowCount: 1,
        recentComplaints: [],
      })

      vi.spyOn(feedbackService, 'getDepartmentFeedback').mockResolvedValue({
        success: true,
        data: [
          {
            id: 201,
            complaintNumber: 'CE-2026-0004',
            citizenName: 'Jane Citizen',
            rating: 5,
            comments: 'Outstanding asphalt repair work on Sector 4 road!',
            createdAt: '2026-10-12T16:00:00Z',
          },
        ],
      })

      renderWithProviders(<DepartmentDashboard />, mockDeptUser)

      await waitFor(() => {
        expect(screen.getByTestId('dept-satisfaction-kpi-grid')).toBeDefined()
        expect(screen.getByTestId('dept-avg-rating')).toBeDefined()
        expect(screen.getByTestId('dept-positive-pct')).toBeDefined()
        expect(screen.getByTestId('dept-feedback-testimonials')).toBeDefined()
        expect(screen.getByTestId('testimonial-201')).toBeDefined()
        expect(screen.getByText(/Outstanding asphalt repair work/i)).toBeDefined()
      })
    })
  })

  describe('CitizenDashboard (Phase 8)', () => {
    it('renders resolution verification alert notice when resolved complaints exist', async () => {
      vi.spyOn(dashboardService, 'getCitizenDashboard').mockResolvedValue({
        totalComplaints: 3,
        pendingComplaints: 0,
        inProgressComplaints: 1,
        resolvedComplaints: 2,
        recentComplaints: [
          {
            id: 101,
            trackingNumber: 'CE-2026-0004',
            title: 'Repaired road pothole',
            category: 'ROADS_AND_FOOTPATHS',
            priority: 'HIGH',
            status: 'RESOLVED',
            departmentName: 'Public Works & Roads',
          },
        ],
      })

      renderWithProviders(<CitizenDashboard />, mockCitizenUser)

      await waitFor(() => {
        expect(screen.getByTestId('citizen-resolved-notice')).toBeDefined()
        expect(screen.getByTestId('rate-resolution-CE-2026-0004')).toBeDefined()
      })
    })
  })
})
