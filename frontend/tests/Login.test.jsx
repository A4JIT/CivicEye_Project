import React from 'react'
import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext'
import Login from '../src/pages/public/Login'

const renderWithProviders = (ui) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  )
}

describe('Login Component', () => {
  it('renders login form with title, fields, and submit button', () => {
    renderWithProviders(<Login />)

    expect(screen.getByRole('heading', { name: /welcome back/i })).toBeDefined()
    expect(screen.getByLabelText(/email or username/i)).toBeDefined()
    expect(screen.getByLabelText(/^password/i, { selector: 'input' })).toBeDefined()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeDefined()
  })

  it('shows validation errors when submitted with empty fields', async () => {
    renderWithProviders(<Login />)

    const submitBtn = screen.getByRole('button', { name: /sign in/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(screen.getByText(/email or username is required/i)).toBeDefined()
      expect(screen.getByText(/password is required/i)).toBeDefined()
    })
  })

  it('populates fields when demo credentials button is clicked', () => {
    renderWithProviders(<Login />)

    const citizenDemoBtn = screen.getByRole('button', { name: /citizen demo/i })
    fireEvent.click(citizenDemoBtn)

    const usernameInput = screen.getByLabelText(/email or username/i)
    const passwordInput = screen.getByLabelText(/^password/i, { selector: 'input' })

    expect(usernameInput.value).toBe('citizen@civiceye.com')
    expect(passwordInput.value).toBe('Password@123')
  })

  it('provides navigation link to registration page', () => {
    renderWithProviders(<Login />)

    const registerLink = screen.getByRole('link', { name: /create an account/i })
    expect(registerLink.getAttribute('href')).toBe('/register')
  })
})
