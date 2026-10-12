import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthContext } from '../src/context/AuthContext'
import CitizenDashboard from '../src/pages/citizen/CitizenDashboard'
import AdminDashboard from '../src/pages/admin/AdminDashboard'
import DepartmentDashboard from '../src/pages/department/DepartmentDashboard'
import Sidebar from '../src/components/layout/Sidebar'
import dashboardService from '../src/services/dashboardService'

const mockCitizenUser = {
  id: 2,
  username: 'jane_doe',
  fullName: 'Jane Doe',
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
  id: 3,
  username: 'officer_sharma',
  fullName: 'Rajesh Sharma',
  email: 'dept_roads@civiceye.com',
  role: 'DEPARTMENT_USER',
}

const mockCitizenData = {
  citizenId: 2,
  citizenName: 'Jane Doe',
  totalComplaints: 5,
  pendingComplaints: 1,
  inProgressComplaints: 2,
  resolvedComplaints: 2,
  recentComplaints: [
    {
      id: 1,
      trackingNumber: 'CE-20261012-0001',
      title: 'Deep pothole causing vehicle damage near Metro Gate 2',
      category: 'POTHOLE',
      priority: 'HIGH',
      status: 'IN_PROGRESS',
      departmentName: 'Roads & Traffic',
      addressText: 'MG Road, near Metro Station Gate 2',
    },
  ],
}

const mockAdminData = {
  totalComplaints: 15,
  pendingComplaints: 4,
  inProgressComplaints: 6,
  resolvedComplaints: 5,
  totalCitizens: 42,
  totalDepartments: 4,
  criticalComplaints: 3,
  complaintsByDepartment: {
    'Roads & Traffic': 6,
    'Water Supply': 4,
  },
  complaintsByCategory: {
    POTHOLE: 6,
    WATER_LEAKAGE: 4,
  },
  recentComplaints: [
    {
      id: 1,
      trackingNumber: 'CE-20261012-0001',
      title: 'Deep pothole causing vehicle damage near Metro Gate 2',
      category: 'POTHOLE',
      priority: 'HIGH',
      status: 'IN_PROGRESS',
      departmentName: 'Roads & Traffic',
      citizenName: 'Jane Doe',
    },
  ],
}

const mockDeptData = {
  departmentId: 1,
  departmentName: 'Roads & Traffic',
  departmentCode: 'ROADS',
  totalAssigned: 8,
  pendingComplaints: 3,
  inProgressComplaints: 3,
  resolvedComplaints: 2,
  criticalCount: 2,
  highCount: 3,
  mediumCount: 2,
  lowCount: 1,
  recentComplaints: [
    {
      id: 1,
      trackingNumber: 'CE-20261012-0001',
      title: 'Deep pothole causing vehicle damage near Metro Gate 2',
      priority: 'CRITICAL',
      status: 'IN_PROGRESS',
      addressText: 'MG Road Junction',
    },
  ],
}

const renderWithAuth = (ui, user = mockCitizenUser) => {
  const authValue = {
    user,
    token: 'mock-jwt-token',
    isAuthenticated: true,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
  }

  return render(
    <BrowserRouter>
      <AuthContext.Provider value={authValue}>
        {ui}
      </AuthContext.Provider>
    </BrowserRouter>
  )
}

describe('Dashboard Component Suite', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(dashboardService, 'getCitizenDashboard').mockResolvedValue(mockCitizenData)
    vi.spyOn(dashboardService, 'getAdminDashboard').mockResolvedValue(mockAdminData)
    vi.spyOn(dashboardService, 'getDepartmentDashboard').mockResolvedValue(mockDeptData)
  })

  describe('CitizenDashboard', () => {
    it('renders greeting, KPI stat cards, and quick report button', async () => {
      renderWithAuth(<CitizenDashboard />, mockCitizenUser)

      await waitFor(() => {
        expect(screen.getByText(/Total Reported/i)).toBeDefined()
        expect(screen.getByText(/Pending Triage/i)).toBeDefined()
        expect(screen.getAllByText(/In Progress/i).length).toBeGreaterThan(0)
      })

      expect(screen.getByText(/Report New Civic Issue/i)).toBeDefined()
      expect(screen.getByText(/Recent Submissions/i)).toBeDefined()
    })

    it('renders recent complaints table with tracking codes and status tags', async () => {
      renderWithAuth(<CitizenDashboard />, mockCitizenUser)

      await waitFor(() => {
        expect(screen.getByTestId('citizen-complaints-table')).toBeDefined()
        expect(screen.getByText('CE-20261012-0001')).toBeDefined()
      })
    })
  })

  describe('AdminDashboard', () => {
    it('renders system command center KPIs and health status banner', async () => {
      renderWithAuth(<AdminDashboard />, mockAdminUser)

      await waitFor(() => {
        expect(screen.getByText(/City Operations Command Center/i)).toBeDefined()
        expect(screen.getByText(/Total Complaints/i)).toBeDefined()
        expect(screen.getByText(/Critical Urgent/i)).toBeDefined()
      })

      expect(screen.getByText(/AI Categorization Engine/i)).toBeDefined()
    })

    it('renders department workload distribution and category chips', async () => {
      renderWithAuth(<AdminDashboard />, mockAdminUser)

      await waitFor(() => {
        expect(screen.getByTestId('department-workload-panel')).toBeDefined()
        expect(screen.getByTestId('category-distribution-panel')).toBeDefined()
        expect(screen.getAllByText('Roads & Traffic').length).toBeGreaterThan(0)
      })
    })
  })

  describe('DepartmentDashboard', () => {
    it('renders department console and assigned workload cards', async () => {
      renderWithAuth(<DepartmentDashboard />, mockDeptUser)

      await waitFor(() => {
        expect(screen.getByText(/Assigned Workload/i)).toBeDefined()
        expect(screen.getByText(/Pending Action/i)).toBeDefined()
        expect(screen.getByText(/Severity Breakdown/i)).toBeDefined()
      })
    })

    it('renders urgent alert notice and department work queue table', async () => {
      renderWithAuth(<DepartmentDashboard />, mockDeptUser)

      await waitFor(() => {
        expect(screen.getByTestId('dept-complaints-table')).toBeDefined()
        expect(screen.getByTestId('critical-alert-banner')).toBeDefined()
      })
    })
  })

  describe('Sidebar Navigation', () => {
    it('renders role-appropriate links and allows toggling collapse', () => {
      renderWithAuth(<Sidebar />, mockCitizenUser)

      expect(screen.getByText('Overview')).toBeDefined()
      expect(screen.getByText('Report Issue')).toBeDefined()
      expect(screen.getByText('My Complaints')).toBeDefined()

      const toggleBtn = screen.getByLabelText(/toggle sidebar/i)
      fireEvent.click(toggleBtn)

      const sidebarEl = screen.getByTestId('dashboard-sidebar')
      expect(sidebarEl.className).toContain('collapsed')
    })
  })
})
