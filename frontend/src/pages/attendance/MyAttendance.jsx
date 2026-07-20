import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import axiosClient from '../../api/axiosClient';

export default function MyAttendance() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axiosClient.get('/students/me').then(async res => {
      const studentId = res.data.data.id;
      const sum = await axiosClient.get(`/attendance/students/${studentId}/summary`);
      setSummary(sum.data.data);
    }).finally(() => setLoading(false));
  }, []);

  const pct = summary?.attendancePercentage ?? 0;
  const isGood = pct >= 75;
  const isWarning = pct >= 60 && pct < 75;

  return (
    <Layout title="My Attendance" subtitle="Your attendance record and percentage">
      {loading ? (
        <div style={{ padding: 60, textAlign: 'center', color: 'var(--gray-400)' }}>⏳ Loading...</div>
      ) : (
        <>
          {/* Alert Banner */}
          {!isGood && (
            <div style={{
              background: isWarning ? 'var(--warning-bg)' : 'var(--danger-bg)',
              border: `1px solid ${isWarning ? 'var(--warning)' : 'var(--danger)'}`,
              borderRadius: 'var(--radius-lg)',
              padding: '16px 20px',
              marginBottom: 20,
              display: 'flex', alignItems: 'center', gap: 12,
            }}>
              <span style={{ fontSize: 24 }}>{isWarning ? '⚠️' : '🚨'}</span>
              <div>
                <div style={{ fontWeight: 700, color: isWarning ? '#92400e' : '#991b1b', fontSize: 14 }}>
                  {isWarning ? 'Attendance Below Recommended Level' : 'Critical: Attendance Below 60%!'}
                </div>
                <div style={{ fontSize: 13, color: isWarning ? '#b45309' : '#b91c1c', marginTop: 2 }}>
                  Your attendance is {pct}%. Minimum 75% is required. Please attend classes regularly.
                </div>
              </div>
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: 20 }}>
            {/* Circular Progress */}
            <div className="card">
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16, padding: 32 }}>
                <div style={{ position: 'relative', width: 160, height: 160 }}>
                  <svg width="160" height="160" style={{ transform: 'rotate(-90deg)' }}>
                    <circle cx="80" cy="80" r="70" fill="none" stroke="var(--gray-200)" strokeWidth="12" />
                    <circle cx="80" cy="80" r="70" fill="none"
                      stroke={isGood ? 'var(--success)' : isWarning ? 'var(--warning)' : 'var(--danger)'}
                      strokeWidth="12"
                      strokeDasharray={`${2 * Math.PI * 70}`}
                      strokeDashoffset={`${2 * Math.PI * 70 * (1 - pct / 100)}`}
                      strokeLinecap="round"
                      style={{ transition: 'stroke-dashoffset 1s ease' }}
                    />
                  </svg>
                  <div style={{
                    position: 'absolute', inset: 0,
                    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <div style={{ fontSize: 32, fontWeight: 800, color: 'var(--gray-900)' }}>{pct}%</div>
                    <div style={{ fontSize: 12, color: 'var(--gray-500)', fontWeight: 500 }}>Attendance</div>
                  </div>
                </div>

                <div style={{
                  padding: '8px 20px',
                  borderRadius: 99,
                  background: isGood ? '#d1fae5' : isWarning ? '#fef3c7' : '#fee2e2',
                  color: isGood ? '#065f46' : isWarning ? '#92400e' : '#991b1b',
                  fontWeight: 700, fontSize: 13,
                }}>
                  {isGood ? '✅ Good Standing' : isWarning ? '⚠️ Needs Improvement' : '❌ Critical'}
                </div>

                <div style={{ fontSize: 12, color: 'var(--gray-400)', textAlign: 'center' }}>
                  Minimum 75% required to appear in examinations
                </div>
              </div>
            </div>

            {/* Stats Grid */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              {[
                { label: 'Total Sessions', value: summary?.totalSessions ?? 0, icon: '📅', color: 'blue' },
                { label: 'Present', value: summary?.present ?? 0, icon: '✅', color: 'green' },
                { label: 'Absent', value: summary?.absent ?? 0, icon: '❌', color: 'red' },
                { label: 'Late', value: summary?.late ?? 0, icon: '⏰', color: 'amber' },
                { label: 'Excused', value: summary?.excused ?? 0, icon: '📋', color: 'purple' },
              ].map(item => (
                <div key={item.label} className={`stat-card ${item.color}`}
                  style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 16 }}>
                  <div className={`stat-icon ${item.color}`} style={{ margin: 0, fontSize: 20 }}>{item.icon}</div>
                  <div>
                    <div style={{ fontSize: 12, color: 'var(--gray-500)', textTransform: 'uppercase', letterSpacing: '0.04em', fontWeight: 600 }}>
                      {item.label}
                    </div>
                    <div style={{ fontSize: 24, fontWeight: 800, color: 'var(--gray-900)' }}>{item.value}</div>
                  </div>
                  {item.label !== 'Total Sessions' && (
                    <div style={{ marginLeft: 'auto', fontSize: 13, fontWeight: 600, color: 'var(--gray-400)' }}>
                      {summary?.totalSessions > 0 ? Math.round((item.value / summary.totalSessions) * 100) : 0}%
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </Layout>
  );
}
