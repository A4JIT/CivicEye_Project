import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext'
import aiService from '../src/services/aiService'
import complaintService from '../src/services/complaintService'
import CreateComplaint from '../src/pages/citizen/CreateComplaint'
import ReviewComplaint from '../src/pages/admin/ReviewComplaint'

// Mock react-router useNavigate
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const renderWithProviders = (ui) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  )
}

describe('Duplicate Complaint Detection & Endorsement System', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('aiService.checkDuplicateComplaint', () => {
    it('detects duplicate for matching title and category against active complaints pool', async () => {
      const result = await aiService.checkDuplicateComplaint({
        title: 'Severe crater pothole on MG Road near Metro Station',
        description: 'Large pothole damaging cars and rims near pillar 24.',
        category: 'ROADS_AND_FOOTPATHS',
        location: 'MG Road Metro Station',
      })

      expect(result).toBeDefined()
      expect(result.isDuplicate).toBe(true)
      expect(result.topMatch).toBeDefined()
      expect(result.topMatch.complaintNumber).toBe('CE-2026-0001')
      expect(result.highestScore).toBeGreaterThanOrEqual(0.65)
    })

    it('detects duplicate based on GPS proximity and category match', async () => {
      const result = await aiService.checkDuplicateComplaint({
        title: 'Road damage with hole',
        description: 'Road surface broken',
        category: 'ROADS_AND_FOOTPATHS',
        latitude: 12.97165,
        longitude: 77.59462,
      })

      expect(result).toBeDefined()
      expect(result.isDuplicate).toBe(true)
      expect(result.topMatch.complaintNumber).toBe('CE-2026-0001')
      expect(result.topMatch.distanceMeters).toBeLessThanOrEqual(50)
    })

    it('returns isDuplicate false for non-matching or novel complaints', async () => {
      const result = await aiService.checkDuplicateComplaint({
        title: 'Unique park bench broken in Whitefield layout park',
        description: 'Wooden slats missing from public bench.',
        category: 'OTHER',
        location: 'Whitefield Inner Park',
      })

      expect(result.isDuplicate).toBe(false)
    })
  })

  describe('complaintService duplicate actions', () => {
    it('upvoteComplaint returns updated upvotes count', async () => {
      const res = await complaintService.upvoteComplaint(1)
      expect(res.success).toBe(true)
      expect(res.data.upvotesCount).toBeDefined()
    })

    it('mergeDuplicate marks complaint as merged duplicate', async () => {
      const res = await complaintService.mergeDuplicate(2, 1, 'Duplicate issue on same street')
      expect(res.success).toBe(true)
      expect(res.data.status).toBe('REJECTED')
    })
  })

  describe('CreateComplaint citizen duplicate alert & upvote flow', () => {
    it('shows duplicate warning when citizen enters matching issue details', async () => {
      renderWithProviders(<CreateComplaint />)

      // Select Roads category
      const roadBtn = screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS')
      fireEvent.click(roadBtn)

      // Advance to Step 2
      fireEvent.click(screen.getByTestId('btn-submit-q1'))

      // Enter matching title
      const titleInput = screen.getByTestId('complaint-title-input')
      fireEvent.change(titleInput, {
        target: { value: 'Severe crater pothole on MG Road near Metro Station' },
      })

      // Advance to Step 3
      fireEvent.click(screen.getByTestId('btn-submit-q2'))

      const descInput = screen.getByTestId('complaint-description-input')
      fireEvent.change(descInput, {
        target: { value: 'Deep crater pothole damaging car wheels near metro pillar 24' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q3'))

      const locInput = screen.getByTestId('complaint-location-input')
      fireEvent.change(locInput, {
        target: { value: 'MG Road Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q4'))

      // Verify Duplicate Warning Card appears
      await waitFor(() => {
        expect(screen.getByTestId('duplicate-warning-card')).toBeDefined()
      }, { timeout: 2500 })

      expect(screen.getByTestId('duplicate-similarity-badge')).toBeDefined()
      expect(screen.getByTestId('duplicate-ticket-number')).toBeDefined()
      expect(screen.getByTestId('btn-upvote-duplicate')).toBeDefined()
      expect(screen.getByTestId('btn-dismiss-duplicate')).toBeDefined()
    })

    it('upvoting duplicate displays endorsement confirmation screen without submitting new ticket', async () => {
      renderWithProviders(<CreateComplaint />)

      // Fill in matching issue
      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))
      fireEvent.click(screen.getByTestId('btn-submit-q1'))

      const titleInput = screen.getByTestId('complaint-title-input')
      fireEvent.change(titleInput, {
        target: { value: 'Severe crater pothole on MG Road near Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q2'))

      const descInput = screen.getByTestId('complaint-description-input')
      fireEvent.change(descInput, {
        target: { value: 'Deep road crater causing vehicle rim damage' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q3'))

      fireEvent.change(screen.getByTestId('complaint-location-input'), {
        target: { value: 'MG Road Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q4'))

      // Wait for warning
      const upvoteBtn = await screen.findByTestId('btn-upvote-duplicate', {}, { timeout: 2500 })
      fireEvent.click(upvoteBtn)

      // Should show upvote success screen
      await waitFor(() => {
        expect(screen.getByTestId('complaint-upvote-success-screen')).toBeDefined()
      })

      expect(screen.getByTestId('upvoted-number-display')).toBeDefined()
      expect(screen.getByTestId('btn-track-existing')).toBeDefined()
    })

    it('dismissing duplicate warning hides the card and allows normal filing', async () => {
      renderWithProviders(<CreateComplaint />)

      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))
      fireEvent.click(screen.getByTestId('btn-submit-q1'))

      const titleInput = screen.getByTestId('complaint-title-input')
      fireEvent.change(titleInput, {
        target: { value: 'Severe crater pothole on MG Road near Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q2'))

      const descInput = screen.getByTestId('complaint-description-input')
      fireEvent.change(descInput, {
        target: { value: 'Deep road crater causing vehicle rim damage' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q3'))

      fireEvent.change(screen.getByTestId('complaint-location-input'), {
        target: { value: 'MG Road Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q4'))

      const dismissBtn = await screen.findByTestId('btn-dismiss-duplicate', {}, { timeout: 2500 })
      fireEvent.click(dismissBtn)

      await waitFor(() => {
        expect(screen.queryByTestId('duplicate-warning-card')).toBeNull()
      })
    })

    it('prompts confirmation modal if citizen attempts to submit without addressing detected duplicate', async () => {
      renderWithProviders(<CreateComplaint />)

      fireEvent.click(screen.getByTestId('category-btn-ROADS_AND_FOOTPATHS'))
      fireEvent.click(screen.getByTestId('btn-submit-q1'))

      const titleInput = screen.getByTestId('complaint-title-input')
      fireEvent.change(titleInput, {
        target: { value: 'Severe crater pothole on MG Road near Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q2'))

      const descInput = screen.getByTestId('complaint-description-input')
      fireEvent.change(descInput, {
        target: { value: 'Deep road crater causing vehicle rim damage' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q3'))

      fireEvent.change(screen.getByTestId('complaint-location-input'), {
        target: { value: 'MG Road Metro Station' },
      })
      fireEvent.click(screen.getByTestId('btn-submit-q4'))

      // Wait for duplicate to be detected
      await screen.findByTestId('duplicate-warning-card', {}, { timeout: 2500 })

      // Click final submit complaint without dismissing
      const submitBtn = screen.getByTestId('btn-submit-complaint')
      fireEvent.click(submitBtn)

      // Confirm modal opens
      expect(screen.getByTestId('btn-modal-upvote-duplicate')).toBeDefined()
      expect(screen.getByTestId('btn-force-submit-duplicate')).toBeDefined()
    })
  })

  describe('ReviewComplaint admin duplicate badge and triage', () => {
    it('renders duplicate parent box and upvote badge when complaint is a linked duplicate', () => {
      const mockComplaint = {
        id: 99,
        complaintNumber: 'CE-2026-0099',
        title: 'Road hole near station',
        description: 'Road damage',
        category: 'ROADS_AND_FOOTPATHS',
        status: 'REJECTED',
        priority: 'HIGH',
        location: 'MG Road',
        upvotesCount: 4,
        duplicateOfId: 1,
        duplicateOfNumber: 'CE-2026-0001',
        duplicateScore: 0.88,
        createdAt: '2026-10-15T10:00:00Z',
      }

      renderWithProviders(<ReviewComplaint complaintProp={mockComplaint} />)

      expect(screen.getByTestId('badge-upvotes-count').textContent).toContain('4')
      expect(screen.getByTestId('badge-duplicate-status')).toBeDefined()
      expect(screen.getByTestId('duplicate-parent-box')).toBeDefined()
      expect(screen.getByTestId('duplicate-parent-box').textContent).toContain('CE-2026-0001')
    })

    it('shows parent complaint input when DUPLICATE rejection category is selected', () => {
      const mockComplaint = {
        id: 50,
        complaintNumber: 'CE-2026-0050',
        title: 'Flickering street lamp',
        description: 'Lamp flickering',
        category: 'STREETLIGHTS_AND_ELECTRICITY',
        status: 'SUBMITTED',
        priority: 'MEDIUM',
        location: '5th Main Avenue',
        createdAt: '2026-10-15T10:00:00Z',
      }

      renderWithProviders(<ReviewComplaint complaintProp={mockComplaint} />)

      fireEvent.click(screen.getByTestId('btn-open-reject-modal'))

      const categorySelect = screen.getByTestId('select-reject-category')
      fireEvent.change(categorySelect, { target: { value: 'DUPLICATE' } })

      expect(screen.getByTestId('duplicate-parent-input-group')).toBeDefined()
      expect(screen.getByTestId('input-duplicate-parent-id')).toBeDefined()
    })
  })
})
