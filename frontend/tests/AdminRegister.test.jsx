import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext'
import AdminRegister from '../src/pages/public/AdminRegister'
import authService from '../src/services/authService'

const renderWithProviders = (ui) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  )
}

describe('AdminRegister Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders admin registration form with required special authorization code field', () => {
    renderWithProviders(<AdminRegister />)

    expect(screen.getByRole('heading', { name: /admin registration/i })).toBeDefined()
    expect(screen.getByLabelText(/special admin authorization code/i)).toBeDefined()
    expect(screen.getByLabelText(/administrator full name/i)).toBeDefined()
    expect(screen.getByLabelText(/admin username/i)).toBeDefined()
    expect(screen.getByLabelText(/official municipal email/i)).toBeDefined()
    expect(screen.getByLabelText(/password \(min 6 characters\)/i)).toBeDefined()
    expect(screen.getByLabelText(/confirm password/i)).toBeDefined()
  })

  it('validates that admin authorization code is required', async () => {
    renderWithProviders(<AdminRegister />)

    const submitBtn = screen.getByRole('button', { name: /authorize & create admin account/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(screen.getByText(/special admin authorization code is required/i)).toBeDefined()
    })
  })

  it('submits admin registration when valid code and credentials are provided', async () => {
    vi.spyOn(authService, 'registerAdmin').mockResolvedValue({
      token: 'test-admin-token',
      userId: 99,
      username: 'admin_officer',
      role: 'ADMIN',
    })

    renderWithProviders(<AdminRegister />)

    fireEvent.change(screen.getByLabelText(/special admin authorization code/i), {
      target: { value: 'CIVICEYE-ADMIN-2026-SECURE' },
    })
    fireEvent.change(screen.getByLabelText(/administrator full name/i), {
      target: { value: 'Chief Administrator' },
    })
    fireEvent.change(screen.getByLabelText(/admin username/i), {
      target: { value: 'admin_officer' },
    })
    fireEvent.change(screen.getByLabelText(/official municipal email/i), {
      target: { value: 'chief@civiceye.com' },
    })
    fireEvent.change(screen.getByLabelText(/password \(min 6 characters\)/i), {
      target: { value: 'AdminSecret@123' },
    })
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: 'AdminSecret@123' },
    })

    const submitBtn = screen.getByRole('button', { name: /authorize & create admin account/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(authService.registerAdmin).toHaveBeenCalledWith({
        fullName: 'Chief Administrator',
        username: 'admin_officer',
        email: 'chief@civiceye.com',
        adminCode: 'CIVICEYE-ADMIN-2026-SECURE',
        password: 'AdminSecret@123',
      })
    })

    expect(await screen.findByText(/administrator verified and registered/i)).toBeDefined()
  })
})
