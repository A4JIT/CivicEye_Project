/**
 * Utility to resolve full image URLs from backend relative paths
 * @param {string} url - e.g. "/api/files/xyz.jpg"
 * @returns {string|null}
 */
export const getFullImageUrl = (url) => {
  if (!url) return null
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  const apiBase = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  return `${apiBase}${url.startsWith('/') ? url : `/${url}`}`
}

export default getFullImageUrl
