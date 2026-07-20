import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Topbar({ title, subtitle }) {
  const { user, primaryRole, logout } = useAuth();
  const navigate = useNavigate();
  const initials = user?.fullName?.split(' ').map(n => n[0]).slice(0, 2).join('') || 'U';

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="topbar">
      <div className="topbar-left">
        <div className="topbar-breadcrumb">
          <span>NPC ERP</span>
          <span style={{ color: 'var(--gray-300)' }}>›</span>
          <span className="current">{title}</span>
        </div>
      </div>

      <div className="topbar-right">
        <div className="topbar-user" onClick={handleLogout} title="Logout">
          <div className="topbar-avatar">{initials}</div>
          <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--gray-700)' }}>
            {user?.fullName?.split(' ')[0]}
          </span>
          <span style={{ fontSize: 11, background: 'var(--primary-100)', color: 'var(--primary-700)', padding: '2px 8px', borderRadius: 99, fontWeight: 600 }}>
            {primaryRole?.replace(/_/g, ' ')}
          </span>
          <span style={{ fontSize: 16 }}>⏻</span>
        </div>
      </div>
    </div>
  );
}
