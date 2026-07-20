import Sidebar from './Sidebar';
import Topbar from './Topbar';

export default function Layout({ title, subtitle, children, actions }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main-area">
        <Topbar title={title} subtitle={subtitle} />
        <div className="page-content page-fade">
          {(title || actions) && (
            <div className="page-header">
              <div>
                <h1 className="page-title">{title}</h1>
                {subtitle && <p className="page-subtitle">{subtitle}</p>}
              </div>
              {actions && <div className="flex gap-2">{actions}</div>}
            </div>
          )}
          {children}
        </div>
      </div>
    </div>
  );
}
