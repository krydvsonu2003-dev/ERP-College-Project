import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV_BY_ROLE = {
  SUPER_ADMIN: [
    { section: 'Main' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'Administration' },
    { to: '/admin/users', icon: '👥', label: 'User Management' },
    { to: '/admin/roles', icon: '🛡️', label: 'Roles & Privileges' },
    { to: '/admin/audit-logs', icon: '📋', label: 'Audit Logs' },
    { section: 'Academic' },
    { to: '/admissions', icon: '📝', label: 'Admissions' },
    { to: '/attendance', icon: '✅', label: 'Attendance' },
    { to: '/exams', icon: '🎓', label: 'Examinations' },
    { to: '/exams/marks-entry', icon: '📊', label: 'Marks Entry' },
    { section: 'Finance' },
    { to: '/fees', icon: '💰', label: 'Fee Management' },
  ],
  PRINCIPAL: [
    { section: 'Overview' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'Academic' },
    { to: '/admissions', icon: '📝', label: 'Admissions' },
    { to: '/attendance', icon: '✅', label: 'Attendance' },
    { to: '/exams', icon: '🎓', label: 'Results' },
    { to: '/exams/marks-entry', icon: '📊', label: 'Marks Entry' },
    { section: 'Finance' },
    { to: '/fees', icon: '💰', label: 'Fee Overview' },
  ],
  HOD: [
    { section: 'Overview' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'Academic' },
    { to: '/admissions', icon: '📝', label: 'Admissions' },
    { to: '/attendance', icon: '✅', label: 'Attendance' },
    { to: '/exams', icon: '🎓', label: 'Examinations' },
    { to: '/exams/marks-entry', icon: '📊', label: 'Marks Entry' },
  ],
  FACULTY: [
    { section: 'Overview' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'My Work' },
    { to: '/attendance', icon: '✅', label: 'Mark Attendance' },
    { to: '/exams/marks-entry', icon: '📊', label: 'Enter Marks' },
    { to: '/exams', icon: '🎓', label: 'Results' },
    { to: '/exams/marks-entry', icon: '📊', label: 'Enter Marks' },
  ],
  STUDENT: [
    { section: 'Overview' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'My Academics' },
    { to: '/my/attendance', icon: '✅', label: 'Attendance' },
    { to: '/my/results', icon: '🎓', label: 'Results' },
    { section: 'Finance' },
    { to: '/my/fees', icon: '💳', label: 'Fee Dues' },
  ],
  ACCOUNTANT: [
    { section: 'Overview' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'Finance' },
    { to: '/fees', icon: '💰', label: 'Fee Collection' },
    { to: '/fees/reports', icon: '📊', label: 'Reports' },
  ],
  ADMISSION_OFFICE_STAFF: [
    { section: 'Overview' },
    { to: '/', icon: '🏠', label: 'Dashboard' },
    { section: 'Admissions' },
    { to: '/admissions', icon: '📝', label: 'Applications' },
    { to: '/admissions/new', icon: '➕', label: 'New Application' },
  ],
};

export default function Sidebar() {
  const { user, primaryRole } = useAuth();
  const items = NAV_BY_ROLE[primaryRole] || NAV_BY_ROLE.STUDENT;
  const initials = user?.fullName?.split(' ').map(n => n[0]).slice(0, 2).join('') || 'U';

  return (
    <aside className="sidebar">
      {/* Logo */}
      <div className="sidebar-logo">
        <div className="sidebar-logo-icon">💊</div>
        <div className="sidebar-logo-text">
          <h1>NPC ERP</h1>
          <span>Pharmacy College</span>
        </div>
      </div>

      {/* Nav */}
      <nav className="sidebar-nav">
        {items.map((item, idx) => {
          if (item.section) {
            return (
              <div key={idx} className="sidebar-section-label">{item.section}</div>
            );
          }
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
            >
              <div className="nav-icon">{item.icon}</div>
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* User */}
      <div className="sidebar-user">
        <div className="sidebar-avatar">{initials}</div>
        <div className="sidebar-user-info">
          <div className="sidebar-user-name">{user?.fullName}</div>
          <div className="sidebar-user-role">{primaryRole?.replace(/_/g, ' ')}</div>
        </div>
      </div>
    </aside>
  );
}
