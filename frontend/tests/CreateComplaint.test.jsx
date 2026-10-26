import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext'
import CreateComplaint from '../src/pages/citizen/CreateComplaint'
import complaintService from '../src/services/complaintService'
import aiService, { analyzePixelData, computeLocalPhotoVerification } from '../src/services/aiService'

// Helper to render with routing and auth
const renderWithProviders = (ui) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  )
}

describe('CreateComplaint Component Suite', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.clearAllMocks()
    global.URL.createObjectURL = vi.fn(() => 'blob:http://localhost/test-preview')
  })

  it('renders all registration sections and inputs', () => {
    renderWithProviders(<CreateComplaint />)

    expect(screen.getByRole('heading', { name: /register a civic complaint/i })).toBeDefined()
    expect(screen.getByText(/select issue category/i)).toBeDefined()
    expect(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS')).toBeDefined()
    expect(screen.getByTestId('category-btn-GARBAGE_AND_CLEANLINESS')).toBeDefined()
    expect(screen.getByTestId('complaint-title-input')).toBeDefined()
    expect(screen.getByTestId('complaint-description-input')).toBeDefined()
    expect(screen.getByTestId('complaint-location-input')).toBeDefined()
    expect(screen.getByTestId('btn-detect-gps')).toBeDefined()
    expect(screen.getByTestId('photo-dropzone')).toBeDefined()
    expect(screen.getByTestId('btn-submit-complaint')).toBeDefined()
  })

  it('validates mandatory fields on empty submission', async () => {
    renderWithProviders(<CreateComplaint />)

    const submitBtn = screen.getByTestId('btn-submit-complaint')
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(screen.getByTestId('title-error')).toBeDefined()
      expect(screen.getByTestId('category-error')).toBeDefined()
      expect(screen.getByTestId('description-error')).toBeDefined()
      expect(screen.getByTestId('location-error')).toBeDefined()
    })
  })

  it('allows selecting an issue category and highlights it', async () => {
    renderWithProviders(<CreateComplaint />)

    const potholeBtn = screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS')
    fireEvent.click(potholeBtn)

    expect(potholeBtn.className).toContain('active')
    expect(potholeBtn.getAttribute('aria-pressed')).toBe('true')
  })

  it('handles GPS geolocation detection', async () => {
    const mockGeolocation = {
      getCurrentPosition: vi.fn((success) => {
        success({
          coords: {
            latitude: 18.52043,
            longitude: 73.85674,
          },
        })
      }),
    }
    vi.stubGlobal('navigator', {
      ...global.navigator,
      geolocation: mockGeolocation,
    })

    renderWithProviders(<CreateComplaint />)

    const gpsBtn = screen.getByTestId('btn-detect-gps')
    fireEvent.click(gpsBtn)

    await waitFor(() => {
      expect(mockGeolocation.getCurrentPosition).toHaveBeenCalled()
      expect(screen.getByTestId('gps-coordinates-pill')).toBeDefined()
      expect(screen.getByText(/18.520430, 73.856740/)).toBeDefined()
    })
  })

  it('supports selecting and previewing photo evidence', async () => {
    renderWithProviders(<CreateComplaint />)

    const fileInput = screen.getByTestId('photo-file-input')
    const file = new File(['dummy-image-content'], 'pothole.jpg', { type: 'image/jpeg' })

    fireEvent.change(fileInput, { target: { files: [file] } })

    await waitFor(() => {
      expect(screen.getByTestId('photo-preview-card')).toBeDefined()
      expect(screen.getByText('pothole.jpg')).toBeDefined()
    })

    // Remove photo
    const removeBtn = screen.getByTestId('btn-remove-photo')
    fireEvent.click(removeBtn)

    await waitFor(() => {
      expect(screen.queryByTestId('photo-preview-card')).toBeNull()
      expect(screen.getByTestId('photo-dropzone')).toBeDefined()
    })
  })

  it('successfully submits valid complaint and displays generated Complaint ID', async () => {
    const mockCreateComplaint = vi.spyOn(complaintService, 'createComplaint').mockResolvedValue({
      success: true,
      data: {
        id: 42,
        complaintNumber: 'CE-2026-0042',
        title: 'Dangerous Pothole on Sector 5',
        description: 'Large crater causing vehicular accidents near school gate',
        category: 'ROADS_AND_FOOTPATHS',
        status: 'SUBMITTED',
        priority: 'MEDIUM',
        location: 'Sector 5 Main Crossing',
        imageUrl: null,
      },
    })

    renderWithProviders(<CreateComplaint />)

    // Fill form
    fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))
    fireEvent.change(screen.getByTestId('complaint-title-input'), {
      target: { value: 'Dangerous Pothole on Sector 5' },
    })
    fireEvent.change(screen.getByTestId('complaint-description-input'), {
      target: { value: 'Large crater causing vehicular accidents near school gate' },
    })
    fireEvent.change(screen.getByTestId('complaint-location-input'), {
      target: { value: 'Sector 5 Main Crossing' },
    })

    // Submit
    fireEvent.click(screen.getByTestId('btn-submit-complaint'))

    await waitFor(() => {
      expect(mockCreateComplaint).toHaveBeenCalled()
      expect(screen.getByTestId('complaint-success-screen')).toBeDefined()
      expect(screen.getByTestId('complaint-number-display').textContent).toBe('CE-2026-0042')
      expect(screen.getByText('Dangerous Pothole on Sector 5')).toBeDefined()
    })
  })

  it('allows copying the complaint ID to clipboard on success screen', async () => {
    vi.spyOn(complaintService, 'createComplaint').mockResolvedValue({
      success: true,
      data: {
        id: 42,
        complaintNumber: 'CE-2026-0042',
        title: 'Broken Streetlight',
        description: 'Pole lamp broken',
        category: 'STREETLIGHTS_AND_ELECTRICITY',
        status: 'SUBMITTED',
        priority: 'MEDIUM',
        location: 'Park Lane',
      },
    })

    const writeTextMock = vi.fn().mockResolvedValue()
    vi.stubGlobal('navigator', {
      ...global.navigator,
      clipboard: {
        writeText: writeTextMock,
      },
    })

    renderWithProviders(<CreateComplaint />)

    fireEvent.click(screen.getByTestId('category-btn-STREETLIGHTS_AND_ELECTRICITY'))
    fireEvent.change(screen.getByTestId('complaint-title-input'), { target: { value: 'Broken Streetlight' } })
    fireEvent.change(screen.getByTestId('complaint-description-input'), { target: { value: 'Pole lamp broken' } })
    fireEvent.change(screen.getByTestId('complaint-location-input'), { target: { value: 'Park Lane' } })

    fireEvent.click(screen.getByTestId('btn-submit-complaint'))

    await waitFor(() => {
      expect(screen.getByTestId('btn-copy-id')).toBeDefined()
    })

    fireEvent.click(screen.getByTestId('btn-copy-id'))
    expect(writeTextMock).toHaveBeenCalledWith('CE-2026-0042')
  })

  describe('Voice-to-Text Complaint Reporting', () => {
    it('renders voice language selector and speak buttons for title and description', () => {
      renderWithProviders(<CreateComplaint />)

      expect(screen.getByTestId('voice-lang-group')).toBeDefined()
      expect(screen.getByTestId('voice-lang-select')).toBeDefined()
      expect(screen.getByTestId('btn-voice-title')).toBeDefined()
      expect(screen.getByTestId('btn-voice-title').getAttribute('title')).toBe('Mick')
      expect(screen.getByTestId('btn-voice-title').textContent).toContain('Mick')
      expect(screen.getByTestId('btn-voice-description')).toBeDefined()
      expect(screen.getByTestId('btn-voice-description').getAttribute('title')).toBe('Mick')
      expect(screen.getByTestId('btn-voice-description').textContent).toContain('Mick')

      const options = screen.getByTestId('voice-lang-select').querySelectorAll('option')
      expect(options.length).toBeGreaterThanOrEqual(5)
    })

    it('shows polite error alert if speech recognition is not supported in the browser', async () => {
      // Ensure no SpeechRecognition on window
      delete window.SpeechRecognition
      delete window.webkitSpeechRecognition

      renderWithProviders(<CreateComplaint />)

      const voiceTitleBtn = screen.getByTestId('btn-voice-title')
      fireEvent.click(voiceTitleBtn)

      await waitFor(() => {
        expect(screen.getByTestId('speech-error-alert')).toBeDefined()
        expect(screen.getByText(/Speech recognition is not supported/i)).toBeDefined()
      })

      // Dismiss error
      fireEvent.click(screen.getByTestId('btn-dismiss-speech-error'))
      expect(screen.queryByTestId('speech-error-alert')).toBeNull()
    })

    it('transcribes spoken voice into Complaint Title when speech recognized', async () => {
      let activeRecognitionInstance = null

      class MockSpeechRecognition {
        constructor() {
          this.lang = 'en-IN'
          this.continuous = false
          this.interimResults = false
          this.onstart = null
          this.onresult = null
          this.onerror = null
          this.onend = null
          activeRecognitionInstance = this
        }
        start() {
          if (this.onstart) this.onstart()
        }
        stop() {
          if (this.onend) this.onend()
        }
        abort() {
          if (this.onend) this.onend()
        }
      }

      window.SpeechRecognition = MockSpeechRecognition

      renderWithProviders(<CreateComplaint />)

      const voiceTitleBtn = screen.getByTestId('btn-voice-title')
      fireEvent.click(voiceTitleBtn)

      // Verify listening card appears
      await waitFor(() => {
        expect(screen.getByTestId('voice-listening-title')).toBeDefined()
        expect(screen.getByText(/Listening for Title/i)).toBeDefined()
      })

      // Simulate recognition result event
      act(() => {
        activeRecognitionInstance.results = [
          [{ transcript: 'Severe waterlogging near metro station' }],
        ]
        activeRecognitionInstance.results[0].isFinal = true
        activeRecognitionInstance.onresult({
          resultIndex: 0,
          results: activeRecognitionInstance.results,
        })
      })

      await waitFor(() => {
        expect(screen.getByTestId('complaint-title-input').value).toBe('Severe waterlogging near metro station')
      })

      // Click Done
      fireEvent.click(screen.getByTestId('btn-voice-done-title'))
      expect(screen.queryByTestId('voice-listening-title')).toBeNull()

      delete window.SpeechRecognition
    })

    it('transcribes spoken voice into Detailed Description and supports language change', async () => {
      let activeRecognitionInstance = null

      class MockSpeechRecognition {
        constructor() {
          this.lang = 'en-IN'
          this.continuous = false
          this.interimResults = false
          this.onstart = null
          this.onresult = null
          this.onerror = null
          this.onend = null
          activeRecognitionInstance = this
        }
        start() {
          if (this.onstart) this.onstart()
        }
        stop() {
          if (this.onend) this.onend()
        }
        abort() {
          if (this.onend) this.onend()
        }
      }

      window.webkitSpeechRecognition = MockSpeechRecognition

      renderWithProviders(<CreateComplaint />)

      // Change speech language to Hindi
      const langSelect = screen.getByTestId('voice-lang-select')
      fireEvent.change(langSelect, { target: { value: 'hi-IN' } })

      const voiceDescBtn = screen.getByTestId('btn-voice-description')
      fireEvent.click(voiceDescBtn)

      await waitFor(() => {
        expect(screen.getByTestId('voice-listening-description')).toBeDefined()
        expect(activeRecognitionInstance.lang).toBe('hi-IN')
      })

      // Simulate recognition result
      act(() => {
        const mockResultEvent = {
          resultIndex: 0,
          results: [
            [{ transcript: 'यहाँ सड़क पर कचरे का बहुत बड़ा ढेर लगा है।' }],
          ],
        }
        mockResultEvent.results[0].isFinal = true
        activeRecognitionInstance.onresult(mockResultEvent)
      })

      await waitFor(() => {
        expect(screen.getByTestId('complaint-description-input').value).toContain('कचरे का बहुत बड़ा ढेर')
      })

      // Toggle off listening
      fireEvent.click(screen.getByTestId('btn-voice-description'))
      expect(screen.queryByTestId('voice-listening-description')).toBeNull()

      delete window.webkitSpeechRecognition
    })

    it('handles microphone permission denied error gracefully', async () => {
      let activeRecognitionInstance = null

      class MockSpeechRecognition {
        constructor() {
          this.onstart = null
          this.onerror = null
          this.onend = null
          activeRecognitionInstance = this
        }
        start() {
          if (this.onstart) this.onstart()
        }
        stop() {
          if (this.onend) this.onend()
        }
        abort() {
          if (this.onend) this.onend()
        }
      }

      window.SpeechRecognition = MockSpeechRecognition

      renderWithProviders(<CreateComplaint />)

      fireEvent.click(screen.getByTestId('btn-voice-title'))

      await waitFor(() => {
        expect(screen.getByTestId('voice-listening-title')).toBeDefined()
      })

      // Trigger permission denied error
      act(() => {
        activeRecognitionInstance.onerror({ error: 'not-allowed' })
      })

      await waitFor(() => {
        expect(screen.getByTestId('speech-error-alert')).toBeDefined()
        expect(screen.getByText(/Microphone permission was denied/i)).toBeDefined()
        expect(screen.queryByTestId('voice-listening-title')).toBeNull()
      })

      delete window.SpeechRecognition
    })
  })

  describe('Progressive Question Step Workflow', () => {
    it('advances from Question 1 to Question 5 upon submitting answers', async () => {
      renderWithProviders(<CreateComplaint />)

      // Stepper initially at Question 1
      expect(screen.getByText(/Question 1 of 5/i)).toBeDefined()

      // Question 1 submit button without category triggers error
      const q1Submit = screen.getByTestId('btn-submit-q1')
      fireEvent.click(q1Submit)
      expect(screen.getByTestId('category-error')).toBeDefined()

      // Select category and click submit on Question 1
      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))
      fireEvent.click(q1Submit)

      // Advances to Question 2
      expect(screen.getByText(/Question 2 of 5/i)).toBeDefined()
      expect(screen.getByTestId('question-step-2').className).toContain('question-visible')

      // Question 2 submit button without title triggers error
      const q2Submit = screen.getByTestId('btn-submit-q2')
      fireEvent.click(q2Submit)
      expect(screen.getByTestId('title-error')).toBeDefined()

      // Provide title and click submit on Question 2
      fireEvent.change(screen.getByTestId('complaint-title-input'), {
        target: { value: 'Deep crater on highway' },
      })
      fireEvent.click(q2Submit)

      // Advances to Question 3
      expect(screen.getByText(/Question 3 of 5/i)).toBeDefined()
      expect(screen.getByTestId('question-step-3').className).toContain('question-visible')

      // Question 3 submit button without description triggers error
      const q3Submit = screen.getByTestId('btn-submit-q3')
      fireEvent.click(q3Submit)
      expect(screen.getByTestId('description-error')).toBeDefined()

      // Provide description and click submit on Question 3
      fireEvent.change(screen.getByTestId('complaint-description-input'), {
        target: { value: 'Dangerous pothole damaging vehicles' },
      })
      fireEvent.click(q3Submit)

      // Advances to Question 4
      expect(screen.getByText(/Question 4 of 5/i)).toBeDefined()
      expect(screen.getByTestId('question-step-4').className).toContain('question-visible')

      // Question 4 submit button without location triggers error
      const q4Submit = screen.getByTestId('btn-submit-q4')
      fireEvent.click(q4Submit)
      expect(screen.getByTestId('location-error')).toBeDefined()

      // Provide location and click submit on Question 4
      fireEvent.change(screen.getByTestId('complaint-location-input'), {
        target: { value: 'Near metro pillar 124' },
      })
      fireEvent.click(q4Submit)

      // Advances to Question 5
      expect(screen.getByText(/Question 5 of 5/i)).toBeDefined()
      expect(screen.getByTestId('question-step-5').className).toContain('question-visible')
      expect(screen.getByTestId('btn-submit-complaint')).toBeDefined()
    })
  })

  describe('AI Photo Relevance Verification & Automatic Rejection', () => {
    it('verifies valid matching photo evidence and displays AI verified badge', async () => {
      renderWithProviders(<CreateComplaint />)

      // Step 1: Select Roads & Footpaths
      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))

      // Step 5: Upload valid matching photo
      const fileInput = screen.getByTestId('photo-file-input')
      const validPhoto = new File(['road-crater-content'], 'road_pothole_crater.jpg', {
        type: 'image/jpeg',
      })
      fireEvent.change(fileInput, { target: { files: [validPhoto] } })

      await waitFor(() => {
        expect(screen.getByTestId('photo-preview-card')).toBeDefined()
        expect(screen.getByTestId('badge-photo-passed')).toBeDefined()
        expect(screen.getByTestId('ai-photo-verified')).toBeDefined()
        expect(screen.getByText(/AI Verified: Photo Matches Category/i)).toBeDefined()
      })

      // Submit button is not blocked
      const submitBtn = screen.getByTestId('btn-submit-complaint')
      expect(submitBtn.className).not.toContain('btn-submit-blocked')
    })

    it('rejects unrelated photo (selfie) and blocks complaint submission', async () => {
      renderWithProviders(<CreateComplaint />)

      // Select Roads & Footpaths
      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))

      // Upload unrelated photo (selfie)
      const fileInput = screen.getByTestId('photo-file-input')
      const unrelatedPhoto = new File(['selfie-content'], 'my_selfie_portrait.jpg', {
        type: 'image/jpeg',
      })
      fireEvent.change(fileInput, { target: { files: [unrelatedPhoto] } })

      await waitFor(() => {
        expect(screen.getByTestId('photo-preview-card')).toBeDefined()
        expect(screen.getByTestId('badge-photo-rejected')).toBeDefined()
        expect(screen.getByTestId('ai-photo-rejected')).toBeDefined()
        expect(screen.getByText(/AI Photo Verification Failed: Complaint Rejected/i)).toBeDefined()
        expect(screen.getAllByText(/Personal Selfie \/ Portrait/i).length).toBeGreaterThan(0)
        expect(screen.getByTestId('photo-rejection-notice')).toBeDefined()
      })

      // Submit button is blocked
      const submitBtn = screen.getByTestId('btn-submit-complaint')
      expect(submitBtn.className).toContain('btn-submit-blocked')
      expect(submitBtn.getAttribute('disabled')).not.toBeNull()

      // Removing rejected photo clears rejection state
      const removeBtn = screen.getByTestId('btn-remove-rejected-photo')
      fireEvent.click(removeBtn)

      await waitFor(() => {
        expect(screen.queryByTestId('photo-preview-card')).toBeNull()
        expect(screen.queryByTestId('ai-photo-rejected')).toBeNull()
        expect(screen.queryByTestId('photo-rejection-notice')).toBeNull()
      })
    })

    it('rejects cross-category mismatched photo (streetlight photo uploaded for garbage complaint)', async () => {
      renderWithProviders(<CreateComplaint />)

      // Select Garbage & Waste
      fireEvent.click(screen.getByTestId('category-btn-GARBAGE_AND_CLEANLINESS'))

      // Upload photo showing streetlight / electrical equipment
      const fileInput = screen.getByTestId('photo-file-input')
      const mismatchedPhoto = new File(['pole-content'], 'broken_streetlight_pole.jpg', {
        type: 'image/jpeg',
      })
      fireEvent.change(fileInput, { target: { files: [mismatchedPhoto] } })

      await waitFor(() => {
        expect(screen.getByTestId('ai-photo-rejected')).toBeDefined()
        expect(screen.getAllByText(/Streetlight & Electrical Infrastructure/i).length).toBeGreaterThan(0)
        expect(screen.getByTestId('btn-reupload-photo')).toBeDefined()
      })
    })

    it('re-evaluates uploaded photo when user switches issue category', async () => {
      renderWithProviders(<CreateComplaint />)

      // Select Roads & Footpaths
      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))

      // Upload pothole photo -> Passes
      const fileInput = screen.getByTestId('photo-file-input')
      const photo = new File(['pothole-content'], 'deep_pothole_asphalt.jpg', {
        type: 'image/jpeg',
      })
      fireEvent.change(fileInput, { target: { files: [photo] } })

      await waitFor(() => {
        expect(screen.getByTestId('ai-photo-verified')).toBeDefined()
      })

      // Switch category to Streetlights & Power
      fireEvent.click(screen.getByTestId('category-btn-STREETLIGHTS_AND_ELECTRICITY'))

      // Pothole photo is now recognized as mismatched and rejected
      await waitFor(() => {
        expect(screen.getByTestId('ai-photo-rejected')).toBeDefined()
        expect(screen.getAllByText(/Road Surface & Pothole Damage/i).length).toBeGreaterThan(0)
        expect(screen.getByTestId('btn-submit-complaint').className).toContain('btn-submit-blocked')
      })
    })

    it('rejects generic file named images.jpg when visual features detect road potholes for a streetlight issue', async () => {
      renderWithProviders(<CreateComplaint />)

      // Step 1: Select Streetlights & Power
      fireEvent.click(screen.getByTestId('category-btn-STREETLIGHTS_AND_ELECTRICITY'))

      // Fill in complaint details
      fireEvent.change(screen.getByTestId('complaint-title-input'), {
        target: { value: 'broken Street lamp problem' },
      })

      // Upload image named images.jpg (the exact name Google Images gives when user downloads pothole photo)
      const fileInput = screen.getByTestId('photo-file-input')
      const potholeImage = new File(['pothole-raw-pixel-data'], 'images.jpg', {
        type: 'image/jpeg',
      })

      // Mock verifyPhotoRelevance to return visual road detection (as canvas pixel analysis detects in browser)
      const spy = vi.spyOn(aiService, 'verifyPhotoRelevance').mockResolvedValueOnce({
        valid: false,
        isRejected: true,
        category: 'STREETLIGHTS_AND_ELECTRICITY',
        categoryLabel: 'Streetlights & Power',
        detectedSubject: 'Road Surface & Pothole Damage',
        confidenceScore: 0.95,
        rejectionReason: 'AI Rejection: Visual analysis detected "Road Surface & Pothole Damage" (65% road/ground detected). This does not match your selected category "Streetlights & Power".',
        detailedReason: 'The uploaded photo depicts a road defect or pothole rather than streetlight or electrical equipment. Please switch category to "Roads & Footpaths" or upload a photo showing the damaged street lamp.',
        matchedFeatures: ['visual_pothole_mismatch'],
      })

      fireEvent.change(fileInput, { target: { files: [potholeImage] } })

      await waitFor(() => {
        // Must show AI Photo Verification Failed
        expect(screen.getByTestId('ai-photo-rejected')).toBeDefined()
        expect(screen.getByText(/AI Photo Verification Failed: Complaint Rejected/i)).toBeDefined()
        expect(screen.getAllByText(/Road Surface & Pothole Damage/i).length).toBeGreaterThan(0)
        expect(screen.getByTestId('badge-photo-rejected')).toBeDefined()
        expect(screen.getByTestId('photo-rejection-notice')).toBeDefined()
      })

      // Submit complaint button MUST be blocked by AI
      const submitBtn = screen.getByTestId('btn-submit-complaint')
      expect(submitBtn.className).toContain('btn-submit-blocked')
      expect(submitBtn.getAttribute('disabled')).not.toBeNull()
      expect(screen.getByText(/Submission Blocked by AI/i)).toBeDefined()

      spy.mockRestore()
    })
  })

  describe('analyzePixelData & AI Vision Logic', () => {
    it('accurately identifies asphalt and pothole pixels from canvas pixel buffer', () => {
      const size = 64
      const buffer = new Uint8ClampedArray(size * size * 4)

      // Fill lower 60% with desaturated asphalt / road gray (r=60, g=60, b=60, a=255)
      // and deep pothole cavity shadow (r=30, g=30, b=30, a=255)
      for (let y = 0; y < size; y++) {
        for (let x = 0; x < size; x++) {
          const idx = (y * size + x) * 4
          if (y >= 24) {
            // Lower 60% is road surface & pothole crater
            const isCrater = (x >= 20 && x <= 44 && y >= 30 && y <= 50)
            const val = isCrater ? 25 : 65
            buffer[idx] = val
            buffer[idx + 1] = val
            buffer[idx + 2] = val
            buffer[idx + 3] = 255
          } else {
            // Upper 40% is sky or background
            buffer[idx] = 140
            buffer[idx + 1] = 180
            buffer[idx + 2] = 220
            buffer[idx + 3] = 255
          }
        }
      }

      const features = analyzePixelData(buffer, size)
      expect(features).not.toBeNull()
      expect(features.bottomRoadRatio).toBeGreaterThan(0.90)
      expect(features.roadRatio).toBeGreaterThan(0.55)
      expect(features.skinRatio).toBe(0)
    })

    it('rejects pothole visual features when uploaded for streetlights category', () => {
      const roadVisual = {
        roadRatio: 0.55,
        bottomRoadRatio: 0.65,
        topSkyRatio: 0.15,
        skinRatio: 0,
        garbageRatio: 0.05,
      }
      const file = { name: 'images.jpg' }
      const result = computeLocalPhotoVerification('STREETLIGHTS_AND_ELECTRICITY', file, roadVisual)
      expect(result.valid).toBe(false)
      expect(result.isRejected).toBe(true)
      expect(result.detectedSubject).toBe('Road Surface & Pothole Damage')
      expect(result.rejectionReason).toContain('Road Surface & Pothole Damage')
      expect(result.rejectionReason).toContain('Streetlights & Power')
    })

    it('verifies pothole visual features when uploaded for roads category', () => {
      const roadVisual = {
        roadRatio: 0.55,
        bottomRoadRatio: 0.65,
        topSkyRatio: 0.15,
        skinRatio: 0,
        garbageRatio: 0.05,
      }
      const file = { name: 'images.jpg' }
      const result = computeLocalPhotoVerification('ROADS_AND_FOOTPATHS', file, roadVisual)
      expect(result.valid).toBe(true)
      expect(result.isRejected).toBe(false)
      expect(result.detectedSubject).toBe('Road Surface & Pothole Damage')
    })
  })
})
