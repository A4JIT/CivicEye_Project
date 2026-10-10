import React from 'react'
import { Routes, Route } from 'react-router-dom'

const Home = () => <div><h1>CivicEye AI</h1><p>Welcome to the civic issue reporting platform.</p></div>
const NotFound = () => <div><h1>404 Not Found</h1></div>

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRoutes
