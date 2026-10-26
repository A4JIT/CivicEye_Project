import '@testing-library/react'

if (typeof window !== 'undefined') {
  window.URL.createObjectURL = window.URL.createObjectURL || (() => 'blob:mock-url')
  window.URL.revokeObjectURL = window.URL.revokeObjectURL || (() => {})
}
