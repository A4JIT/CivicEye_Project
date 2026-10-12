import React from 'react'
import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext'
import Register from '../src/pages/public/Register'

const renderWithProviders = (ui) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  )
}

describe('Register Component', () => {
  it('renders registration form with all required fields', () => {
    renderWithProviders(<Register />)

    expect(screen.getByRole('heading', { name: /create an account/i })).toBeDefined()
    expect(screen.getByLabelText(/full name/i)).toBeDefined()
    expect(screen.getByLabelText(/username/i)).toBeDefined()
    expect(screen.getByLabelText(/email address/i)).toBeDefined()
    expect(screen.getByLabelText(/account role/i)).toBeDefined()
    expect(screen.getByLabelText(/password \(min 6 characters\)/i)).toBeDefined()
    expect(screen.getByLabelText(/confirm password/i)).toBeDefined()
  })

  it('validates empty inputs on form submission', async () => {
    renderWithProviders(<Register />)

    const submitBtn = screen.getByRole('button', { name: /create account/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(screen.getByText(/full name is required/i)).toBeDefined()
      expect(screen.getByText(/username is required/i)).toBeDefined()
      expect(screen.getByText(/email is required/i)).toBeDefined()
      expect(screen.getByText(/password is required/i)).toBeDefined()
      expect(screen.getByText(/confirm your password/i)).toBeDefined()
    })
  })

  it('validates that passwords must match', async () => {
    renderWithProviders(<Register />)

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'Jane Doe' } })
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'janedoe' } })
    fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'jane@example.com' } })
    fireEvent.change(screen.getByLabelText(/password \(min 6 characters\)/i), { target: { value: 'Secret123' } })
    fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'Different123' } })

    const submitBtn = screen.getByRole('button', { name: /create account/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(screen.getByText(/passwords do not match/i)).toBeDefined()
    })
  })

  it('has navigation link to sign-in page', () => {
    renderWithProviders(<Register />)

    const loginLink = screen.getByRole('link', { name: /sign in here/i })
    expect(loginLink.getAttribute('href')).toBe('/login')
  })
})
