import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthContext } from '../src/context/AuthContext'
import DepartmentStaffManagement from '../src/pages/admin/DepartmentStaffManagement'
import adminService from '../src/services/adminService'

const mockAdminUser = {
  id: 1,
  username: 'admin',
  fullName: 'Civic Administrator',
  email: 'admin@civiceye.com',
  role: 'ADMIN',
}

const renderWithProviders = (ui) => {
  return render(
    <AuthContext.Provider value={{ user: mockAdminUser, logout: vi.fn() }}>
      <BrowserRouter>{ui}</BrowserRouter>
    </AuthContext.Provider>
  )
}

describe('DepartmentStaffManagement Component', () => {
  const sampleStaff = [
    {
      id: 10,
      username: 'officer_sharma',
      fullName: 'Rajesh Sharma',
      email: 'sharma@roads.gov.in',
      role: 'DEPARTMENT_USER',
      departmentId: 1,
      departmentName: 'Public Works & Roads',
      departmentCode: 'ROAD_WORKS',
      isActive: true,
      createdAt: '2026-10-15T10:00:00Z',
    },
    {
      id: 11,
      username: 'officer_verma',
      fullName: 'Anita Verma',
      email: 'verma@water.gov.in',
      role: 'DEPARTMENT_USER',
      departmentId: 3,
      departmentName: 'Water Supply & Sewerage',
      departmentCode: 'WATER_SUPPLY',
      isActive: false,
      createdAt: '2026-10-16T11:00:00Z',
    },
  ]

  const sampleDepts = [
    { id: 1, name: 'Public Works & Roads', code: 'ROAD_WORKS' },
    { id: 3, name: 'Water Supply & Sewerage', code: 'WATER_SUPPLY' },
  ]

  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(adminService, 'getDepartmentStaff').mockResolvedValue([...sampleStaff])
    vi.spyOn(adminService, 'getDepartments').mockResolvedValue([...sampleDepts])
    vi.spyOn(adminService, 'createDepartmentStaff').mockImplementation((data) =>
      Promise.resolve({
        id: 99,
        username: data.username,
        fullName: data.fullName,
        email: data.email,
        role: 'DEPARTMENT_USER',
        departmentId: data.departmentId,
        departmentName: 'Public Works & Roads',
        departmentCode: 'ROAD_WORKS',
        isActive: true,
        createdAt: new Date().toISOString(),
      })
    )
    vi.spyOn(adminService, 'toggleStaffStatus').mockImplementation((id) =>
      Promise.resolve({
        id,
        isActive: false,
      })
    )
  })

  it('renders Department Staff Management directory with staff table and stats', async () => {
    renderWithProviders(<DepartmentStaffManagement />)

    expect(await screen.findByText(/Municipal Department Staff Management/i)).toBeDefined()
    expect(screen.getByTestId('staff-search-input')).toBeDefined()
    expect(screen.getByTestId('staff-dept-filter')).toBeDefined()
    expect(screen.getByTestId('btn-add-staff')).toBeDefined()

    expect(await screen.findByTestId('staff-row-10')).toBeDefined()
    expect(screen.getByTestId('staff-row-11')).toBeDefined()
    expect(screen.getByText('Rajesh Sharma')).toBeDefined()
    expect(screen.getByText('Anita Verma')).toBeDefined()
  })

  it('filters staff directory by search input', async () => {
    renderWithProviders(<DepartmentStaffManagement />)

    expect(await screen.findByTestId('staff-row-10')).toBeDefined()
    const searchInput = screen.getByTestId('staff-search-input')
    fireEvent.change(searchInput, { target: { value: 'Anita' } })

    expect(screen.queryByTestId('staff-row-10')).toBeNull()
    expect(screen.getByTestId('staff-row-11')).toBeDefined()
  })

  it('opens modal and creates a new department staff officer', async () => {
    renderWithProviders(<DepartmentStaffManagement />)

    const addBtn = await screen.findByTestId('btn-add-staff')
    fireEvent.click(addBtn)

    expect(screen.getByText(/Create Department Staff Account/i)).toBeDefined()

    fireEvent.change(screen.getByLabelText(/officer full name/i), {
      target: { value: 'Sunil Patil' },
    })
    fireEvent.change(screen.getByLabelText(/officer username/i), {
      target: { value: 'officer_patil' },
    })
    fireEvent.change(screen.getByLabelText(/official email address/i), {
      target: { value: 'patil@roads.gov.in' },
    })
    fireEvent.change(screen.getByLabelText(/initial password/i), {
      target: { value: 'StaffSecret@123' },
    })
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: 'StaffSecret@123' },
    })

    const submitBtn = screen.getByTestId('btn-submit-create-staff')
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(adminService.createDepartmentStaff).toHaveBeenCalledWith({
        fullName: 'Sunil Patil',
        username: 'officer_patil',
        email: 'patil@roads.gov.in',
        password: 'StaffSecret@123',
        departmentId: 1,
      })
    })

    expect(await screen.findByText(/created successfully/i)).toBeDefined()
  })

  it('toggles department staff active status', async () => {
    renderWithProviders(<DepartmentStaffManagement />)

    const toggleBtn = await screen.findByTestId('btn-toggle-status-10')
    fireEvent.click(toggleBtn)

    await waitFor(() => {
      expect(adminService.toggleStaffStatus).toHaveBeenCalledWith(10)
    })
  })
})
