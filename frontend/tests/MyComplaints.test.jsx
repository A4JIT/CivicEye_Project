import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthContext } from '../src/context/AuthContext'
import MyComplaints from '../src/pages/citizen/MyComplaints'
import { complaintService } from '../src/services/complaintService'

const mockUser = {
  id: 2,
  username: 'jane_doe',
  fullName: 'Jane Doe',
  email: 'citizen@civiceye.com',
  role: 'CITIZEN',
}

const mockComplaintsList = [
  {
    id: 1,
    complaintNumber: 'CE-2026-0001',
    title: 'Severe crater pothole on MG Road near Metro Station',
    description: 'Deep road crater causing vehicle rim damage.',
    category: 'ROADS_AND_FOOTPATHS',
    status: 'IN_PROGRESS',
    priority: 'HIGH',
    location: 'MG Road Metro Station',
    departmentName: 'Public Works & Roads',
    createdAt: '2026-10-20T10:00:00Z',
  },
  {
    id: 2,
    complaintNumber: 'CE-2026-0002',
    title: 'Street lights flickering and failing on 5th Main Avenue',
    description: 'Series of 4 LED street lamp posts completely dark.',
    category: 'STREETLIGHTS_AND_ELECTRICITY',
    status: 'UNDER_REVIEW',
    priority: 'MEDIUM',
    location: '5th Main Avenue, Sector 9',
    departmentName: 'Electricity & Street Lighting',
    createdAt: '2026-10-19T10:00:00Z',
  },
  {
    id: 3,
    complaintNumber: 'CE-2026-0003',
    title: 'Major drinking water main pipeline leak',
    description: 'Treated water gushing into storm drain.',
    category: 'WATER_SUPPLY_AND_LEAKAGE',
    status: 'RESOLVED',
    priority: 'CRITICAL',
    location: 'Ring Road Junction',
    departmentName: 'Water Supply & Sewerage',
    createdAt: '2026-10-18T10:00:00Z',
  },
  {
    id: 4,
    complaintNumber: 'CE-2026-0004',
    title: 'Garbage accumulation near community school gate',
    description: 'Municipal waste bin overflowing onto sidewalk.',
    category: 'GARBAGE_AND_CLEANLINESS',
    status: 'SUBMITTED',
    priority: 'MEDIUM',
    location: 'Public School Lane',
    departmentName: 'Sanitation & Solid Waste',
    createdAt: '2026-10-17T10:00:00Z',
  },
  {
    id: 5,
    complaintNumber: 'CE-2026-0005',
    title: 'Clogged storm water drainage causing road waterlogging',
    description: 'Monsoon silt blocking drain gratings.',
    category: 'DRAINAGE_AND_SEWAGE',
    status: 'ASSIGNED',
    priority: 'HIGH',
    location: 'Gandhi Nagar 3rd Cross',
    departmentName: 'Drainage & Stormwater',
    createdAt: '2026-10-16T10:00:00Z',
  },
  {
    id: 6,
    complaintNumber: 'CE-2026-0006',
    title: 'Stagnant wastewater pooling behind residential complex',
    description: 'Uncovered ditch attracting mosquitoes.',
    category: 'PUBLIC_HEALTH_HAZARDS',
    status: 'CLOSED',
    priority: 'LOW',
    location: 'Greenwood Apartments Backgate',
    departmentName: 'Public Health Department',
    createdAt: '2026-10-15T10:00:00Z',
  },
  {
    id: 7,
    complaintNumber: 'CE-2026-0007',
    title: 'Fallen tree branch damaging overhead power cables',
    description: 'Heavy bough resting on 440V distribution lines.',
    category: 'STREETLIGHTS_AND_ELECTRICITY',
    status: 'RESOLVED',
    priority: 'HIGH',
    location: 'Park Avenue, Near Clock Tower',
    departmentName: 'Electricity & Street Lighting',
    createdAt: '2026-10-14T10:00:00Z',
  },
]

const renderMyComplaints = () => {
  return render(
    <BrowserRouter>
      <AuthContext.Provider value={{ user: mockUser, isAuthenticated: true }}>
        <MyComplaints />
      </AuthContext.Provider>
    </BrowserRouter>
  )
}

describe('MyComplaints Component Suite', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(complaintService, 'getMyComplaints').mockResolvedValue({
      success: true,
      data: mockComplaintsList,
    })
  })

  it('renders distinct My Complaints workspace with header, KPI cards, and search toolbar', async () => {
    renderMyComplaints()

    await waitFor(() => {
      expect(screen.getByTestId('my-complaints-page')).toBeDefined()
      expect(screen.getByText('My Filed Grievances & Defect Log')).toBeDefined()
      expect(screen.getByText(/Total Grievances Registered/i)).toBeDefined()
      expect(screen.getByTestId('input-search-complaints')).toBeDefined()
    })
  })

  it('initially displays first 5 complaints and provides load more / show all buttons', async () => {
    renderMyComplaints()

    await waitFor(() => {
      // First 5 should be present
      expect(screen.getByText('CE-2026-0001')).toBeDefined()
      expect(screen.getByText('CE-2026-0002')).toBeDefined()
      expect(screen.getByText('CE-2026-0003')).toBeDefined()
      expect(screen.getByText('CE-2026-0004')).toBeDefined()
      expect(screen.getByText('CE-2026-0005')).toBeDefined()
      
      // 6th and 7th should not be visible yet
      expect(screen.queryByText('CE-2026-0006')).toBeNull()
      expect(screen.queryByText('CE-2026-0007')).toBeNull()
    })

    // Shows pagination counter
    expect(screen.getByText(/Showing/i)).toBeDefined()
    expect(screen.getByTestId('btn-load-more')).toBeDefined()
    expect(screen.getByTestId('btn-show-all')).toBeDefined()

    // Click Show All button
    fireEvent.click(screen.getByTestId('btn-show-all'))

    await waitFor(() => {
      // Now all 7 should be visible
      expect(screen.getByText('CE-2026-0006')).toBeDefined()
      expect(screen.getByText('CE-2026-0007')).toBeDefined()
    })

    // Collapse button appears
    expect(screen.getByTestId('btn-collapse')).toBeDefined()
    fireEvent.click(screen.getByTestId('btn-collapse'))

    await waitFor(() => {
      // Back to 5 complaints
      expect(screen.queryByText('CE-2026-0006')).toBeNull()
    })
  })

  it('filters complaints by keyword search', async () => {
    renderMyComplaints()

    await waitFor(() => {
      expect(screen.getByText('CE-2026-0001')).toBeDefined()
    })

    const searchInput = screen.getByTestId('input-search-complaints')
    fireEvent.change(searchInput, { target: { value: 'tree branch' } })

    await waitFor(() => {
      // Only CE-2026-0007 matches
      expect(screen.getByText('CE-2026-0007')).toBeDefined()
      expect(screen.queryByText('CE-2026-0001')).toBeNull()
    })
  })

  it('toggles between cards view and tabular view', async () => {
    renderMyComplaints()

    await waitFor(() => {
      expect(screen.getByTestId('complaints-card-grid')).toBeDefined()
    })

    const tableToggle = screen.getByTestId('toggle-view-table')
    fireEvent.click(tableToggle)

    await waitFor(() => {
      expect(screen.getByTestId('citizen-complaints-table')).toBeDefined()
      expect(screen.queryByTestId('complaints-card-grid')).toBeNull()
    })
  })
})
