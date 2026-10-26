import api from './api'

const MOCK_DEPARTMENTS = [
  { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
  { id: 2, name: 'Electricity & Street Lighting', code: 'ELECTRICITY' },
  { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
  { id: 4, name: 'Sanitation & Waste Management', code: 'SOLID_WASTE' },
  { id: 5, name: 'Public Health & Vector Control', code: 'HEALTH' },
]

const INITIAL_MOCK_COMPLAINTS = [
  {
    id: 1,
    complaintNumber: 'CE-2026-0001',
    title: 'Severe crater pothole on MG Road near Metro Station',
    description: 'Deep road crater causing vehicle rim damage and severe traffic slowdown during peak hours.',
    category: 'ROADS_AND_FOOTPATHS',
    status: 'SUBMITTED',
    priority: 'HIGH',
    location: 'MG Road Metro Station, Pillar 24',
    citizenId: 2,
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
    createdAt: new Date(Date.now() - 3600000 * 5).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 5).toISOString(),
  },
  {
    id: 2,
    complaintNumber: 'CE-2026-0002',
    title: 'Street lights flickering and failing on 5th Main Avenue',
    description: 'Series of 4 LED street lamp posts completely dark since Monday evening creating safety risks.',
    category: 'ELECTRICITY',
    status: 'UNDER_REVIEW',
    priority: 'MEDIUM',
    location: '5th Main Avenue, Sector 9',
    citizenId: 2,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: 2,
    departmentName: 'Electricity & Street Lighting',
    aiCategoryOverridden: false,
    aiPriorityOverridden: false,
    adminNotes: 'Field inspection scheduled for tonight lighting crew.',
    needsInfoRemarks: null,
    rejectionReason: null,
    imageUrl: null,
    createdAt: new Date(Date.now() - 3600000 * 18).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 12).toISOString(),
  },
  {
    id: 3,
    complaintNumber: 'CE-2026-0003',
    title: 'Unclear property dispute report',
    description: 'Someone parked their vehicle in front of the gate and spoke rudely.',
    category: 'OTHER',
    status: 'NEEDS_INFORMATION',
    priority: 'LOW',
    location: 'Green Glen Layout, Gate 3',
    citizenId: 2,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: null,
    departmentName: null,
    aiCategoryOverridden: false,
    aiPriorityOverridden: false,
    adminNotes: 'Awaiting citizen clarification on public encroachment vs private parking.',
    needsInfoRemarks: 'Please clarify if the parked vehicle is on a municipal road blocking public passage.',
    rejectionReason: null,
    imageUrl: null,
    createdAt: new Date(Date.now() - 3600000 * 28).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 14).toISOString(),
  },
  {
    id: 4,
    complaintNumber: 'CE-2026-0004',
    title: 'Major drinking water main pipeline leak',
    description: 'Treated water gushing into storm drain at high velocity since 4 AM.',
    category: 'WATER_AND_SEWAGE',
    status: 'ASSIGNED',
    priority: 'CRITICAL',
    location: 'Ring Road Junction, Ward 12',
    citizenId: 2,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: 3,
    departmentName: 'Water Supply & Sewerage',
    aiCategoryOverridden: true,
    aiPriorityOverridden: true,
    adminNotes: 'Overridden priority to CRITICAL due to volume of water loss.',
    needsInfoRemarks: null,
    rejectionReason: null,
    imageUrl: null,
    createdAt: new Date(Date.now() - 3600000 * 36).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 20).toISOString(),
  },
  {
    id: 5,
    complaintNumber: 'CE-2026-0005',
    title: 'Private residential wall painting argument',
    description: 'Neighbor selected an ugly paint color for their front boundary wall.',
    category: 'OTHER',
    status: 'REJECTED',
    priority: 'LOW',
    location: 'Flat 402, Green Avenue',
    citizenId: 2,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: null,
    departmentName: null,
    aiCategoryOverridden: false,
    aiPriorityOverridden: false,
    adminNotes: null,
    needsInfoRemarks: null,
    rejectionReason: '[OUT_OF_JURISDICTION] Private residential color aesthetic dispute outside municipal authority.',
    imageUrl: null,
    createdAt: new Date(Date.now() - 3600000 * 48).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 24).toISOString(),
  },
]

let memoryCatalog = [...INITIAL_MOCK_COMPLAINTS]

const INITIAL_MOCK_STAFF = [
  {
    id: 3,
    username: 'officer_sharma',
    fullName: 'Rajesh Sharma',
    email: 'dept_roads@civiceye.com',
    role: 'DEPARTMENT_USER',
    departmentId: 1,
    departmentName: 'Public Works & Roads',
    departmentCode: 'ROAD_WORKS',
    isActive: true,
    createdAt: new Date(Date.now() - 3600000 * 24 * 10).toISOString(),
  },
  {
    id: 4,
    username: 'officer_kumar',
    fullName: 'Anil Kumar',
    email: 'dept_water@civiceye.com',
    role: 'DEPARTMENT_USER',
    departmentId: 3,
    departmentName: 'Water Supply & Sewerage',
    departmentCode: 'WATER_SUPPLY',
    isActive: true,
    createdAt: new Date(Date.now() - 3600000 * 24 * 7).toISOString(),
  },
  {
    id: 5,
    username: 'officer_priya',
    fullName: 'Priya Sundaram',
    email: 'dept_power@civiceye.com',
    role: 'DEPARTMENT_USER',
    departmentId: 2,
    departmentName: 'Electricity & Street Lighting',
    departmentCode: 'ELECTRICITY',
    isActive: true,
    createdAt: new Date(Date.now() - 3600000 * 24 * 4).toISOString(),
  },
]

