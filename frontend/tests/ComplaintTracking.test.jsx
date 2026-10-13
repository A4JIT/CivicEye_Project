import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import ComplaintTracking from '../src/pages/citizen/ComplaintTracking'
import complaintService from '../src/services/complaintService'

const renderWithRouter = (ui) => {
  return render(<BrowserRouter>{ui}</BrowserRouter>)
}

describe('ComplaintTracking Component Suite', () => {
  const mockComplaint = {
    id: 101,
    complaintNumber: 'CE-2026-0001',
    title: 'Hazardous Road Pothole',
    description: 'Deep road crack endangering traffic',
    category: 'ROADS_AND_FOOTPATHS',
    status: 'IN_PROGRESS',
    priority: 'HIGH',
    location: 'MG Road, Ward 12',
    departmentName: 'Roads & Traffic',
    assignedToName: 'Rajesh Sharma (PWD)',
    imageUrl: 'http://localhost/test-evidence.jpg',
    resolutionImageUrl: 'http://localhost/test-resolved.jpg',
    resolutionNotes: 'Asphalt patched and compacted.',
    createdAt: '2026-10-10T10:00:00Z',
    resolvedAt: null,
  }

  const mockTimeline = [
    {
      id: 1,
      fromStatus: null,
      toStatus: 'SUBMITTED',
      changedByName: 'Jane Citizen',
      changedByRole: 'CITIZEN',
      remarks: 'Complaint filed by citizen',
      createdAt: '2026-10-10T10:00:00Z',
    },
    {
      id: 2,
      fromStatus: 'SUBMITTED',
      toStatus: 'ASSIGNED',
      changedByName: 'Admin User',
      changedByRole: 'ADMIN',
      remarks: 'Assigned to Roads Department',
      createdAt: '2026-10-11T12:00:00Z',
    },
    {
      id: 3,
      fromStatus: 'ASSIGNED',
      toStatus: 'IN_PROGRESS',
      changedByName: 'Rajesh Sharma',
      changedByRole: 'DEPARTMENT_USER',
      remarks: 'Squad on site',
      createdAt: '2026-10-12T09:00:00Z',
    },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(complaintService, 'getComplaintByNumber').mockResolvedValue({
      success: true,
      data: mockComplaint,
    })
    vi.spyOn(complaintService, 'getComplaintTimeline').mockResolvedValue({
      success: true,
      data: mockTimeline,
    })
  })

  it('renders search bar and quick tracking chips on initial load', () => {
    renderWithRouter(<ComplaintTracking />)

    expect(screen.getByText(/Track Civic Defect Status/i)).toBeDefined()
    expect(screen.getByTestId('tracking-search-input')).toBeDefined()
    expect(screen.getByTestId('btn-search-tracking')).toBeDefined()
    expect(screen.getByText('CE-2026-0001')).toBeDefined()
  })

  it('searches complaint and renders details card', async () => {
    renderWithRouter(<ComplaintTracking />)

    const input = screen.getByTestId('tracking-search-input')
    const searchBtn = screen.getByTestId('btn-search-tracking')

    fireEvent.change(input, { target: { value: 'CE-2026-0001' } })
    fireEvent.click(searchBtn)

    await waitFor(() => {
      expect(complaintService.getComplaintByNumber).toHaveBeenCalledWith('CE-2026-0001')
      expect(screen.getByTestId('tracking-details-card')).toBeDefined()
      expect(screen.getByText('Hazardous Road Pothole')).toBeDefined()
      expect(screen.getByText('Roads & Traffic')).toBeDefined()
    })
  })

  it('renders 4-step progress stepper with active and completed milestones', async () => {
    renderWithRouter(<ComplaintTracking />)

    const input = screen.getByTestId('tracking-search-input')
    fireEvent.change(input, { target: { value: 'CE-2026-0001' } })
    fireEvent.click(screen.getByTestId('btn-search-tracking'))

    await waitFor(() => {
      expect(screen.getByTestId('lifecycle-stepper')).toBeDefined()
      const step1 = screen.getByTestId('stepper-step-SUBMITTED')
      const step2 = screen.getByTestId('stepper-step-ASSIGNED')
      const step3 = screen.getByTestId('stepper-step-IN_PROGRESS')

      expect(step1.className).toContain('completed')
      expect(step2.className).toContain('completed')
      expect(step3.className).toContain('current')
    })
  })

  it('renders chronological audit timeline events', async () => {
    renderWithRouter(<ComplaintTracking />)

    const input = screen.getByTestId('tracking-search-input')
    fireEvent.change(input, { target: { value: 'CE-2026-0001' } })
    fireEvent.click(screen.getByTestId('btn-search-tracking'))

    await waitFor(() => {
      expect(screen.getByTestId('timeline-audit-card')).toBeDefined()
      const timelineNodes = screen.getAllByTestId('timeline-node')
      expect(timelineNodes.length).toBe(3)
      expect(screen.getByText(/Assigned to Roads Department/i)).toBeDefined()
      expect(screen.getByText(/Squad on site/i)).toBeDefined()
    })
  })

  it('renders defect photo and resolution proof evidence when present', async () => {
    renderWithRouter(<ComplaintTracking />)

    const input = screen.getByTestId('tracking-search-input')
    fireEvent.change(input, { target: { value: 'CE-2026-0001' } })
    fireEvent.click(screen.getByTestId('btn-search-tracking'))

    await waitFor(() => {
      expect(screen.getByText(/Citizen Defect Evidence/i)).toBeDefined()
      expect(screen.getByText(/Field Resolution Proof/i)).toBeDefined()
      expect(screen.getByText(/Asphalt patched and compacted/i)).toBeDefined()
    })
  })

  it('renders rejection notice when complaint status is REJECTED', async () => {
    vi.spyOn(complaintService, 'getComplaintByNumber').mockResolvedValue({
      success: true,
      data: {
        ...mockComplaint,
        status: 'REJECTED',
        rejectionReason: 'Outside civic boundary jurisdiction',
      },
    })

    renderWithRouter(<ComplaintTracking />)

    const input = screen.getByTestId('tracking-search-input')
    fireEvent.change(input, { target: { value: 'CE-2026-0005' } })
    fireEvent.click(screen.getByTestId('btn-search-tracking'))

    await waitFor(() => {
      expect(screen.getByTestId('rejected-banner')).toBeDefined()
      expect(screen.getByText(/Outside civic boundary jurisdiction/i)).toBeDefined()
    })
  })

  it('allows copying tracking ID to clipboard', async () => {
    Object.assign(navigator, {
      clipboard: {
        writeText: vi.fn().mockImplementation(() => Promise.resolve()),
      },
    })

    renderWithRouter(<ComplaintTracking />)

    const input = screen.getByTestId('tracking-search-input')
    fireEvent.change(input, { target: { value: 'CE-2026-0001' } })
    fireEvent.click(screen.getByTestId('btn-search-tracking'))

    await waitFor(() => {
      const copyBtn = screen.getByText(/📋 Copy ID/i)
      fireEvent.click(copyBtn)
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith('CE-2026-0001')
    })
  })
})
