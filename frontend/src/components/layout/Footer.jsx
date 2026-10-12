const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <p>
          &copy; {new Date().getFullYear()} <strong>CivicEye AI</strong>. AI-assisted civic issue reporting & management.
        </p>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
          Transparent classification, automated department routing, and community-driven accountability.
        </p>
      </div>
    </footer>
  )
}

export default Footer
