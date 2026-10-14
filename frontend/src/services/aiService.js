import api from './api'

// Fallback rule dictionaries for client-side offline / mock mode
const CATEGORY_KEYWORDS = {
  ROADS_AND_FOOTPATHS: ['pothole', 'road', 'footpath', 'curb', 'crater', 'asphalt', 'sidewalk', 'tar', 'divider'],
  GARBAGE_AND_CLEANLINESS: ['garbage', 'trash', 'waste', 'dump', 'bin', 'litter', 'smell', 'debris', 'sweep'],
  WATER_SUPPLY_AND_LEAKAGE: ['water', 'pipe', 'leak', 'burst', 'contamination', 'pressure', 'tap', 'supply', 'pipeline'],
  DRAINAGE_AND_SEWAGE: ['drain', 'sewer', 'sewage', 'gutter', 'manhole', 'overflow', 'blockage', 'clogged'],
  STREETLIGHTS_AND_ELECTRICITY: ['light', 'lamp', 'streetlight', 'pole', 'wire', 'electric', 'dark', 'transformer', 'spark'],
  PUBLIC_HEALTH_HAZARDS: ['mosquito', 'dengue', 'chemical', 'hazard', 'health', 'vector', 'fogging', 'toxic'],
}

const CRITICAL_KEYWORDS = ['spark', 'fire', 'burst', 'open manhole', 'electrocution', 'severe flooding', 'hazard', 'hospital', 'school zone']
const HIGH_KEYWORDS = ['accident', 'danger', 'overflow', 'pothole', 'dark', 'leak', 'broken', 'deep crater']

const computeLocalFallbackRecommendation = ({ title = '', description = '', location = '' }) => {
  const text = `${title} ${description} ${location}`.toLowerCase()

  let detectedCategory = 'OTHER'
  let maxMatches = 0
  let matchedCategoryKeywords = []

  Object.entries(CATEGORY_KEYWORDS).forEach(([cat, keywords]) => {
    const matches = keywords.filter((kw) => text.includes(kw))
    if (matches.length > maxMatches) {
      maxMatches = matches.length
      detectedCategory = cat
      matchedCategoryKeywords = matches
    }
  })

  // Priority scoring
  let calculatedPriority = 'LOW'
  let matchedRiskKeywords = []

  if (CRITICAL_KEYWORDS.some((kw) => text.includes(kw))) {
    calculatedPriority = 'CRITICAL'
    matchedRiskKeywords = CRITICAL_KEYWORDS.filter((kw) => text.includes(kw))
  } else if (HIGH_KEYWORDS.some((kw) => text.includes(kw)) || maxMatches >= 2) {
    calculatedPriority = 'HIGH'
    matchedRiskKeywords = HIGH_KEYWORDS.filter((kw) => text.includes(kw))
  } else if (maxMatches > 0) {
    calculatedPriority = 'MEDIUM'
  }

  // Confidence calculation (0.60 to 0.95)
  const confidenceScore = Math.min(0.95, Math.max(0.65, 0.55 + maxMatches * 0.12))

  const departmentMapping = {
    ROADS_AND_FOOTPATHS: { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
    GARBAGE_AND_CLEANLINESS: { id: 4, name: 'Sanitation & Waste Management', code: 'SOLID_WASTE' },
    WATER_SUPPLY_AND_LEAKAGE: { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
    DRAINAGE_AND_SEWAGE: { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
    STREETLIGHTS_AND_ELECTRICITY: { id: 2, name: 'Electricity & Street Lighting', code: 'ELECTRICITY' },
    PUBLIC_HEALTH_HAZARDS: { id: 5, name: 'Public Health & Vector Control', code: 'HEALTH' },
    OTHER: { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
  }

  const dept = departmentMapping[detectedCategory] || departmentMapping.OTHER

  const explanationParts = []
  if (matchedCategoryKeywords.length > 0) {
    explanationParts.push(`Keywords detected: "${matchedCategoryKeywords.join(', ')}"`)
  }
  if (matchedRiskKeywords.length > 0) {
    explanationParts.push(`Urgency signals: "${matchedRiskKeywords.join(', ')}"`)
  }
  const explanation = explanationParts.length > 0
    ? explanationParts.join(' | ') + ` -> Routed to ${dept.name}`
    : `Standard civic routing based on complaint contents -> Routed to ${dept.name}`

  return {
    id: null,
    suggestedCategory: detectedCategory,
    suggestedPriority: calculatedPriority,
    suggestedDepartmentId: dept.id,
    suggestedDepartmentName: dept.name,
    suggestedDepartmentCode: dept.code,
    confidenceScore: parseFloat(confidenceScore.toFixed(2)),
    explanation,
    isOverridden: false,
    matchedKeywords: matchedCategoryKeywords.concat(matchedRiskKeywords),
  }
}

export const aiService = {
  /**
   * Get real-time AI recommendation from text inputs (title, description, location)
   */
  getRealtimeRecommendation: async ({ title, description = '', location = '' }) => {
    if (!title || title.trim().length < 3) {
      return null
    }

    try {
      const response = await api.post('/api/v1/ai/recommend', {
        title: title.trim(),
        description: description?.trim() || '',
        location: location?.trim() || '',
      })
      if (response.data && response.data.data) {
        return response.data.data
      }
    } catch {
      try {
        const legacyResponse = await api.post('/api/ai/recommend', {
          title: title.trim(),
          description: description?.trim() || '',
          location: location?.trim() || '',
        })
        if (legacyResponse.data && legacyResponse.data.data) {
          return legacyResponse.data.data
        }
      } catch {
        // Fallback to client-side rule evaluation for seamless UX
      }
    }

    return computeLocalFallbackRecommendation({ title, description, location })
  },

  /**
   * Fetch stored AI recommendation for an existing complaint
   */
  getComplaintRecommendation: async (complaintId) => {
    if (!complaintId) return null
    try {
      const response = await api.get(`/api/v1/complaints/${complaintId}/recommendation`)
      if (response.data && response.data.data) {
        return response.data.data
      }
    } catch {
      try {
        const legacyResponse = await api.get(`/api/complaints/${complaintId}/recommendation`)
        if (legacyResponse.data && legacyResponse.data.data) {
          return legacyResponse.data.data
        }
      } catch {
        // Not found or network failure
      }
    }
    return null
  },

  /**
   * 1-Click triage acceptance of AI suggestion by Admin
   */
  applyRecommendation: async (complaintId) => {
    try {
      const response = await api.post(`/api/v1/admin/complaints/${complaintId}/apply-recommendation`)
      if (response.data && response.data.data) {
        return response.data.data
      }
    } catch {
      const legacyResponse = await api.post(`/api/admin/complaints/${complaintId}/apply-recommendation`)
      if (legacyResponse.data && legacyResponse.data.data) {
        return legacyResponse.data.data
      }
    }
  },

  /**
   * Admin triage manual override with reason and audit logging
   */
  overrideRecommendation: async (complaintId, overrideData) => {
    try {
      const response = await api.post(`/api/v1/admin/complaints/${complaintId}/override-recommendation`, overrideData)
      if (response.data && response.data.data) {
        return response.data.data
      }
    } catch {
      const legacyResponse = await api.post(`/api/admin/complaints/${complaintId}/override-recommendation`, overrideData)
      if (legacyResponse.data && legacyResponse.data.data) {
        return legacyResponse.data.data
      }
    }
  },
}

export default aiService
