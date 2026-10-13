import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext'
import CreateComplaint from '../src/pages/citizen/CreateComplaint'
import complaintService from '../src/services/complaintService'

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
})
