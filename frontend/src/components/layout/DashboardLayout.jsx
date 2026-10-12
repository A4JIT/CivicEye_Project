import Sidebar from './Sidebar'

const DashboardLayout = ({ children, title, subtitle }) => {
  return (
    <div className="dashboard-layout" data-testid="dashboard-layout">
      <Sidebar />
      <main className="dashboard-main-area">
        <header className="dashboard-header">
          <div className="dashboard-header-text">
            {title && <h1 className="dashboard-title">{title}</h1>}
            {subtitle && <p className="dashboard-subtitle">{subtitle}</p>}
          </div>
          <div className="dashboard-status-indicator">
            <span className="live-pulse"></span>
            <span className="live-text">Live System Connected</span>
          </div>
        </header>

        <section className="dashboard-body">{children}</section>
      </main>
    </div>
  )
}

export default DashboardLayout