let memoryStaff = [...INITIAL_MOCK_STAFF]

export const adminService = {
  getDepartments: async () => {
    try {
      const response = await api.get('/departments')
      if (response.data && response.data.data) {
        return response.data.data
      }
    } catch {
      // Fallback to standard municipal catalog
    }
    return MOCK_DEPARTMENTS
  },

  getAllComplaints: async (params = {}) => {
    try {
      const response = await api.get('/api/admin/complaints', { params })
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        // Filter in memory for fallback
        let filtered = [...memoryCatalog]
        if (params.status) {
          const statuses = Array.isArray(params.status)
            ? params.status
            : params.status.split(',')
          filtered = filtered.filter((c) => statuses.includes(c.status))
        }
        if (params.priority) {
          filtered = filtered.filter((c) => c.priority === params.priority)
        }
        if (params.category) {
          filtered = filtered.filter((c) => c.category === params.category)
        }
        if (params.departmentId) {
          filtered = filtered.filter((c) => Number(c.departmentId) === Number(params.departmentId))
        }
        if (params.search) {
          const q = params.search.toLowerCase()
          filtered = filtered.filter(
            (c) =>
              c.title.toLowerCase().includes(q) ||
              c.complaintNumber.toLowerCase().includes(q) ||
              c.location.toLowerCase().includes(q) ||
              c.description.toLowerCase().includes(q)
          )
        }

        return {
          complaints: filtered,
          totalItems: filtered.length,
          totalPages: 1,
          currentPage: 0,
        }
      }
      throw error
    }
  },

  getComplaintById: async (id) => {
    try {
      const response = await api.get(`/api/complaints/${id}`)
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const found = memoryCatalog.find((c) => String(c.id) === String(id))
        if (found) return found
      }
      throw error
    }
  },

  reviewComplaint: async (id, data) => {
    try {
      const response = await api.put(`/api/admin/complaints/${id}/review`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = memoryCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.status = 'UNDER_REVIEW'
          if (data.notes) item.adminNotes = data.notes
          if (data.category) {
            item.category = data.category
            item.aiCategoryOverridden = true
          }
          if (data.priority) {
            item.priority = data.priority
            item.aiPriorityOverridden = true
          }
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  rejectComplaint: async (id, data) => {
    try {
      const response = await api.put(`/api/admin/complaints/${id}/reject`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = memoryCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.status = 'REJECTED'
          const prefix = data.rejectionCategory ? `[${data.rejectionCategory}] ` : ''
          item.rejectionReason = `${prefix}${data.rejectionReason}`
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  requestInformation: async (id, data) => {
    try {
      const response = await api.put(`/api/admin/complaints/${id}/request-info`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = memoryCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.status = 'NEEDS_INFORMATION'
          item.needsInfoRemarks = data.remarks
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  assignDepartment: async (id, data) => {
    try {
      const response = await api.put(`/api/admin/complaints/${id}/assign`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = memoryCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.departmentId = data.departmentId
          const dept = MOCK_DEPARTMENTS.find((d) => d.id === Number(data.departmentId))
          item.departmentName = dept ? dept.name : 'Assigned Department'
          if (data.priority) {
            item.priority = data.priority
            item.aiPriorityOverridden = true
          }
          item.status = 'ASSIGNED'
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  closeComplaint: async (id, data = {}) => {
    try {
      const response = await api.post(`/api/admin/complaints/${id}/close`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = memoryCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.status = 'CLOSED'
          item.closedAt = new Date().toISOString()
          if (data.closureRemarks) item.closureRemarks = data.closureRemarks
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  getDepartmentStaff: async () => {
    try {
      const response = await api.get('/api/admin/department-staff')
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        return [...memoryStaff]
      }
      throw error
    }
  },

  createDepartmentStaff: async (staffData) => {
    try {
      const response = await api.post('/api/admin/department-staff', staffData)
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const dept = MOCK_DEPARTMENTS.find((d) => d.id === Number(staffData.departmentId))
        const newStaff = {
          id: Date.now(),
          username: staffData.username,
          fullName: staffData.fullName,
          email: staffData.email,
          role: 'DEPARTMENT_USER',
          departmentId: Number(staffData.departmentId),
          departmentName: dept ? dept.name : 'Municipal Department',
          departmentCode: dept ? dept.code : 'DEPT',
          isActive: true,
          createdAt: new Date().toISOString(),
        }
        memoryStaff.unshift(newStaff)
        return newStaff
      }
      throw error
    }
  },

  toggleStaffStatus: async (id) => {
    try {
      const response = await api.patch(`/api/admin/department-staff/${id}/toggle-status`)
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const staff = memoryStaff.find((s) => String(s.id) === String(id))
        if (staff) {
          staff.isActive = !staff.isActive
          return { ...staff }
        }
      }
      throw error
    }
  },
}

export default adminService
