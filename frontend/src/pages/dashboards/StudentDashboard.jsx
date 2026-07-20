import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import StatCard from '../../components/StatCard';
import axiosClient from '../../api/axiosClient';
import { Link } from 'react-router-dom';

export default function StudentDashboard() {
  const [data, setData] = useState(null);
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    axiosClient.get('/dashboard/student').then(res => setData(res.data.data));
    axiosClient.get('/students/me').then(res => setProfile(res.data.data));
  }, []);

  const attPct = data?.attendancePercentage ?? 0;
  const attColor = attPct >= 75 ? 'green' : attPct >= 60 ? 'amber' : 'red';

  return (
    <Layout title="My Dashboard" subtitle="Your academic overview">

      {/* Profile Banner */}
      {profile && (
        <div style={{
          background: 'linear-gradient(135deg, var(--primary-700), var(--primary-500))',
          borderRadius: 'var(--radius-lg)',
          padding: '24px',
          marginBottom: 24,
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          gap: 20,
          boxShadow: 'var(--shadow-lg)',
        }}>
          <div style={{
            width: 64, height: 64, borderRadius: '50%',
            background: 'rgba(255,255,255,0.2)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 28, fontWeight: 800, color: '#fff',
            border: '3px solid rgba(255,255,255,0.3)',
            flexShrink: 0,
          }}>
            {profile.fullName?.charAt(0)}
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 20, fontWeight: 800, marginBottom: 4 }}>{profile.fullName}</div>
            <div style={{ opacity: 0.8, fontSize: 13, display: 'flex', gap: 20, flexWrap: 'wrap' }}>
              <span>🎓 {profile.courseName}</span>
              <span>📋 {profile.studentCode}</span>
              <span>📚 Semester {profile.currentSemester}</span>
              <span>🏫 {profile.departmentName}</span>
            </div>
          </div>
          <div style={{
            background: 'rgba(255,255,255,0.15)',
            borderRadius: 'var(--radius-md)',
            padding: '10px 18px',
            textAlign: 'center',
          }}>
            <div style={{ fontSize: 11, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Status</div>
            <div style={{ fontSize: 16, fontWeight: 700 }}>{profile.status}</div>
          </div>
        </div>
      )}

      <div className="stats-grid">
        <StatCard label="Attendance" value={`${attPct}%`} icon="✅" color={attColor} />
        <StatCard label="Published Results" value={data?.publishedResultCount ?? 0} icon="📊" color="blue" />
        <StatCard label="Fee Due" value={`₹${Number(data?.totalDueAmount ?? 0).toLocaleString('en-IN')}`} icon="💳" color="amber" />
        <StatCard label="Profile Complete" value={`${data?.profileCompletenessPercent ?? 0}%`} icon="👤" color="purple" />
      </div>

      {/* Quick Links */}
      <div className="dashboard-grid">
        <div className="card">
          <div className="card-header"><div className="card-title">⚡ Quick Actions</div></div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {[
              { to: '/my/attendance', icon: '✅', label: 'View My Attendance', color: 'var(--success)' },
              { to: '/my/results', icon: '📊', label: 'View My Results', color: 'var(--primary-500)' },
              { to: '/my/fees', icon: '💳', label: 'View Fee Dues', color: 'var(--warning)' },
            ].map(item => (
              <Link key={item.to} to={item.to} style={{
                display: 'flex', alignItems: 'center', gap: 12,
                padding: '12px 16px',
                background: 'var(--gray-50)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--gray-200)',
                color: 'var(--gray-700)',
                fontWeight: 600, fontSize: 13.5,
                transition: 'all 0.15s ease',
                textDecoration: 'none',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--primary-50)'}
              onMouseLeave={e => e.currentTarget.style.background = 'var(--gray-50)'}
              >
                <span style={{ fontSize: 20 }}>{item.icon}</span>
                {item.label}
                <span style={{ marginLeft: 'auto', color: 'var(--gray-400)' }}>›</span>
              </Link>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-header"><div className="card-title">📊 Attendance Overview</div></div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16 }}>
            <div style={{
              width: 120, height: 120,
              borderRadius: '50%',
              background: `conic-gradient(${attPct >= 75 ? 'var(--success)' : attPct >= 60 ? 'var(--warning)' : 'var(--danger)'} ${attPct * 3.6}deg, var(--gray-200) 0deg)`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              position: 'relative',
              boxShadow: 'var(--shadow-md)',
            }}>
              <div style={{
                width: 84, height: 84,
                borderRadius: '50%',
                background: '#fff',
                display: 'flex', flexDirection: 'column',
                alignItems: 'center', justifyContent: 'center',
              }}>
                <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--gray-900)' }}>{attPct}%</div>
                <div style={{ fontSize: 10, color: 'var(--gray-500)' }}>Attendance</div>
              </div>
            </div>
            <div style={{ fontSize: 13, color: attPct >= 75 ? 'var(--success)' : 'var(--danger)', fontWeight: 600, textAlign: 'center' }}>
              {attPct >= 75 ? '✅ Good Standing' : attPct >= 60 ? '⚠️ Below Recommended' : '❌ Critical — Below 60%'}
            </div>
            <div style={{ fontSize: 12, color: 'var(--gray-500)', textAlign: 'center' }}>
              Minimum 75% attendance required
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
