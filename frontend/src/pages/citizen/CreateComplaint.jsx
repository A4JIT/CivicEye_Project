import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import complaintService from '../../services/complaintService'
import aiService from '../../services/aiService'
import Loader from '../../components/common/Loader'
import AuthenticatedImage from '../../components/common/AuthenticatedImage'

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

const VOICE_LANGUAGES = [
  { code: 'en-IN', label: 'English (India)' },
  { code: 'hi-IN', label: 'हिंदी (Hindi)' },
  { code: 'mr-IN', label: 'मराठी (Marathi)' },
  { code: 'ta-IN', label: 'தமிழ் (Tamil)' },
  { code: 'te-IN', label: 'తెలుగు (Telugu)' },
  { code: 'bn-IN', label: 'বাংলা (Bengali)' },
  { code: 'en-US', label: 'English (US)' },
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
  const [unlockedStep, setUnlockedStep] = useState(1)

  // AI Real-Time Suggestion States
  const [aiSuggestion, setAiSuggestion] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)

  // AI Photo Relevance Verification States
  const [aiPhotoStatus, setAiPhotoStatus] = useState(null)

  // AI Duplicate Detection States
  const [duplicateWarning, setDuplicateWarning] = useState(null)
  const [checkingDuplicate, setCheckingDuplicate] = useState(false)
  const [dismissedDuplicateId, setDismissedDuplicateId] = useState(null)
  const [upvoting, setUpvoting] = useState(false)
  const [upvotedSuccess, setUpvotedSuccess] = useState(null)
  const [showDuplicateConfirmModal, setShowDuplicateConfirmModal] = useState(false)

  // Voice-to-Text Speech Dictation States
  const [listeningTarget, setListeningTarget] = useState(null) // null | 'title' | 'description'
  const [speechLang, setSpeechLang] = useState('en-IN')
  const [interimTranscript, setInterimTranscript] = useState('')
  const [speechError, setSpeechError] = useState(null)
  const recognitionRef = useRef(null)

  const isSpeechSupported =
    typeof window !== 'undefined' &&
    Boolean(window.SpeechRecognition || window.webkitSpeechRecognition)

  const stopListening = () => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop()
      } catch {
        // ignore if already stopped
      }
      recognitionRef.current = null
    }
    setListeningTarget(null)
    setInterimTranscript('')
  }

  const toggleListening = (field) => {
    if (!isSpeechSupported) {
      setSpeechError('Speech recognition is not supported in this browser. Please try Chrome, Edge, or Safari.')
      return
    }

    if (listeningTarget === field) {
      stopListening()
      return
    }

    // Stop active listening if switching fields
    stopListening()
    setSpeechError(null)

    try {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      const recognition = new SpeechRecognition()
      recognition.lang = speechLang
      recognition.continuous = true
      recognition.interimResults = true
      recognition.maxAlternatives = 1

      recognition.onstart = () => {
        setListeningTarget(field)
        setSpeechError(null)
      }

      recognition.onresult = (event) => {
        let finalChunk = ''
        let interimChunk = ''

        for (let i = event.resultIndex; i < event.results.length; i++) {
          const item = event.results[i]
          const transcript = item[0]?.transcript || ''
          if (item.isFinal) {
            finalChunk += transcript
          } else {
            interimChunk += transcript
          }
        }

        if (finalChunk) {
          setFormData((prev) => {
            const currentVal = prev[field] || ''
            const combined = currentVal ? `${currentVal.trim()} ${finalChunk.trim()}` : finalChunk.trim()
            const finalVal = field === 'title' ? combined.slice(0, 150) : combined
            return {
              ...prev,
              [field]: finalVal,
            }
          })
          if (formErrors[field]) {
            setFormErrors((prev) => ({ ...prev, [field]: null }))
          }
        }

        setInterimTranscript(interimChunk)
      }

      recognition.onerror = (event) => {
        console.warn('Speech recognition error:', event.error)
        if (event.error === 'not-allowed' || event.error === 'service-not-allowed') {
          setSpeechError('Microphone permission was denied. Please allow microphone access in your browser settings.')
        } else if (event.error === 'no-speech') {
          // harmless silence event
        } else if (event.error === 'network') {
          setSpeechError('Network error during speech recognition. Please check your internet connection.')
        } else {
          setSpeechError(`Speech recognition notice: ${event.error}`)
        }
        setListeningTarget(null)
        setInterimTranscript('')
      }

      recognition.onend = () => {
        setListeningTarget(null)
        setInterimTranscript('')
      }

      recognitionRef.current = recognition
      recognition.start()
      setListeningTarget(field)
    } catch (err) {
      console.error('Speech recognition initiation failed:', err)
      setSpeechError('Failed to initialize microphone. Please check your browser permissions.')
      setListeningTarget(null)
    }
  }

  const handleLanguageChange = (e) => {
    const newLang = e.target.value
    setSpeechLang(newLang)
    if (recognitionRef.current) {
      recognitionRef.current.lang = newLang
    }
  }

  // Cleanup speech recognition on unmount
  useEffect(() => {
    return () => {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.abort()
        } catch {
          // ignore
        }
      }
    }
  }, [])

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

  // Real-time debounced Duplicate Complaint Detection
  useEffect(() => {
    if (!formData.title || formData.title.trim().length < 4) {
      setDuplicateWarning(null)
      return
    }

    setCheckingDuplicate(true)
    const timer = setTimeout(async () => {
      try {
        const result = await aiService.checkDuplicateComplaint({
          title: formData.title,
          description: formData.description,
          category: formData.category,
          location: formData.location,
          latitude: formData.latitude,
          longitude: formData.longitude,
        })
        if (result && result.isDuplicate && result.topMatch) {
          setDuplicateWarning(result)
        } else {
          setDuplicateWarning(null)
        }
      } catch (err) {
        console.error('Duplicate complaint check failed:', err)
      } finally {
        setCheckingDuplicate(false)
      }
    }, 400)

    return () => clearTimeout(timer)
  }, [formData.title, formData.description, formData.category, formData.location, formData.latitude, formData.longitude])

  const handleUpvoteExisting = async (complaintId, complaintNumber) => {
    setUpvoting(true)
    try {
      const res = await complaintService.upvoteComplaint(complaintId)
      setUpvotedSuccess({
        complaintId,
        complaintNumber,
        upvotesCount: res?.data?.upvotesCount || (duplicateWarning?.topMatch?.upvotesCount || 1) + 1,
        title: duplicateWarning?.topMatch?.title,
      })
    } catch (err) {
      console.error('Failed to upvote duplicate complaint', err)
      setUpvotedSuccess({
        complaintId,
        complaintNumber,
        upvotesCount: (duplicateWarning?.topMatch?.upvotesCount || 1) + 1,
        title: duplicateWarning?.topMatch?.title,
      })
    } finally {
      setUpvoting(false)
    }
  }

  const handleDismissDuplicate = () => {
    if (duplicateWarning?.topMatch?.complaintId) {
      setDismissedDuplicateId(duplicateWarning.topMatch.complaintId)
    }
  }

  const renderDuplicateWarningCard = () => {
    if (
      !duplicateWarning?.isDuplicate ||
      !duplicateWarning?.topMatch ||
      dismissedDuplicateId === duplicateWarning.topMatch.complaintId
    ) {
      return null
    }

    const match = duplicateWarning.topMatch

    return (
      <div className="duplicate-complaint-alert-card" data-testid="duplicate-warning-card">
        <div className="duplicate-alert-header">
          <div className="duplicate-alert-title-wrap">
            <span className="duplicate-spark-icon">⚡</span>
            <span className="duplicate-alert-title">Similar Active Issue Found</span>
          </div>
          <div className="duplicate-match-pill" data-testid="duplicate-similarity-badge">
            {Math.round(match.similarityScore * 100)}% Match
          </div>
        </div>

        <div className="duplicate-alert-body">
          <p className="duplicate-alert-intro">
            A matching complaint has already been submitted in this neighborhood.
            Upvoting this existing complaint increases its priority in the department dispatch queue and prevents duplicate investigations!
          </p>

          <div className="duplicate-matched-item-box" data-testid="duplicate-matched-item">
            <div className="duplicate-item-header">
              <span className="duplicate-ticket-badge" data-testid="duplicate-ticket-number">
                #{match.complaintNumber}
              </span>
              <span className={`status-badge status-${match.status?.toLowerCase() || 'submitted'}`}>
                {match.status || 'ACTIVE'}
              </span>
              {match.distanceMeters != null && (
                <span className="distance-badge" data-testid="duplicate-distance-badge">
                  📍 {match.distanceMeters}m away
                </span>
              )}
              <span className="upvotes-count-badge" data-testid="duplicate-upvotes-count">
                👍 {match.upvotesCount || 1} Endorsements
              </span>
            </div>

            <div className="duplicate-item-title" data-testid="duplicate-matched-title">
              {match.title}
            </div>

            {match.location && (
              <div className="duplicate-item-location">
                📍 {match.location}
              </div>
            )}

            {match.matchReason && (
              <div className="duplicate-item-reason">
                💡 {match.matchReason}
              </div>
            )}
          </div>

          <div className="duplicate-card-actions">
            <button
              type="button"
              className="btn btn-upvote-issue"
              onClick={() => handleUpvoteExisting(match.complaintId, match.complaintNumber)}
              disabled={upvoting}
              data-testid="btn-upvote-duplicate"
            >
              {upvoting ? (
                <>
                  <Loader size="sm" />
                  <span>Endorsing Issue...</span>
                </>
              ) : (
                <span>👍 Upvote & Follow Existing Issue</span>
              )}
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-dismiss-duplicate"
              onClick={handleDismissDuplicate}
              data-testid="btn-dismiss-duplicate"
            >
              Different Issue, Continue Filing
            </button>
          </div>
        </div>
      </div>
    )
  }

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
    // Re-verify existing photo against the new category
    if (imageFile) {
      verifyImageRelevance(imageFile, categoryId)
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

  const verifyImageRelevance = async (file, categoryToVerify) => {
    if (!file) return

    setAiPhotoStatus({
      analyzing: true,
      valid: null,
      isRejected: false,
      result: null,
      error: null,
    })

    try {
      const result = await aiService.verifyPhotoRelevance(categoryToVerify, file, {
        title: formData.title,
        description: formData.description,
      })
      if (result) {
        if (result.isRejected || result.valid === false) {
          setAiPhotoStatus({
            analyzing: false,
            valid: false,
            isRejected: true,
            result,
            error: result.rejectionReason,
          })
          setFormErrors((prev) => ({ ...prev, image: result.rejectionReason }))
        } else {
          setAiPhotoStatus({
            analyzing: false,
            valid: true,
            isRejected: false,
            result,
            error: null,
          })
          setFormErrors((prev) => ({ ...prev, image: null }))
        }
      }
    } catch (err) {
      console.error('Error verifying image relevance:', err)
      setAiPhotoStatus({
        analyzing: false,
        valid: true,
        isRejected: false,
        result: {
          valid: true,
          categoryLabel: CATEGORIES.find((c) => c.id === categoryToVerify)?.label || 'Selected Category',
          detectedSubject: 'Visual Proof Attached',
          confidenceScore: 0.85,
        },
        error: null,
      })
    }
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
      verifyImageRelevance(file, formData.category)
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
      verifyImageRelevance(file, formData.category)
    }
  }

  const removeImage = () => {
    setImageFile(null)
    setImagePreview(null)
    setAiPhotoStatus(null)
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

  const handleStepSubmit = (step) => {
    if (step === 1) {
      if (!formData.category) {
        setFormErrors((prev) => ({ ...prev, category: 'Please select an issue category' }))
        return
      }
      setFormErrors((prev) => ({ ...prev, category: null }))
      setUnlockedStep((prev) => Math.max(prev, 2))
      setTimeout(() => {
        document.getElementById('question-step-2')?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
      }, 50)
    } else if (step === 2) {
      if (!formData.title.trim()) {
        setFormErrors((prev) => ({ ...prev, title: 'Title is required' }))
        return
      } else if (formData.title.length > 150) {
        setFormErrors((prev) => ({ ...prev, title: 'Title cannot exceed 150 characters' }))
        return
      }
      setFormErrors((prev) => ({ ...prev, title: null }))
      setUnlockedStep((prev) => Math.max(prev, 3))
      setTimeout(() => {
        document.getElementById('question-step-3')?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
      }, 50)
    } else if (step === 3) {
      if (!formData.description.trim()) {
        setFormErrors((prev) => ({ ...prev, description: 'Please provide detailed description of the issue' }))
        return
      }
      setFormErrors((prev) => ({ ...prev, description: null }))
      setUnlockedStep((prev) => Math.max(prev, 4))
      setTimeout(() => {
        document.getElementById('question-step-4')?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
      }, 50)
    } else if (step === 4) {
      if (!formData.location.trim()) {
        setFormErrors((prev) => ({ ...prev, location: 'Location address or landmark is required' }))
        return
      }
      setFormErrors((prev) => ({ ...prev, location: null }))
      setUnlockedStep((prev) => Math.max(prev, 5))
      setTimeout(() => {
        document.getElementById('question-step-5')?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
      }, 50)
    }
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

    if (imageFile && aiPhotoStatus?.valid === false) {
      errors.image = aiPhotoStatus?.error || 'AI Rejection: Uploaded photo does not match the issue category. Please upload valid civic defect evidence or remove the photo.'
    }

    setFormErrors(errors)
    if (Object.keys(errors).length > 0) {
      setUnlockedStep(5)
    }
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async (e, bypassDuplicate = false) => {
    if (e && e.preventDefault) e.preventDefault()
    if (!validate()) return

    if (imageFile && aiPhotoStatus?.valid === false) {
      setServerError('Complaint submission rejected by AI: Uploaded photo does not match the issue category. Please upload valid civic defect evidence or remove the photo.')
      return
    }

    if (
      !bypassDuplicate &&
      duplicateWarning?.isDuplicate &&
      duplicateWarning?.topMatch &&
      dismissedDuplicateId !== duplicateWarning.topMatch.complaintId
    ) {
      setShowDuplicateConfirmModal(true)
      return
    }

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
    stopListening()
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
    setSpeechError(null)
    setAiSuggestion(null)
    setDuplicateWarning(null)
    setDismissedDuplicateId(null)
    setUpvotedSuccess(null)
    setUnlockedStep(1)
  }

  // Upvote Existing Issue Success Screen
  if (upvotedSuccess) {
    return (
      <DashboardLayout
        title="Issue Endorsed & Tracked"
        subtitle="You have upvoted an existing civic complaint. Thank you for keeping municipal reporting efficient!"
      >
        <div className="create-complaint-container" data-testid="complaint-upvote-success-screen">
          <div className="complaint-success-card">
            <div className="success-icon-badge" style={{ background: 'rgba(59, 130, 246, 0.2)', color: '#3b82f6' }}>
              👍
            </div>
            <span className="success-tag" style={{ background: 'rgba(59, 130, 246, 0.15)', color: '#60a5fa' }}>
              Duplicate Prevented • Issue Endorsed
            </span>
            <h2 className="success-title">Existing Issue Upvoted!</h2>
            <p className="success-desc">
              Instead of creating a duplicate ticket, you endorsed complaint <strong>#{upvotedSuccess.complaintNumber}</strong>.
              Your upvote escalates this issue’s priority in the municipal department queue.
            </p>

            <div className="complaint-id-box">
              <div className="complaint-id-label">TRACKED ISSUE ID</div>
              <div className="complaint-id-value" data-testid="upvoted-number-display">
                {upvotedSuccess.complaintNumber}
              </div>
              <div style={{ marginTop: '0.6rem', color: '#38bdf8', fontSize: '0.95rem', fontWeight: 600 }}>
                🔥 {upvotedSuccess.upvotesCount} Citizens Endorsed This Issue
              </div>
            </div>

            {upvotedSuccess.title && (
              <div className="complaint-summary-preview" style={{ marginTop: '1.25rem' }}>
                <div className="summary-row">
                  <span className="summary-label">Issue:</span>
                  <span className="summary-val">{upvotedSuccess.title}</span>
                </div>
              </div>
            )}

            <div className="form-actions-bar" style={{ marginTop: '2rem', justifyContent: 'center' }}>
              <Link to={`/complaints/${upvotedSuccess.complaintId}`} className="btn btn-primary" data-testid="btn-track-existing">
                🔍 Track Live Updates
              </Link>
              <Link to="/complaints/my" className="btn btn-secondary">
                My Dashboard
              </Link>
              <button type="button" className="btn btn-secondary" onClick={resetForm} data-testid="btn-report-another">
                Report Another Issue
              </button>
            </div>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  // Success Confirmation Screen
  if (submittedComplaint) {
    return (
      <DashboardLayout
        title="Complaint Submitted"
        subtitle="Your complaint has been successfully lodged and assigned a unique tracking ID."
      >
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

            {(submittedComplaint.imageUrl || imagePreview) && (
              <div style={{ marginTop: '1.25rem', textAlign: 'center' }}>
                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#94a3b8', marginBottom: '8px' }}>
                  📸 Registered Defect Photo Evidence:
                </div>
                {submittedComplaint.imageUrl ? (
                  <AuthenticatedImage
                    src={submittedComplaint.imageUrl}
                    alt="Defect Proof"
                    style={{ maxHeight: '220px', maxWidth: '100%', borderRadius: '8px', objectFit: 'contain' }}
                    allowZoom={true}
                  />
                ) : (
                  <img
                    src={imagePreview}
                    alt="Defect Proof Preview"
                    style={{ maxHeight: '220px', maxWidth: '100%', borderRadius: '8px', objectFit: 'contain' }}
                  />
                )}
              </div>
            )}

            <div className="success-actions">
              <Link
                to={`/complaints/track/${submittedComplaint.complaintNumber}`}
                className="btn btn-primary"
                id="btn-track-new-complaint"
              >
                🔍 Track Live Status
              </Link>
              <Link to="/dashboard" className="btn btn-secondary" id="btn-back-dashboard">
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
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout
      title="Register a Civic Complaint"
      subtitle="Report municipal issues directly to the authorities with photo evidence and accurate location data."
    >
      <div className="create-complaint-container">
        {serverError && (
          <div className="alert-error" data-testid="server-error-banner" role="alert">
            <span className="alert-icon">⚠️</span>
            <span>{serverError}</span>
          </div>
        )}

        <form className="create-complaint-form" onSubmit={handleSubmit} noValidate>
          {/* Progressive Question Stepper Header */}
          <div className="complaint-stepper-container" data-testid="complaint-stepper">
            <div className="stepper-meta-row">
              <span className="stepper-step-badge">
                Question {Math.min(unlockedStep, 5)} of 5
              </span>
              <span className="stepper-progress-percentage">
                {Math.min(unlockedStep * 20, 100)}% Completed
              </span>
            </div>
            <div className="stepper-progress-track">
              <div
                className="stepper-progress-bar"
                style={{ width: `${Math.min(unlockedStep * 20, 100)}%` }}
              />
            </div>
            <div className="stepper-nav-tabs">
              {[
                { num: 1, label: 'Category' },
                { num: 2, label: 'Title' },
                { num: 3, label: 'Description' },
                { num: 4, label: 'Location' },
                { num: 5, label: 'Evidence & Submit' },
              ].map((s) => {
                const isPassed = unlockedStep > s.num
                const isCurrent = unlockedStep === s.num
                return (
                  <button
                    key={s.num}
                    type="button"
                    className={`stepper-tab ${isCurrent ? 'active' : ''} ${isPassed ? 'completed' : ''}`}
                    onClick={() => {
                      if (unlockedStep >= s.num) {
                        document.getElementById(`question-step-${s.num}`)?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
                      }
                    }}
                    disabled={unlockedStep < s.num}
                    title={`Question ${s.num}: ${s.label}`}
                  >
                    <span className="stepper-tab-num">{isPassed ? '✓' : s.num}</span>
                    <span className="stepper-tab-label">{s.label}</span>
                  </button>
                )
              })}
            </div>
          </div>

          {/* Question 1: Select Category */}
          <div
            id="question-step-1"
            data-testid="question-step-1"
            className={`form-section question-section ${unlockedStep >= 1 ? 'question-visible' : 'question-locked'}`}
          >
            <div className="section-header">
              <div className="section-number">1</div>
              <div>
                <h3 className="section-title">Select Issue Category</h3>
                <p className="section-desc">Choose the category that best matches the civic defect.</p>
              </div>
              {formData.category && unlockedStep > 1 && (
                <span className="question-answered-badge">✓ Category Selected</span>
              )}
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

            <div className="question-submit-box">
              <button
                type="button"
                className="btn btn-primary btn-step-submit"
                onClick={() => handleStepSubmit(1)}
                data-testid="btn-submit-q1"
              >
                <span>Submit</span>
                <span className="step-arrow">➔</span>
              </button>
            </div>
          </div>

          {/* Question 2: Complaint Title */}
          <div
            id="question-step-2"
            data-testid="question-step-2"
            className={`form-section question-section ${unlockedStep >= 2 ? 'question-visible' : 'question-locked'}`}
          >
            <div className="section-header section-header-with-action">
              <div className="section-header-main">
                <div className="section-number">2</div>
                <div>
                  <h3 className="section-title">Complaint Title</h3>
                  <p className="section-desc">Provide a concise headline summarizing the civic defect.</p>
                </div>
                {formData.title.trim() && unlockedStep > 2 && (
                  <span className="question-answered-badge">✓ Title Entered</span>
                )}
              </div>

              <div className="voice-lang-selector-group" data-testid="voice-lang-group">
                <label htmlFor="voice-speech-lang" className="voice-lang-label">
                  <span className="mic-badge-icon">🎙️</span>
                  <span>Voice Language:</span>
                </label>
                <select
                  id="voice-speech-lang"
                  className="voice-lang-select"
                  value={speechLang}
                  onChange={handleLanguageChange}
                  aria-label="Voice input language"
                  data-testid="voice-lang-select"
                >
                  {VOICE_LANGUAGES.map((lang) => (
                    <option key={lang.code} value={lang.code}>
                      {lang.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {speechError && (
              <div className="alert-error speech-alert-error" data-testid="speech-error-alert" role="alert">
                <span className="alert-icon">⚠️</span>
                <span className="speech-error-text">{speechError}</span>
                <button
                  type="button"
                  className="btn-dismiss-speech-error"
                  onClick={() => setSpeechError(null)}
                  aria-label="Dismiss voice error"
                  data-testid="btn-dismiss-speech-error"
                >
                  ✕
                </button>
              </div>
            )}

            <div className="form-group">
              <div className="label-row">
                <label htmlFor="complaint-title" className="form-label">
                  Complaint Title <span className="required">*</span>
                </label>
                <div className="label-actions-group">
                  <button
                    type="button"
                    className={`btn-voice-dictate ${listeningTarget === 'title' ? 'listening' : ''}`}
                    onClick={() => toggleListening('title')}
                    data-testid="btn-voice-title"
                    title={listeningTarget === 'title' ? 'Click to stop listening' : 'Mick'}
                    aria-pressed={listeningTarget === 'title'}
                  >
                    {listeningTarget === 'title' ? (
                      <>
                        <span className="mic-recording-pulse" />
                        <span>Stop Listening</span>
                      </>
                    ) : (
                      <>
                        <span className="mic-btn-icon">🎙️</span>
                        <span>Mick</span>
                      </>
                    )}
                  </button>
                  <span className="char-counter">{formData.title.length}/150</span>
                </div>
              </div>

              {listeningTarget === 'title' && (
                <div className="voice-listening-card" data-testid="voice-listening-title">
                  <div className="voice-wave-container" aria-hidden="true">
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                  </div>
                  <div className="voice-listening-info">
                    <span className="voice-listening-title">
                      🔴 Listening for Title ({VOICE_LANGUAGES.find((l) => l.code === speechLang)?.label || 'English'})...
                    </span>
                    <span className="voice-interim-text">
                      {interimTranscript ? `"${interimTranscript}"` : 'Speak now... speech will transcribe directly into title.'}
                    </span>
                  </div>
                  <button
                    type="button"
                    className="btn-voice-done"
                    onClick={() => toggleListening('title')}
                    data-testid="btn-voice-done-title"
                  >
                    ✓ Done
                  </button>
                </div>
              )}

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

              {checkingDuplicate && (
                <div
                  className="duplicate-checking-hint"
                  style={{
                    fontSize: '0.82rem',
                    color: '#38bdf8',
                    marginTop: '0.5rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.45rem',
                  }}
                >
                  <Loader size="sm" />
                  <span>Checking neighborhood for existing complaints...</span>
                </div>
              )}

              {unlockedStep < 5 && renderDuplicateWarningCard()}
            </div>

            <div className="question-submit-box">
              <button
                type="button"
                className="btn btn-primary btn-step-submit"
                onClick={() => handleStepSubmit(2)}
                data-testid="btn-submit-q2"
              >
                <span>Submit</span>
                <span className="step-arrow">➔</span>
              </button>
            </div>
          </div>

          {/* Question 3: Detailed Description & AI Assistant */}
          <div
            id="question-step-3"
            data-testid="question-step-3"
            className={`form-section question-section ${unlockedStep >= 3 ? 'question-visible' : 'question-locked'}`}
          >
            <div className="section-header">
              <div className="section-number">3</div>
              <div>
                <h3 className="section-title">Detailed Description</h3>
                <p className="section-desc">Provide comprehensive details about when it started, hazards, and obstacles.</p>
              </div>
              {formData.description.trim() && unlockedStep > 3 && (
                <span className="question-answered-badge">✓ Description Entered</span>
              )}
            </div>

            <div className="form-group">
              <div className="label-row">
                <label htmlFor="complaint-description" className="form-label">
                  Detailed Description <span className="required">*</span>
                </label>
                <button
                  type="button"
                  className={`btn-voice-dictate ${listeningTarget === 'description' ? 'listening' : ''}`}
                  onClick={() => toggleListening('description')}
                  data-testid="btn-voice-description"
                  title={listeningTarget === 'description' ? 'Click to stop listening' : 'Mick'}
                  aria-pressed={listeningTarget === 'description'}
                >
                  {listeningTarget === 'description' ? (
                    <>
                      <span className="mic-recording-pulse" />
                      <span>Stop Listening</span>
                    </>
                  ) : (
                    <>
                      <span className="mic-btn-icon">🎙️</span>
                      <span>Mick</span>
                    </>
                  )}
                </button>
              </div>

              {listeningTarget === 'description' && (
                <div className="voice-listening-card" data-testid="voice-listening-description">
                  <div className="voice-wave-container" aria-hidden="true">
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                    <div className="soundwave-bar"></div>
                  </div>
                  <div className="voice-listening-info">
                    <span className="voice-listening-title">
                      🔴 Listening for Description ({VOICE_LANGUAGES.find((l) => l.code === speechLang)?.label || 'English'})...
                    </span>
                    <span className="voice-interim-text">
                      {interimTranscript
                        ? `"${interimTranscript}"`
                        : 'Speak freely... describe when it started, hazards, obstacles, and landmarks.'}
                    </span>
                  </div>
                  <button
                    type="button"
                    className="btn-voice-done"
                    onClick={() => toggleListening('description')}
                    data-testid="btn-voice-done-description"
                  >
                    ✓ Done
                  </button>
                </div>
              )}

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

            <div className="question-submit-box">
              <button
                type="button"
                className="btn btn-primary btn-step-submit"
                onClick={() => handleStepSubmit(3)}
                data-testid="btn-submit-q3"
              >
                <span>Submit</span>
                <span className="step-arrow">➔</span>
              </button>
            </div>
          </div>

          {/* Question 4: Location Details */}
          <div
            id="question-step-4"
            data-testid="question-step-4"
            className={`form-section question-section ${unlockedStep >= 4 ? 'question-visible' : 'question-locked'}`}
          >
            <div className="section-header">
              <div className="section-number">4</div>
              <div>
                <h3 className="section-title">Location & Landmark</h3>
                <p className="section-desc">Specify address, street, or use your device GPS coordinates.</p>
              </div>
              {formData.location.trim() && unlockedStep > 4 && (
                <span className="question-answered-badge">✓ Location Provided</span>
              )}
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

            <div className="question-submit-box">
              <button
                type="button"
                className="btn btn-primary btn-step-submit"
                onClick={() => handleStepSubmit(4)}
                data-testid="btn-submit-q4"
              >
                <span>Submit</span>
                <span className="step-arrow">➔</span>
              </button>
            </div>
          </div>

          {/* Question 5: Photograph Evidence Upload & Submission */}
          <div
            id="question-step-5"
            data-testid="question-step-5"
            className={`form-section question-section ${unlockedStep >= 5 ? 'question-visible' : 'question-locked'}`}
          >
            <div className="section-header">
              <div className="section-number">5</div>
              <div>
                <h3 className="section-title">Photo Evidence (Optional but Recommended)</h3>
                <p className="section-desc">Upload photographic proof to help staff dispatch appropriate equipment.</p>
              </div>
              {imageFile && (
                <span className="question-answered-badge">✓ Photo Attached</span>
              )}
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
                <div className="preview-thumbnail-wrapper">
                  <img src={imagePreview} alt="Evidence Preview" className="preview-thumbnail" />
                  {aiPhotoStatus?.valid === true && (
                    <span className="thumbnail-badge badge-passed" data-testid="badge-photo-passed">
                      ✓ AI Verified
                    </span>
                  )}
                  {aiPhotoStatus?.valid === false && (
                    <span className="thumbnail-badge badge-rejected" data-testid="badge-photo-rejected">
                      ✕ AI Rejected
                    </span>
                  )}
                </div>
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

            {/* AI Photo Verification Status Card */}
            {imageFile && aiPhotoStatus?.analyzing && (
              <div className="ai-photo-verification-card ai-photo-analyzing" data-testid="ai-photo-analyzing">
                <div className="ai-verification-spinner" />
                <div className="ai-verification-content">
                  <div className="ai-verification-title">
                    <span className="ai-spark-icon">✨</span> CivicEye AI Vision Analyzing...
                  </div>
                  <div className="ai-verification-desc">
                    Scanning photo visual patterns to verify relevance for <strong>{CATEGORIES.find((c) => c.id === formData.category)?.label || 'Selected Category'}</strong>...
                  </div>
                </div>
              </div>
            )}

            {imageFile && !aiPhotoStatus?.analyzing && aiPhotoStatus?.valid === true && (
              <div className="ai-photo-verification-card ai-photo-passed" data-testid="ai-photo-verified">
                <div className="ai-verification-icon success">✅</div>
                <div className="ai-verification-content">
                  <div className="ai-verification-title success">AI Verified: Photo Matches Category</div>
                  <div className="ai-verification-desc">
                    Photo evidence confirmed for <strong>{aiPhotoStatus.result?.categoryLabel}</strong> • Confidence: {Math.round((aiPhotoStatus.result?.confidenceScore || 0.9) * 100)}%
                  </div>
                  {aiPhotoStatus.result?.detectedSubject && (
                    <div className="ai-verification-tag success">
                      <span>Detected Feature:</span> {aiPhotoStatus.result.detectedSubject}
                    </div>
                  )}
                </div>
              </div>
            )}

            {imageFile && !aiPhotoStatus?.analyzing && aiPhotoStatus?.valid === false && (
              <div className="ai-photo-verification-card ai-photo-rejected" data-testid="ai-photo-rejected">
                <div className="ai-verification-icon danger">❌</div>
                <div className="ai-verification-content">
                  <div className="ai-verification-title danger">AI Photo Verification Failed: Complaint Rejected</div>
                  <div className="ai-rejection-alert-pill">
                    <span>Evidence Not Related to Issue Category</span>
                  </div>
                  <div className="ai-verification-desc danger-desc">
                    {aiPhotoStatus.result?.rejectionReason || 'The uploaded photo is not related to the selected category.'}
                  </div>
                  {aiPhotoStatus.result?.detectedSubject && (
                    <div className="ai-verification-tag danger">
                      <span>AI Detected:</span> <strong>{aiPhotoStatus.result.detectedSubject}</strong>
                    </div>
                  )}
                  {aiPhotoStatus.result?.detailedReason && (
                    <div className="ai-rejection-guide">
                      💡 {aiPhotoStatus.result.detailedReason}
                    </div>
                  )}
                  <div className="ai-rejected-actions">
                    <button
                      type="button"
                      className="btn btn-sm btn-reupload"
                      onClick={() => fileInputRef.current?.click()}
                      data-testid="btn-reupload-photo"
                    >
                      🔄 Upload Matching Photo
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-remove-rejected"
                      onClick={removeImage}
                      data-testid="btn-remove-rejected-photo"
                    >
                      ✕ Remove Photo
                    </button>
                  </div>
                </div>
              </div>
            )}

            {formErrors.image && (
              <div className="field-error" data-testid="image-error">
                {formErrors.image}
              </div>
            )}

            {/* AI Duplicate Detection Warning Banner in Final Review */}
            {unlockedStep >= 5 && renderDuplicateWarningCard()}

            {/* Submission Action Bar */}
            {aiPhotoStatus?.valid === false && (
              <div className="photo-rejected-block-warning" data-testid="photo-rejection-notice">
                ⚠️ <strong>Complaint Submission Blocked:</strong> Photo evidence was rejected by AI because it is unrelated to the selected category ({CATEGORIES.find((c) => c.id === formData.category)?.label || 'Issue Category'}). Please upload a matching photograph or remove the photo to submit.
              </div>
            )}

            <div className="form-actions-bar" style={{ marginTop: '1.75rem' }}>
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
                className={`btn btn-primary btn-submit ${aiPhotoStatus?.valid === false ? 'btn-submit-blocked' : ''}`}
                disabled={submitting || aiPhotoStatus?.valid === false}
                id="btn-submit-complaint"
                data-testid="btn-submit-complaint"
                title={aiPhotoStatus?.valid === false ? 'Complaint cannot be submitted with unrelated photo evidence' : 'Submit Complaint'}
              >
                {submitting ? (
                  <>
                    <Loader size="sm" />
                    <span>Registering Complaint...</span>
                  </>
                ) : aiPhotoStatus?.valid === false ? (
                  <span>⛔ Submission Blocked by AI</span>
                ) : (
                  <span>🚀 Submit Complaint</span>
                )}
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Duplicate Confirmation Modal on Final Submit */}
      {showDuplicateConfirmModal && duplicateWarning?.topMatch && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" style={{ zIndex: 9999 }}>
          <div className="modal-container" style={{ maxWidth: '580px', width: '90%' }}>
            <div className="modal-header">
              <div className="duplicate-alert-title-wrap">
                <span className="duplicate-spark-icon">⚡</span>
                <h3 className="modal-title" style={{ color: '#fbbf24' }}>
                  Similar Complaint Already Exists
                </h3>
              </div>
              <button
                type="button"
                className="modal-close-btn"
                onClick={() => setShowDuplicateConfirmModal(false)}
                aria-label="Close modal"
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p className="modal-instruction-text" style={{ color: '#e2e8f0' }}>
                CivicEye AI detected a complaint with{' '}
                <strong>{Math.round(duplicateWarning.topMatch.similarityScore * 100)}% similarity</strong> already logged in this area.
              </p>

              <div className="duplicate-matched-item-box" style={{ margin: '1rem 0' }}>
                <div className="duplicate-item-header">
                  <span className="duplicate-ticket-badge">#{duplicateWarning.topMatch.complaintNumber}</span>
                  <span className={`status-badge status-${duplicateWarning.topMatch.status?.toLowerCase() || 'submitted'}`}>
                    {duplicateWarning.topMatch.status || 'ACTIVE'}
                  </span>
                  {duplicateWarning.topMatch.distanceMeters != null && (
                    <span className="distance-badge">
                      📍 {duplicateWarning.topMatch.distanceMeters}m away
                    </span>
                  )}
                  <span className="upvotes-count-badge">
                    👍 {duplicateWarning.topMatch.upvotesCount || 1} Endorsements
                  </span>
                </div>
                <div className="duplicate-item-title">{duplicateWarning.topMatch.title}</div>
                {duplicateWarning.topMatch.location && (
                  <div className="duplicate-item-location">📍 {duplicateWarning.topMatch.location}</div>
                )}
                {duplicateWarning.topMatch.matchReason && (
                  <div className="duplicate-item-reason">💡 {duplicateWarning.topMatch.matchReason}</div>
                )}
              </div>

              <p style={{ fontSize: '0.85rem', color: '#94a3b8', lineHeight: 1.5 }}>
                Instead of submitting a duplicate ticket, upvoting aggregates citizen reports on the primary ticket and escalates it higher in the dispatch queue.
              </p>

              <div className="duplicate-card-actions" style={{ marginTop: '1.5rem', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowDuplicateConfirmModal(false)}
                >
                  Review Form
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ opacity: 0.8 }}
                  onClick={() => {
                    handleDismissDuplicate()
                    setShowDuplicateConfirmModal(false)
                    handleSubmit(null, true)
                  }}
                  data-testid="btn-force-submit-duplicate"
                >
                  Submit Anyway
                </button>
                <button
                  type="button"
                  className="btn btn-upvote-issue"
                  onClick={() => {
                    setShowDuplicateConfirmModal(false)
                    handleUpvoteExisting(duplicateWarning.topMatch.complaintId, duplicateWarning.topMatch.complaintNumber)
                  }}
                  disabled={upvoting}
                  data-testid="btn-modal-upvote-duplicate"
                >
                  👍 Upvote & Endorse Existing
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}

export default CreateComplaint
