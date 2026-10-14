import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import complaintService from '../../services/complaintService'
import aiService from '../../services/aiService'
import Loader from '../../components/common/Loader'

const CATEGORIES = [
  {
    id: 'ROADS_AND_FOOTPATHS',
    label: 'Roads & Footpaths',
    icon: '🛣️',
    description: 'Potholes, broken pavement, craters & curbs',
  },
  {
    id: 'GARBAGE_AND_CLEANLINESS',
    label: 'Garbage & Waste',
    icon: '🗑️',
    description: 'Overflowing bins, uncollected trash & litter',
  },
  {
    id: 'WATER_SUPPLY_AND_LEAKAGE',
    label: 'Water Supply',
    icon: '💧',
    description: 'Pipeline leaks, contaminated water & low pressure',
  },
  {
    id: 'DRAINAGE_AND_SEWAGE',
    label: 'Drainage & Sewage',
    icon: '🚰',
    description: 'Blocked gutters, manhole issues & overflow',
  },
  {
    id: 'STREETLIGHTS_AND_ELECTRICITY',
    label: 'Streetlights & Power',
    icon: '💡',
    description: 'Dark streets, flickering lamps & dangling wires',
  },
  {
    id: 'PUBLIC_HEALTH_HAZARDS',
    label: 'Public Health Hazards',
    icon: '⚠️',
    description: 'Mosquito breeding, chemical hazard & stray risks',
  },
  {
    id: 'OTHER',
    label: 'Other Civic Issue',
    icon: '📌',
    description: 'Miscellaneous civic defects and concerns',
  },
]

