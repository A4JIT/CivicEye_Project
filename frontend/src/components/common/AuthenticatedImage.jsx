import { useState, useEffect } from 'react'
import api from '../../services/api'
import getFullImageUrl from '../../utils/imageUrl'

/**
 * Image component that securely fetches images using Axios with JWT Authentication,
 * ensuring evidence photos render properly even when endpoints require authentication.
 */
const AuthenticatedImage = ({
  src,
  alt = 'Grievance evidence photo',
  className = '',
  style = {},
  allowZoom = true,
  fallbackText = 'Unable to load photo evidence',
  onClick,
}) => {
  const initialUrl = src
    ? src.startsWith('blob:') || src.startsWith('data:')
      ? src
      : getFullImageUrl(src)
    : null
  const [blobUrl, setBlobUrl] = useState(initialUrl)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(false)
  const [isZoomed, setIsZoomed] = useState(false)

  useEffect(() => {
    if (!src) {
      setBlobUrl(null)
      setLoading(false)
      return
    }

    // If it's already a local blob: or data: URL (e.g. during file upload preview)
    if (src.startsWith('blob:') || src.startsWith('data:')) {
      setBlobUrl(src)
      setLoading(false)
      return
    }

    let isMounted = true
    let createdUrl = null

    const fetchImage = async () => {
      if (!initialUrl) setLoading(true)
      setError(false)

      try {
        const fullUrl = getFullImageUrl(src)
        // Request image with JWT token via axios
        const response = await api.get(fullUrl, { responseType: 'blob' })
        if (isMounted && response.data) {
          createdUrl = URL.createObjectURL(response.data)
          setBlobUrl(createdUrl)
        }
      } catch (err) {
        console.warn('Authenticated image fetch failed, attempting direct load fallback:', err)
        if (isMounted) {
          // Fallback to direct URL if public access or proxy works
          setBlobUrl(getFullImageUrl(src))
        }
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    fetchImage()

    return () => {
      isMounted = false
      if (createdUrl && createdUrl.startsWith('blob:')) {
        URL.revokeObjectURL(createdUrl)
      }
    }
  }, [src])

  if (!src) {
    return (
      <div className="evidence-placeholder-box" style={{ ...style }}>
        <span className="placeholder-camera-icon">📷</span>
        <span>No photographic evidence submitted</span>
      </div>
    )
  }

  if (loading) {
    return (
      <div
        className={`image-loading-placeholder ${className}`}
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '120px',
          background: 'rgba(255, 255, 255, 0.03)',
          borderRadius: '8px',
          color: '#94a3b8',
          fontSize: '0.85rem',
          ...style,
        }}
      >
        <span>⏳ Loading evidence photo...</span>
      </div>
    )
  }

  if (error || !blobUrl) {
    return (
      <div
        className={`image-error-placeholder ${className}`}
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100px',
          background: 'rgba(239, 68, 68, 0.05)',
          border: '1px dashed rgba(239, 68, 68, 0.2)',
          borderRadius: '8px',
          color: '#f87171',
          fontSize: '0.85rem',
          padding: '1rem',
          ...style,
        }}
      >
        <span>📷 {fallbackText}</span>
      </div>
    )
  }

  return (
    <>
      <img
        src={blobUrl}
        alt={alt}
        className={className}
        style={{
          cursor: allowZoom ? 'zoom-in' : 'default',
          ...style,
        }}
        onError={() => setError(true)}
        onClick={(e) => {
          if (onClick) {
            onClick(e)
          } else if (allowZoom) {
            setIsZoomed(true)
          }
        }}
        title={allowZoom ? 'Click to inspect full high-resolution image' : alt}
      />

      {isZoomed && (
        <div
          className="image-lightbox-overlay"
          onClick={() => setIsZoomed(false)}
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0, 0, 0, 0.85)',
            backdropFilter: 'blur(8px)',
            zIndex: 99999,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '2rem',
            cursor: 'zoom-out',
          }}
        >
          <div style={{ position: 'relative', maxWidth: '90vw', maxHeight: '90vh' }}>
            <img
              src={blobUrl}
              alt={alt}
              style={{
                maxWidth: '100%',
                maxHeight: '90vh',
                borderRadius: '8px',
                boxShadow: '0 10px 40px rgba(0, 0, 0, 0.6)',
                objectFit: 'contain',
              }}
            />
            <button
              type="button"
              onClick={() => setIsZoomed(false)}
              style={{
                position: 'absolute',
                top: '-40px',
                right: '0',
                background: 'rgba(255, 255, 255, 0.2)',
                border: 'none',
                color: 'white',
                fontSize: '1.5rem',
                borderRadius: '50%',
                width: '36px',
                height: '36px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              ✕
            </button>
          </div>
        </div>
      )}
    </>
  )
}

export default AuthenticatedImage
