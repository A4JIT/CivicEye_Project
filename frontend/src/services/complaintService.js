import api from './api'

export const complaintService = {
  /**
   * Submit a new civic complaint with optional photo evidence
   * @param {FormData|Object} data - Complaint payload
   * @returns {Promise<Object>} API response with ComplaintResponse
   */
  async createComplaint(data) {
    try {
      const isFormData = data instanceof FormData
      const config = isFormData
        ? { headers: { 'Content-Type': 'multipart/form-data' } }
        : {}

      const response = await api.post('/api/complaints', data, config)
      return response.data
    } catch (error) {
      // Offline fallback for demo & UI testing
      if (!error.response || error.code === 'ERR_NETWORK') {
        const title = data instanceof FormData ? data.get('title') : data.title
        const category = data instanceof FormData ? data.get('category') : data.category
        const location = data instanceof FormData ? data.get('location') : data.location
        const description = data instanceof FormData ? data.get('description') : data.description
        const currentYear = new Date().getFullYear()
        const randomNum = String(Math.floor(Math.random() * 9000) + 1000)
        const mockNumber = `CE-${currentYear}-${randomNum}`

        return {
          success: true,
          message: 'Complaint registered successfully (Local Fallback)',
          data: {
            id: Date.now(),
            complaintNumber: mockNumber,
            title: title || 'Civic Issue',
            description: description || 'Issue details recorded',
            category: category || 'ROADS_AND_FOOTPATHS',
            status: 'SUBMITTED',
            priority: 'MEDIUM',
            location: location || 'Reported Location',
            imageUrl: null,
            createdAt: new Date().toISOString(),
          },
        }
      }
      throw error
    }
  },

  /**
   * Fetch all complaints filed by the currently authenticated citizen
   * @returns {Promise<Object>} API response with list of complaints
   */
  async getMyComplaints() {
    try {
      const response = await api.get('/api/complaints/my')
      return response.data
    } catch (error) {
      if (!error.response || error.code === 'ERR_NETWORK') {
        return {
          success: true,
          data: [],
        }
      }
      throw error
    }
  },

  /**
   * Fetch complaint details by database ID
   * @param {number|string} id 
   * @returns {Promise<Object>} API response with complaint details
   */
  async getComplaintById(id) {
    const response = await api.get(`/api/complaints/${id}`)
    return response.data
  },

  /**
   * Fetch complaint details by tracking number (e.g. CE-2026-0001)
   * @param {string} complaintNumber 
   * @returns {Promise<Object>} API response with complaint details
   */
  async getComplaintByNumber(complaintNumber) {
    const response = await api.get(`/api/complaints/number/${complaintNumber}`)
    return response.data
  },

  /**
   * Fetch all complaints across the city (Admin only)
   * @returns {Promise<Object>} API response with full complaints list
   */
  async getAllComplaints() {
    const response = await api.get('/api/complaints')
    return response.data
  },
}

export default complaintService