const CreateComplaint = () => {
  const navigate = useNavigate()
  const fileInputRef = useRef(null)

  const [formData, setFormData] = useState({
    title: '',
    category: '',
    description: '',
    location: '',
    latitude: '',
    longitude: '',
  })

  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [dragActive, setDragActive] = useState(false)

  const [geoStatus, setGeoStatus] = useState({ loading: false, success: false, error: null })
  const [submitting, setSubmitting] = useState(false)
  const [formErrors, setFormErrors] = useState({})
  const [serverError, setServerError] = useState(null)
  const [submittedComplaint, setSubmittedComplaint] = useState(null)
  const [copied, setCopied] = useState(false)

  // AI Real-Time Suggestion States
  const [aiSuggestion, setAiSuggestion] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)

  // Real-time debounced AI recommendation calculation
  useEffect(() => {
    if (!formData.title || formData.title.trim().length < 3) {
      setAiSuggestion(null)
      return
    }

    setAiLoading(true)
    const timer = setTimeout(async () => {
      try {
        const suggestion = await aiService.getRealtimeRecommendation({
          title: formData.title,
          description: formData.description,
          location: formData.location,
        })
        setAiSuggestion(suggestion)
      } catch (err) {
        console.error('Failed to fetch real-time AI recommendation', err)
      } finally {
        setAiLoading(false)
      }
    }, 350)

    return () => clearTimeout(timer)
  }, [formData.title, formData.description, formData.location])

  const normalizeCategory = (cat) => {
    if (!cat) return ''
    if (cat === 'WATER_SUPPLY') return 'WATER_SUPPLY_AND_LEAKAGE'
    if (cat === 'SOLID_WASTE' || cat === 'SANITATION') return 'GARBAGE_AND_CLEANLINESS'
    if (cat === 'ELECTRICITY') return 'STREETLIGHTS_AND_ELECTRICITY'
    return cat
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (formErrors[name]) {
      setFormErrors((prev) => ({ ...prev, [name]: null }))
    }
  }

  const handleCategorySelect = (categoryId) => {
    setFormData((prev) => ({ ...prev, category: categoryId }))
    if (formErrors.category) {
      setFormErrors((prev) => ({ ...prev, category: null }))
    }
  }

  const validateFile = (file) => {
    if (!file) return null
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
    if (!allowedTypes.includes(file.type.toLowerCase())) {
      return 'Only JPG, PNG, and WEBP image formats are supported.'
    }
    const maxSize = 5 * 1024 * 1024 // 5 MB
    if (file.size > maxSize) {
      return 'File size cannot exceed 5 MB.'
    }
    return null
  }

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      const error = validateFile(file)
      if (error) {
        setFormErrors((prev) => ({ ...prev, image: error }))
        return
      }
      setFormErrors((prev) => ({ ...prev, image: null }))
      setImageFile(file)
      setImagePreview(URL.createObjectURL(file))
    }
  }

  const handleDrag = (e) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true)
    } else if (e.type === 'dragleave') {
      setDragActive(false)
    }
  }

  const handleDrop = (e) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0]
      const error = validateFile(file)
      if (error) {
        setFormErrors((prev) => ({ ...prev, image: error }))
        return
      }
      setFormErrors((prev) => ({ ...prev, image: null }))
      setImageFile(file)
      setImagePreview(URL.createObjectURL(file))
    }
  }

  const removeImage = () => {
    setImageFile(null)
    setImagePreview(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
    setFormErrors((prev) => ({ ...prev, image: null }))
  }

  const handleDetectLocation = () => {
    if (!navigator.geolocation) {
      setGeoStatus({ loading: false, success: false, error: 'Geolocation is not supported by your browser.' })
      return
    }

    setGeoStatus({ loading: true, success: false, error: null })
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude.toFixed(6)
        const lng = position.coords.longitude.toFixed(6)
        setFormData((prev) => ({
          ...prev,
          latitude: lat,
          longitude: lng,
          location: prev.location || `GPS Location: Lat ${lat}, Lng ${lng}`,
        }))
        setGeoStatus({ loading: false, success: true, error: null })
      },
      (error) => {
        setGeoStatus({
          loading: false,
          success: false,
          error: error.message || 'Unable to retrieve location permission.',
        })
      },
      { timeout: 10000, enableHighAccuracy: true }
    )
  }

  const validate = () => {
    const errors = {}
    if (!formData.title.trim()) {
      errors.title = 'Title is required'
    } else if (formData.title.length > 150) {
      errors.title = 'Title cannot exceed 150 characters'
    }

    if (!formData.category) {
      errors.category = 'Please select an issue category'
    }

    if (!formData.description.trim()) {
      errors.description = 'Please provide detailed description of the issue'
    }

    if (!formData.location.trim()) {
      errors.location = 'Location address or landmark is required'
    }

    setFormErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return

    setSubmitting(true)
    setServerError(null)

    try {
      const payload = new FormData()
      payload.append('title', formData.title.trim())
      payload.append('category', formData.category)
      payload.append('description', formData.description.trim())
      payload.append('location', formData.location.trim())
      if (formData.latitude) payload.append('latitude', formData.latitude)
      if (formData.longitude) payload.append('longitude', formData.longitude)
      if (imageFile) payload.append('image', imageFile)

      const response = await complaintService.createComplaint(payload)
      if (response?.success && response?.data) {
        setSubmittedComplaint(response.data)
      } else {
        throw new Error(response?.message || 'Failed to submit complaint')
      }
    } catch (err) {
      console.error('Complaint submission error:', err)
      const errorMsg =
        err.response?.data?.message ||
        err.message ||
        'Unable to register complaint. Please check your network and try again.'
      setServerError(errorMsg)
    } finally {
      setSubmitting(false)
    }
  }

  const copyComplaintNumber = () => {
    if (submittedComplaint?.complaintNumber) {
      navigator.clipboard.writeText(submittedComplaint.complaintNumber)
      setCopied(true)
      setTimeout(() => setCopied(false), 3000)
    }
  }

  const resetForm = () => {
    setFormData({
      title: '',
      category: '',
      description: '',
      location: '',
      latitude: '',
      longitude: '',
    })
    removeImage()
    setSubmittedComplaint(null)
    setFormErrors({})
    setServerError(null)
    setAiSuggestion(null)
  }

  // Success Confirmation Screen
  if (submittedComplaint) {
    return (
      <div className="create-complaint-container" data-testid="complaint-success-screen">
        <div className="complaint-success-card">
          <div className="success-icon-badge">✓</div>
          <span className="success-tag">Registration Completed</span>
          <h2 className="success-title">Complaint Successfully Registered!</h2>
          <p className="success-desc">
            Your complaint has been submitted to the municipal authority. Our AI rule engine has assigned an initial
            triage priority, and an administrator will review and route it to the concerned department.
          </p>

          <div className="complaint-id-box">
            <div className="complaint-id-label">YOUR UNIQUE COMPLAINT ID</div>
            <div className="complaint-id-value" data-testid="complaint-number-display">
              {submittedComplaint.complaintNumber}
            </div>
            <button
              type="button"
              className={`btn-copy ${copied ? 'copied' : ''}`}
              onClick={copyComplaintNumber}
              data-testid="btn-copy-id"
            >
              {copied ? '✓ Copied to Clipboard' : '📋 Copy Complaint ID'}
            </button>
          </div>

          <div className="complaint-summary-preview">
            <div className="summary-row">
              <span className="summary-label">Issue Title:</span>
              <span className="summary-val">{submittedComplaint.title}</span>
            </div>
            <div className="summary-row">
              <span className="summary-label">Category:</span>
              <span className="summary-val badge-category">
                {submittedComplaint.category?.replace(/_/g, ' ')}
              </span>
            </div>
            <div className="summary-row">
              <span className="summary-label">Location:</span>
              <span className="summary-val">{submittedComplaint.location}</span>
            </div>
            <div className="summary-row">
              <span className="summary-label">Initial Status:</span>
              <span className="summary-val status-badge status-submitted">
                {submittedComplaint.status || 'SUBMITTED'}
              </span>
            </div>
            <div className="summary-row">
              <span className="summary-label">Initial Priority:</span>
              <span className="summary-val priority-badge priority-medium">
                {submittedComplaint.priority || 'MEDIUM'}
              </span>
            </div>
          </div>

          <div className="success-actions">
            <Link to="/dashboard" className="btn btn-primary" id="btn-back-dashboard">
              📊 Back to Dashboard
            </Link>
            <button
              type="button"
              className="btn btn-secondary"
              id="btn-submit-another"
              onClick={resetForm}
            >
              ➕ Register Another Issue
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="create-complaint-container">
      <div className="create-complaint-header">
        <div className="header-badge">🏛️ CivicEye Citizen Portal</div>
        <h1 className="page-title">Register a Civic Complaint</h1>
        <p className="page-subtitle">
          Report municipal issues directly to the authorities with photo evidence and accurate location data.
        </p>
      </div>

      {serverError && (
        <div className="alert-error" data-testid="server-error-banner" role="alert">
          <span className="alert-icon">⚠️</span>
          <span>{serverError}</span>
        </div>
      )}

      <form className="create-complaint-form" onSubmit={handleSubmit} noValidate>
        {/* Step 1: Select Category */}
        <div className="form-section">
          <div className="section-header">
            <div className="section-number">1</div>
            <div>
              <h3 className="section-title">Select Issue Category</h3>
              <p className="section-desc">Choose the category that best matches the civic defect.</p>
            </div>
          </div>

          <div className="category-grid" role="radiogroup" aria-label="Issue Category">
            {CATEGORIES.map((cat) => (
              <button
                type="button"
                key={cat.id}
                className={`category-card ${formData.category === cat.id ? 'active' : ''}`}
                onClick={() => handleCategorySelect(cat.id)}
                data-testid={`category-btn-${cat.id}`}
                aria-pressed={formData.category === cat.id}
              >
                <div className="category-card-icon">{cat.icon}</div>
                <div className="category-card-label">{cat.label}</div>
                <div className="category-card-desc">{cat.description}</div>
                {formData.category === cat.id && <div className="category-active-check">✓</div>}
              </button>
            ))}
          </div>
          {formErrors.category && (
            <div className="field-error" data-testid="category-error">
              {formErrors.category}
            </div>
          )}
        </div>

        {/* Step 2: Issue Description & Title */}
        <div className="form-section">
          <div className="section-header">
            <div className="section-number">2</div>
            <div>
              <h3 className="section-title">Describe the Problem</h3>
              <p className="section-desc">Provide a concise headline and comprehensive details.</p>
            </div>
          </div>

          <div className="form-group">
            <div className="label-row">
              <label htmlFor="complaint-title" className="form-label">
                Complaint Title <span className="required">*</span>
              </label>
              <span className="char-counter">{formData.title.length}/150</span>
            </div>
            <p className="form-helper-text">Short 1-line headline summarizing the civic defect.</p>
            <input
              type="text"
              id="complaint-title"
              name="title"
              className={`form-input ${formErrors.title ? 'input-error' : ''}`}
              placeholder="e.g. Hazardous deep pothole on MG Road near bus stop"
              value={formData.title}
              onChange={handleChange}
              maxLength={150}
              data-testid="complaint-title-input"
            />
            {formErrors.title && (
              <div className="field-error" data-testid="title-error">
                {formErrors.title}
              </div>
            )}
          </div>

          <div className="form-group">
            <div className="label-row">
              <label htmlFor="complaint-description" className="form-label">
                Detailed Description <span className="required">*</span>
              </label>
            </div>
            <p className="form-helper-text">
              Comprehensive paragraph detailing when it started, hazards, obstacles, and landmarks.
            </p>
            <textarea
              id="complaint-description"
              name="description"
              rows={4}
              className={`form-textarea ${formErrors.description ? 'input-error' : ''}`}
              placeholder="Provide exact details: When did this issue start? Is it obstructing traffic or endangering pedestrians? Any specific landmark nearby?"
              value={formData.description}
              onChange={handleChange}
              data-testid="complaint-description-input"
            />
            {formErrors.description && (
              <div className="field-error" data-testid="description-error">
                {formErrors.description}
              </div>
            )}
          </div>

          {/* AI Real-Time Assistant Widget */}
          {(aiLoading || aiSuggestion) && (
            <div className="ai-realtime-assistant" data-testid="ai-realtime-widget">
              <div className="ai-widget-header">
                <div className="ai-widget-title-group">
                  <span className="ai-icon-pulse">✨</span>
                  <h4 className="ai-widget-title">AI Triage Assistant</h4>
                  {aiLoading ? (
                    <span className="ai-status-tag loading">Analyzing text...</span>
                  ) : (
                    <span className="ai-status-tag ready">Suggestions Ready</span>
                  )}
                </div>
                {aiSuggestion && (
                  <div className="confidence-meter-container" data-testid="ai-confidence-meter">
                    <span className="confidence-label">
                      {Math.round((aiSuggestion.confidenceScore || 0.8) * 100)}% Confidence
                    </span>
                    <div className="confidence-meter-track">
                      <div
                        className="confidence-meter-fill"
                        style={{ width: `${Math.round((aiSuggestion.confidenceScore || 0.8) * 100)}%` }}
                      />
                    </div>
                  </div>
                )}
              </div>

              {aiSuggestion && (
                <div className="ai-widget-body">
                  <div className="ai-suggestion-pills">
                    <div className="ai-pill-item">
                      <span className="pill-label">Suggested Category:</span>
                      <span className="pill-value category-val" data-testid="ai-suggested-category">
                        {normalizeCategory(aiSuggestion.suggestedCategory)?.replace(/_/g, ' ')}
                      </span>
                      {formData.category === normalizeCategory(aiSuggestion.suggestedCategory) ? (
                        <span className="badge-applied-check" data-testid="badge-category-applied">✓ Applied</span>
                      ) : (
                        <button
                          type="button"
                          className="btn-apply-suggestion"
                          onClick={() => handleCategorySelect(normalizeCategory(aiSuggestion.suggestedCategory))}
                          data-testid="btn-apply-ai-category"
                        >
                          Apply Category
                        </button>
                      )}
                    </div>

                    <div className="ai-pill-item">
                      <span className="pill-label">Calculated Severity:</span>
                      <span
                        className={`pill-value priority-badge priority-${aiSuggestion.suggestedPriority?.toLowerCase()}`}
                        data-testid="ai-suggested-priority"
                      >
                        {aiSuggestion.suggestedPriority}
                      </span>
                    </div>

                    {aiSuggestion.suggestedDepartmentName && (
                      <div className="ai-pill-item">
                        <span className="pill-label">Auto-Route Dept:</span>
                        <span className="pill-value dept-val" data-testid="ai-suggested-dept">
                          {aiSuggestion.suggestedDepartmentName}
                        </span>
                      </div>
                    )}
                  </div>

                  {aiSuggestion.explanation && (
                    <div className="ai-explanation-box" data-testid="ai-explanation-text">
                      <span className="explanation-icon">💡</span>
                      <span className="explanation-content">{aiSuggestion.explanation}</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Step 3: Location Details */}
        <div className="form-section">
          <div className="section-header">
            <div className="section-number">3</div>
            <div>
              <h3 className="section-title">Location & Landmark</h3>
              <p className="section-desc">Specify address, street, or use your device GPS coordinates.</p>
            </div>
          </div>

          <div className="form-group">
            <div className="label-row">
              <label htmlFor="complaint-location" className="form-label">
                Street Address / Landmark <span className="required">*</span>
              </label>
              <button
                type="button"
                className="btn-detect-gps"
                onClick={handleDetectLocation}
                disabled={geoStatus.loading}
                data-testid="btn-detect-gps"
              >
                {geoStatus.loading ? '📡 Detecting...' : '📍 Use Current Location'}
              </button>
            </div>
            <input
              type="text"
              id="complaint-location"
              name="location"
              className={`form-input ${formErrors.location ? 'input-error' : ''}`}
              placeholder="e.g. Opposite City Hospital Gate 3, Sector 12"
              value={formData.location}
              onChange={handleChange}
              maxLength={255}
              data-testid="complaint-location-input"
            />
            {formErrors.location && (
              <div className="field-error" data-testid="location-error">
                {formErrors.location}
              </div>
            )}

            {geoStatus.success && formData.latitude && (
              <div className="gps-pill" data-testid="gps-coordinates-pill">
                <span>📍 GPS Tagged:</span>
                <code>
                  {formData.latitude}, {formData.longitude}
                </code>
              </div>
            )}
            {geoStatus.error && (
              <div className="gps-warning">
                ⚠️ {geoStatus.error} Please type your address manually above.
              </div>
            )}
          </div>
        </div>

        {/* Step 4: Photograph Evidence Upload */}
        <div className="form-section">
          <div className="section-header">
            <div className="section-number">4</div>
            <div>
              <h3 className="section-title">Photo Evidence (Optional but Recommended)</h3>
              <p className="section-desc">Upload photographic proof to help staff dispatch appropriate equipment.</p>
            </div>
          </div>

          {!imagePreview ? (
            <div
              className={`dropzone-box ${dragActive ? 'drag-over' : ''}`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
              data-testid="photo-dropzone"
            >
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="file-input-hidden"
                onChange={handleFileChange}
                data-testid="photo-file-input"
              />
              <div className="dropzone-icon">📷</div>
              <div className="dropzone-title">Click to upload or drag & drop photo</div>
              <div className="dropzone-hint">Supports JPG, PNG, WEBP • Maximum 5 MB</div>
            </div>
          ) : (
            <div className="image-preview-card" data-testid="photo-preview-card">
              <img src={imagePreview} alt="Evidence Preview" className="preview-thumbnail" />
              <div className="preview-details">
                <div className="preview-name">{imageFile?.name}</div>
                <div className="preview-size">
                  {imageFile ? `${(imageFile.size / 1024).toFixed(1)} KB` : ''}
                </div>
                <button
                  type="button"
                  className="btn-remove-image"
                  onClick={removeImage}
                  data-testid="btn-remove-photo"
                >
                  ✕ Remove Photo
                </button>
              </div>
            </div>
          )}
          {formErrors.image && (
            <div className="field-error" data-testid="image-error">
              {formErrors.image}
            </div>
          )}
        </div>

        {/* Submission Action Bar */}
        <div className="form-actions-bar">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate('/dashboard')}
            disabled={submitting}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary btn-submit"
            disabled={submitting}
            id="btn-submit-complaint"
            data-testid="btn-submit-complaint"
          >
            {submitting ? (
              <>
                <Loader size="sm" />
                <span>Registering Complaint...</span>
              </>
            ) : (
              <span>🚀 Submit Complaint</span>
            )}
          </button>
        </div>
      </form>
    </div>
  )
}

export default CreateComplaint
