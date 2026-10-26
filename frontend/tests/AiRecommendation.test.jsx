import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthContext, AuthProvider } from '../src/context/AuthContext'
import CreateComplaint from '../src/pages/citizen/CreateComplaint'
import AllComplaints from '../src/pages/admin/AllComplaints'
import ReviewComplaint from '../src/pages/admin/ReviewComplaint'
import aiService from '../src/services/aiService'
import adminService from '../src/services/adminService'

const mockAdminUser = {
  id: 1,
  username: 'admin',
  email: 'admin@civiceye.com',
  fullName: 'Administrator',
  role: 'ADMIN',
}

const renderWithProviders = (ui, user = mockAdminUser) => {
  return render(
    <AuthContext.Provider value={{ user, logout: vi.fn() }}>
      <BrowserRouter>{ui}</BrowserRouter>
    </AuthContext.Provider>
  )
}

describe('AI Recommendation & Triage Suite (Phase 7)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('aiService Unit Tests', () => {
    it('evaluates text and computes fallback recommendation when title is provided', async () => {
      const rec = await aiService.getRealtimeRecommendation({
        title: 'Massive burst water pipeline flooding road',
        description: 'Clean drinking water is gushing out on the street',
        location: 'MG Road Junction',
      })

      expect(rec).toBeDefined()
      expect(rec.suggestedCategory).toBe('WATER_SUPPLY_AND_LEAKAGE')
      expect(rec.suggestedPriority).toBe('CRITICAL')
      expect(rec.suggestedDepartmentName).toBe('Water Supply & Sewerage')
      expect(rec.confidenceScore).toBeGreaterThanOrEqual(0.65)
      expect(rec.explanation).toContain('Keywords detected')
    })

    it('returns null if title is blank or too short', async () => {
      const rec = await aiService.getRealtimeRecommendation({ title: 'ab' })
      expect(rec).toBeNull()
    })
  })

  describe('Citizen Portal Real-Time AI Assistant (CreateComplaint)', () => {
    it('displays AI suggestion widget and confidence meter when typing problem headline', async () => {
      render(
        <BrowserRouter>
          <AuthProvider>
            <CreateComplaint />
          </AuthProvider>
        </BrowserRouter>
      )

      const titleInput = screen.getByTestId('complaint-title-input')
      fireEvent.change(titleInput, { target: { value: 'Severe deep pothole and broken road' } })

      await waitFor(() => {
        expect(screen.getByTestId('ai-realtime-widget')).toBeDefined()
        expect(screen.getByTestId('ai-suggested-category')).toBeDefined()
        expect(screen.getByTestId('ai-confidence-meter')).toBeDefined()
      }, { timeout: 3000 })

      expect(screen.getByText(/roads & footpaths/i)).toBeDefined()
      expect(screen.getByTestId('btn-apply-ai-category')).toBeDefined()
    })

    it('applies suggested category on 1-click and updates applied badge', async () => {
      render(
        <BrowserRouter>
          <AuthProvider>
            <CreateComplaint />
          </AuthProvider>
        </BrowserRouter>
      )

      const titleInput = screen.getByTestId('complaint-title-input')
      fireEvent.change(titleInput, { target: { value: 'Overflowing garbage bin and terrible waste smell' } })

      await waitFor(() => {
        expect(screen.getByTestId('btn-apply-ai-category')).toBeDefined()
      }, { timeout: 3000 })

      const applyBtn = screen.getByTestId('btn-apply-ai-category')
      fireEvent.click(applyBtn)

      await waitFor(() => {
        expect(screen.getByTestId('badge-category-applied')).toBeDefined()
        expect(screen.getByText(/✓ Applied/i)).toBeDefined()
      })
    })
  })

  describe('Admin Triage Console AI Badges (AllComplaints)', () => {
    const mockComplaints = [
      {
        id: 101,
        complaintNumber: 'CE-2026-0101',
        title: 'Dangling electric wire sparking near school',
        category: 'STREETLIGHTS_AND_ELECTRICITY',
        status: 'SUBMITTED',
        priority: 'CRITICAL',
        location: 'Oakridge School Road',
        citizenName: 'John Citizen',
        citizenEmail: 'citizen@civiceye.com',
        departmentId: 2,
        departmentName: 'Electricity & Street Lighting',
        aiCategoryOverridden: false,
        aiPriorityOverridden: false,
        aiRecommendation: {
          suggestedCategory: 'STREETLIGHTS_AND_ELECTRICITY',
          suggestedPriority: 'CRITICAL',
          suggestedDepartmentCode: 'ELECTRICITY',
          confidenceScore: 0.92,
          isOverridden: false,
        },
      },
      {
        id: 102,
        complaintNumber: 'CE-2026-0102',
        title: 'Neighbor tree branches hanging over fence',
        category: 'OTHER',
        status: 'UNDER_REVIEW',
        priority: 'LOW',
        location: 'Green Park Lane',
        citizenName: 'Jane Citizen',
        citizenEmail: 'jane@civiceye.com',
        departmentId: 1,
        departmentName: 'Public Works & Roads',
        aiCategoryOverridden: true,
        aiPriorityOverridden: false,
        isOverridden: true,
      },
    ]

    it('renders AI suggestion chips with confidence score and department', async () => {
      vi.spyOn(adminService, 'getAllComplaints').mockResolvedValue({
        complaints: mockComplaints,
        totalItems: 2,
      })
      vi.spyOn(adminService, 'getDepartments').mockResolvedValue([
        { id: 1, name: 'Public Works & Roads' },
        { id: 2, name: 'Electricity & Street Lighting' },
      ])

      renderWithProviders(<AllComplaints />)

      await waitFor(() => {
        expect(screen.getByText('CE-2026-0101')).toBeDefined()
        expect(screen.getByText('CE-2026-0102')).toBeDefined()
      })

      // Check for AI Match suggestion chip
      const suggestionChips = screen.getAllByTestId('chip-ai-suggestion')
      expect(suggestionChips.length).toBeGreaterThan(0)
      expect(suggestionChips[0].textContent).toContain('ELECTRICITY')
      expect(suggestionChips[0].textContent).toContain('92%')

      // Check for Overridden chip on complaint 102
      expect(screen.getByTestId('chip-ai-overridden')).toBeDefined()
    })
  })

  describe('Admin Review Drawer AI Panel & Overrides (ReviewComplaint)', () => {
    const sampleComplaint = {
      id: 201,
      complaintNumber: 'CE-2026-0201',
      title: 'Water pipe leak on Market Street',
      description: 'Clean water is flooding the whole footpath and basement entry',
      category: 'WATER_AND_SEWAGE',
      status: 'SUBMITTED',
      priority: 'HIGH',
      location: 'Market Street, Block B',
      citizenName: 'Raj Patel',
      citizenEmail: 'raj@example.com',
      departmentId: null,
      departmentName: null,
      aiCategoryOverridden: false,
      aiPriorityOverridden: false,
      isOverridden: false,
      aiRecommendation: {
        id: 50,
        suggestedCategory: 'WATER_AND_SEWAGE',
        suggestedPriority: 'HIGH',
        suggestedDepartmentId: 3,
        suggestedDepartmentName: 'Water Supply & Sewerage',
        confidenceScore: 0.86,
        explanation: 'Matched "pipe", "leak", "flooding" indicates High severity civic defect.',
        isOverridden: false,
      },
    }

    const mockDepts = [
      { id: 1, name: 'Public Works & Roads' },
      { id: 2, name: 'Electricity & Street Lighting' },
      { id: 3, name: 'Water Supply & Sewerage' },
    ]

    beforeEach(() => {
      vi.spyOn(adminService, 'getDepartments').mockResolvedValue(mockDepts)
      vi.spyOn(adminService, 'getComplaintById').mockResolvedValue(sampleComplaint)
    })

    it('renders the AI Analysis panel, confidence meter, and keyword justification', async () => {
      renderWithProviders(<ReviewComplaint complaintProp={sampleComplaint} isDrawer={true} />)

      expect(screen.getByTestId('ai-recommendation-panel')).toBeDefined()
      expect(screen.getByTestId('badge-ai-status')).toBeDefined()
      expect(screen.getByTestId('badge-ai-status').textContent).toContain('AI Verified')

      expect(screen.getByTestId('ai-confidence-pct').textContent).toBe('86%')
      expect(screen.getByTestId('ai-confidence-bar')).toBeDefined()
      expect(screen.getByTestId('ai-suggested-category-val')).toBeDefined()
      expect(screen.getByTestId('ai-suggested-priority-val').textContent).toBe('HIGH')
      expect(screen.getByTestId('ai-suggested-dept-val').textContent).toBe('Water Supply & Sewerage')

      expect(screen.getByTestId('ai-explanation-box')).toBeDefined()
      expect(screen.getByTestId('ai-explanation-box').textContent).toContain('Matched "pipe", "leak", "flooding"')
    })

    it('executes 1-click Accept AI Recommendation and updates status to ASSIGNED', async () => {
      const applySpy = vi.spyOn(aiService, 'applyRecommendation').mockResolvedValue({
        ...sampleComplaint,
        status: 'ASSIGNED',
        departmentId: 3,
        departmentName: 'Water Supply & Sewerage',
      })

      const onUpdatedMock = vi.fn()

      renderWithProviders(
        <ReviewComplaint complaintProp={sampleComplaint} onUpdated={onUpdatedMock} isDrawer={true} />
      )

      const acceptBtn = screen.getByTestId('btn-accept-ai-recommendation')
      fireEvent.click(acceptBtn)

      await waitFor(() => {
        expect(applySpy).toHaveBeenCalledWith(201)
        expect(screen.getByText(/AI recommendation applied successfully in 1-click triage/i)).toBeDefined()
        expect(onUpdatedMock).toHaveBeenCalled()
      })
    })

    it('toggles override form, validates reason, and submits manual override', async () => {
      const overrideSpy = vi.spyOn(aiService, 'overrideRecommendation').mockResolvedValue({
        ...sampleComplaint,
        category: 'ROADS_AND_FOOTPATHS',
        priority: 'CRITICAL',
        departmentId: 1,
        departmentName: 'Public Works & Roads',
        isOverridden: true,
      })

      const onUpdatedMock = vi.fn()

      renderWithProviders(
        <ReviewComplaint complaintProp={sampleComplaint} onUpdated={onUpdatedMock} isDrawer={true} />
      )

      // Toggle override form
      const toggleBtn = screen.getByTestId('btn-toggle-override')
      fireEvent.click(toggleBtn)

      expect(screen.getByTestId('ai-override-form')).toBeDefined()

      // Attempt submit without reason
      const submitOverrideBtn = screen.getByTestId('btn-submit-ai-override')
      fireEvent.click(submitOverrideBtn)

      await waitFor(() => {
        expect(screen.getByTestId('override-error-msg')).toBeDefined()
      })

      // Fill reason and submit
      const reasonInput = screen.getByTestId('textarea-ai-override-reason')
      fireEvent.change(reasonInput, { target: { value: 'Water leak also cracked the main road surface' } })

      const catSelect = screen.getByTestId('select-ai-override-category')
      fireEvent.change(catSelect, { target: { value: 'ROADS_AND_FOOTPATHS' } })

      fireEvent.click(submitOverrideBtn)

      await waitFor(() => {
        expect(overrideSpy).toHaveBeenCalled()
        expect(screen.getByText(/AI recommendation overridden successfully by administrator/i)).toBeDefined()
        expect(onUpdatedMock).toHaveBeenCalled()
      })
    })
  })
})
