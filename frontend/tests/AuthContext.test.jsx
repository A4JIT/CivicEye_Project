import React from 'react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import { AuthProvider, useAuth } from '../src/context/AuthContext'
import authService from '../src/services/authService'

const TestConsumer = () => {
  const { isAuthenticated, user, token, logout } = useAuth()
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'logged-in' : 'logged-out'}</span>
      <span data-testid="username">{user?.username || 'none'}</span>
      <span data-testid="token">{token || 'none'}</span>
      <button onClick={logout}>Logout</button>
    </div>
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('provides default unauthenticated state when storage is empty', () => {
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    )

    expect(screen.getByTestId('auth-status').textContent).toBe('logged-out')
    expect(screen.getByTestId('username').textContent).toBe('none')
    expect(screen.getByTestId('token').textContent).toBe('none')
  })

  it('restores authentication state from localStorage', () => {
    localStorage.setItem('token', 'sample-test-jwt-token')
    localStorage.setItem(
      'user',
      JSON.stringify({
        userId: 1,
        username: 'citizen_john',
        email: 'john@civiceye.local',
        role: 'CITIZEN',
      })
    )

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    )

    expect(screen.getByTestId('auth-status').textContent).toBe('logged-in')
    expect(screen.getByTestId('username').textContent).toBe('citizen_john')
    expect(screen.getByTestId('token').textContent).toBe('sample-test-jwt-token')
  })

  it('clears session on logout', () => {
    localStorage.setItem('token', 'sample-test-jwt-token')
    localStorage.setItem(
      'user',
      JSON.stringify({ userId: 1, username: 'citizen_john' })
    )

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    )

    expect(screen.getByTestId('auth-status').textContent).toBe('logged-in')

    act(() => {
      screen.getByRole('button', { name: /logout/i }).click()
    })

    expect(screen.getByTestId('auth-status').textContent).toBe('logged-out')
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
  })
})
