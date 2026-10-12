import api from './api'

/**
 * Service to interact with role-based dashboard endpoints
 */

const FALLBACK_CITIZEN_DATA = {
  citizenId: 2,
  citizenName: 'Jane Doe',
  totalComplaints: 3,
  pendingComplaints: 1,
  inProgressComplaints: 1,
  resolvedComplaints: 1,
  rejectedComplaints: 0,
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
      createdAt: '2026-10-12T10:30:00Z',
    },
    {
      id: 2,
      trackingNumber: 'CE-20261012-0002',
      title: 'Water pipe leak flooding pedestrian walkway',
      category: 'WATER_LEAKAGE',
      priority: 'CRITICAL',
      status: 'ASSIGNED',
      departmentName: 'Water Supply',
      addressText: '14th Cross, Indiranagar',
      createdAt: '2026-10-12T11:15:00Z',
    },
    {
      id: 3,
      trackingNumber: 'CE-20261011-0001',
      title: 'Broken streetlight opposite Central Park',
      category: 'STREETLIGHT',
      priority: 'MEDIUM',
      status: 'RESOLVED',
      departmentName: 'Electricity & Lighting',
      addressText: 'Central Park West Avenue',
      createdAt: '2026-10-11T16:00:00Z',
    },
  ],
}

const FALLBACK_ADMIN_DATA = {
  totalComplaints: 18,
  pendingComplaints: 4,
  inProgressComplaints: 8,
  resolvedComplaints: 5,
  rejectedComplaints: 1,
  totalCitizens: 42,
  totalDepartments: 4,
  criticalComplaints: 3,
  highComplaints: 7,
  complaintsByDepartment: {
    'Roads & Traffic': 6,
    'Water Supply': 4,
    'Sanitation & Waste': 5,
    'Electricity & Lighting': 3,
  },
  complaintsByCategory: {
    POTHOLE: 6,
    WATER_LEAKAGE: 4,
    GARBAGE: 5,
    STREETLIGHT: 3,
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
      addressText: 'MG Road, near Metro Station Gate 2',
      createdAt: '2026-10-12T10:30:00Z',
    },
    {
      id: 2,
      trackingNumber: 'CE-20261012-0002',
      title: 'Water pipe leak flooding pedestrian walkway',
      category: 'WATER_LEAKAGE',
      priority: 'CRITICAL',
      status: 'ASSIGNED',
      departmentName: 'Water Supply',
      citizenName: 'Amit Patel',
      addressText: '14th Cross, Indiranagar',
      createdAt: '2026-10-12T11:15:00Z',
    },
    {
      id: 3,
      trackingNumber: 'CE-20261012-0003',
      title: 'Overflowing commercial dumpster blocking driveway',
      category: 'GARBAGE',
      priority: 'MEDIUM',
      status: 'SUBMITTED',
      departmentName: 'Sanitation & Waste',
      citizenName: 'Sarah Jenkins',
      addressText: 'Commercial Street Lane 4',
      createdAt: '2026-10-12T12:00:00Z',
    },
  ],
}

const FALLBACK_DEPARTMENT_DATA = {
  departmentId: 1,
  departmentName: 'Roads & Traffic',
  departmentCode: 'ROADS',
  totalAssigned: 6,
  pendingComplaints: 2,
  inProgressComplaints: 3,
  resolvedComplaints: 1,
  criticalCount: 1,
  highCount: 3,
  mediumCount: 2,
  lowCount: 0,
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
      createdAt: '2026-10-12T10:30:00Z',
    },
    {
      id: 4,
      trackingNumber: 'CE-20261012-0004',
      title: 'Faded pedestrian crosswalk and broken speed breaker',
      category: 'POTHOLE',
      priority: 'CRITICAL',
      status: 'ASSIGNED',
      departmentName: 'Roads & Traffic',
      addressText: 'Ring Road Junction',
      createdAt: '2026-10-12T09:00:00Z',
    },
  ],
}

export const dashboardService = {
  /**
   * Fetch Citizen dashboard metrics
   */
  async getCitizenDashboard(citizenId) {
    try {
      const response = await api.get('/api/dashboard/citizen', {
        params: citizenId ? { citizenId } : {},
      })
      if (response.data && response.data.data) {
        return response.data.data
      }
      return FALLBACK_CITIZEN_DATA
    } catch (error) {
      console.warn('Backend unavailable, using fallback citizen metrics:', error.message)
      return FALLBACK_CITIZEN_DATA
    }
  },

  /**
   * Fetch Administrator system-wide KPI metrics
   */
  async getAdminDashboard() {
    try {
      const response = await api.get('/api/dashboard/admin')
      if (response.data && response.data.data) {
        return response.data.data
      }
      return FALLBACK_ADMIN_DATA
    } catch (error) {
      console.warn('Backend unavailable, using fallback admin metrics:', error.message)
      return FALLBACK_ADMIN_DATA
    }
  },

  /**
   * Fetch Department Officer dashboard metrics
   */
  async getDepartmentDashboard(departmentId = 1) {
    try {
      const response = await api.get('/api/dashboard/department', {
        params: { departmentId },
      })
      if (response.data && response.data.data) {
        return response.data.data
      }
      return FALLBACK_DEPARTMENT_DATA
    } catch (error) {
      console.warn('Backend unavailable, using fallback department metrics:', error.message)
      return FALLBACK_DEPARTMENT_DATA
    }
  },
}

export default dashboardService
