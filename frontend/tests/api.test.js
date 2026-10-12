import { describe, it, expect, beforeEach } from 'vitest'
import api from '../src/services/api'

describe('API Axios Interceptor', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('has base configuration set', () => {
    expect(api.defaults.headers['Content-Type']).toBe('application/json')
    expect(api.defaults.baseURL).toBeDefined()
  })

  it('adds Authorization Bearer header when token exists in storage', async () => {
    localStorage.setItem('token', 'sample-jwt-bearer-token')

    // Test the request interceptor handler directly
    const interceptorHandler = api.interceptors.request.handlers[0].fulfilled
    const config = { headers: {} }
    const updatedConfig = interceptorHandler(config)

    expect(updatedConfig.headers.Authorization).toBe('Bearer sample-jwt-bearer-token')
  })

  it('does not add Authorization header if no token is present', async () => {
    const interceptorHandler = api.interceptors.request.handlers[0].fulfilled
    const config = { headers: {} }
    const updatedConfig = interceptorHandler(config)

    expect(updatedConfig.headers.Authorization).toBeUndefined()
  })
})
