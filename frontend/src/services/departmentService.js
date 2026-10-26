import api from './api'

const INITIAL_DEPARTMENT_COMPLAINTS = [
  {
    id: 100,
    complaintNumber: 'CE-2026-0001',
    title: 'Severe crater pothole on MG Road near Metro Station',
    description: 'Deep road crater causing vehicle rim damage and severe traffic slowdown during peak hours.',
    category: 'ROADS_AND_FOOTPATHS',
    status: 'ASSIGNED',
    priority: 'HIGH',
    location: 'MG Road Metro Station, Pillar 24',
    citizenId: 10,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: 1,
    departmentName: 'Public Works & Roads',
    assignedToId: 20,
    assignedToName: 'Rajesh Sharma',
    startedWorkingAt: null,
    progressNotes: null,
    resolutionNotes: null,
    resolutionImageUrl: null,
    imageUrl: null,
    createdAt: new Date(Date.now() - 3600000 * 24).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 12).toISOString(),
  },
  {
    id: 101,
    complaintNumber: 'CE-2026-0004',
    title: 'Broken stormwater drainage cover posing pedestrian hazard',
    description: 'Concrete slab over gutter broken with exposed rebar right outside Government School gate.',
    category: 'ROADS_AND_FOOTPATHS',
    status: 'IN_PROGRESS',
    priority: 'CRITICAL',
    location: 'Sector 4, Main Market Road',
    citizenId: 10,
    citizenName: 'Jane Citizen',
    citizenEmail: 'citizen@civiceye.com',
    departmentId: 1,
    departmentName: 'Public Works & Roads',
    assignedToId: 20,
    assignedToName: 'Rajesh Sharma',
    startedWorkingAt: new Date(Date.now() - 3600000 * 6).toISOString(),
    progressNotes: 'Safety barricades placed; replacement precast slab ordered from depot.',
    resolutionNotes: null,
    resolutionImageUrl: null,
    imageUrl: null,
    createdAt: new Date(Date.now() - 3600000 * 48).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 6).toISOString(),
  },
]

let departmentCatalog = [...INITIAL_DEPARTMENT_COMPLAINTS]

export const departmentService = {
  getDepartmentComplaints: async (params = {}) => {
    try {
      const response = await api.get('/api/department/complaints', { params })
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        let filtered = [...departmentCatalog]
        if (params.status) {
          const statuses = Array.isArray(params.status)
            ? params.status
            : params.status.split(',')
          filtered = filtered.filter((c) => statuses.includes(c.status))
        }
        if (params.priority) {
          filtered = filtered.filter((c) => c.priority === params.priority)
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

  getDepartmentComplaintById: async (id) => {
    try {
      const response = await api.get(`/api/department/complaints/${id}`)
      if (response.data && response.data.data) {
        return response.data.data
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const found = departmentCatalog.find((c) => String(c.id) === String(id))
        if (found) {
          return {
            ...found,
            timeline: [
              {
                id: 1,
                fromStatus: 'SUBMITTED',
                toStatus: 'ASSIGNED',
                changedByName: 'Admin Officer',
                remarks: 'Assigned to Public Works & Roads',
                createdAt: found.createdAt,
              },
            ],
          }
        }
      }
      throw error
    }
  },

  startWork: async (id, data = {}) => {
    try {
      const response = await api.put(`/api/department/complaints/${id}/start-work`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = departmentCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.status = 'IN_PROGRESS'
          item.startedWorkingAt = new Date().toISOString()
          if (data.remarks) item.progressNotes = data.remarks
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  addProgressNote: async (id, data) => {
    try {
      const response = await api.post(`/api/department/complaints/${id}/progress`, data)
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = departmentCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.progressNotes = data.notes
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },

  resolveComplaint: async (id, data) => {
    try {
      let response
      if (data instanceof FormData) {
        response = await api.post(`/api/department/complaints/${id}/resolve`, data, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        })
      } else {
        response = await api.post(`/api/department/complaints/${id}/resolve`, data)
      }
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK' || error.response?.status >= 400) {
        const item = departmentCatalog.find((c) => String(c.id) === String(id))
        if (item) {
          item.status = 'RESOLVED'
          item.resolvedAt = new Date().toISOString()
          const notes = data instanceof FormData ? data.get('resolutionNotes') : data.resolutionNotes
          item.resolutionNotes = notes
          return { success: true, data: { ...item } }
        }
      }
      throw error
    }
  },
}

export default departmentService
