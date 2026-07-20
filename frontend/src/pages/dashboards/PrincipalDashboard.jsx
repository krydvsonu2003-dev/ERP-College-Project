import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import StatCard from '../../components/StatCard';
import axiosClient from '../../api/axiosClient';

export default function PrincipalDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axiosClient.get('/dashboard/principal')
      .then(res => setData(res.data.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <Layout title="Dashboard" subtitle="College overview">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 300, color: 'var(--gray-400)', fontSize: 16 }}>
        ⏳ Loading dashboard...
      </div>
    </Layout>
  );

  return (
    <Layout title="Principal Dashboard" subtitle="College-wide overview and statistics">

      {/* Stat Cards */}
      <div className="stats-grid">
        <StatCard label="Total Students" value={data?.totalStudents ?? 0} icon="👨‍🎓" color="blue" />
        <StatCard label="Active Students" value={data?.activeStudents ?? 0} icon="✅" color="green" />
        <StatCard label="Pending Admissions" value={data?.pendingAdmissions ?? 0} icon="📝" color="amber" />
        <StatCard label="Fee Invoices Due" value={data?.pendingFeeInvoices ?? 0} icon="⚠️" color="red" />
        <StatCard label="Fee Collected" value={`₹${Number(data?.totalFeeCollected ?? 0).toLocaleString('en-IN')}`} icon="💰" color="green" />
        <StatCard label="Total Fee Due" value={`₹${Number(data?.totalFeeDue ?? 0).toLocaleString('en-IN')}`} icon="💳" color="amber" />
        <StatCard label="Published Results" value={data?.publishedResultCards ?? 0} icon="📊" color="purple" />
        <StatCard label="Draft Results" value={data?.draftResultCards ?? 0} icon="📋" color="cyan" />
      </div>

      {/* Bottom Grid */}
      <div className="dashboard-grid">

        {/* Department Breakdown */}
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">🏛️ Department-wise Students</div>
              <div className="card-subtitle">Current enrollment by department</div>
            </div>
          </div>
          <div className="card-body">
            {data?.studentsByDepartment && Object.entries(data.studentsByDepartment).length > 0
              ? Object.entries(data.studentsByDepartment).map(([dept, count]) => {
                  const total = data.totalStudents || 1;
                  const pct = Math.round((count / total) * 100);
                  return (
                    <div key={dept} style={{ marginBottom: 16 }}>
                      <div className="flex justify-between items-center mb-1">
                        <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--gray-700)' }}>{dept}</span>
                        <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--primary-600)' }}>{count} ({pct}%)</span>
                      </div>
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: `${pct}%` }} />
                      </div>
                    </div>
                  );
                })
              : <div className="empty-state" style={{ padding: '32px 0' }}>
                  <span className="empty-state-emoji">🏛️</span>
                  <div className="empty-state-title">No departments yet</div>
                  <div className="empty-state-desc">Add students to see department breakdown</div>
                </div>
            }
          </div>
        </div>

        {/* Quick Stats Donut */}
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">📈 Student Status Overview</div>
              <div className="card-subtitle">Active vs inactive breakdown</div>
            </div>
          </div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20 }}>
            <div className="donut-chart">
              <div className="donut-center">
                <div className="donut-value">{data?.totalStudents ?? 0}</div>
                <div className="donut-label">Total</div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', justifyContent: 'center' }}>
              {[
                { label: 'Active', color: 'var(--success)', val: data?.activeStudents ?? 0 },
                { label: 'Pending', color: 'var(--warning)', val: data?.pendingAdmissions ?? 0 },
                { label: 'Fee Due', color: 'var(--danger)', val: data?.pendingFeeInvoices ?? 0 },
              ].map(item => (
                <div key={item.label} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12 }}>
                  <div style={{ width: 10, height: 10, borderRadius: '50%', background: item.color }} />
                  <span style={{ color: 'var(--gray-600)' }}>{item.label}: <strong>{item.val}</strong></span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Fee Summary */}
        <div className="card span-2">
          <div className="card-header">
            <div>
              <div className="card-title">💰 Fee Collection Summary</div>
              <div className="card-subtitle">Collected vs outstanding amounts</div>
            </div>
          </div>
          <div className="card-body">
            {(() => {
              const collected = Number(data?.totalFeeCollected ?? 0);
              const due = Number(data?.totalFeeDue ?? 0);
              const total = collected + due || 1;
              const collPct = Math.round((collected / total) * 100);
              const duePct = Math.round((due / total) * 100);
              return (
                <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
                  <div style={{ flex: 1, minWidth: 200 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <span style={{ fontSize: 13, color: 'var(--gray-600)', fontWeight: 500 }}>💚 Collected</span>
                      <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--success)' }}>₹{collected.toLocaleString('en-IN')}</span>
                    </div>
                    <div className="progress-bar">
                      <div className="progress-fill green" style={{ width: `${collPct}%` }} />
                    </div>
                    <div style={{ fontSize: 11.5, color: 'var(--gray-400)', marginTop: 4 }}>{collPct}% of total</div>
                  </div>
                  <div style={{ flex: 1, minWidth: 200 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <span style={{ fontSize: 13, color: 'var(--gray-600)', fontWeight: 500 }}>⚠️ Outstanding</span>
                      <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--warning)' }}>₹{due.toLocaleString('en-IN')}</span>
                    </div>
                    <div className="progress-bar">
                      <div className="progress-fill amber" style={{ width: `${duePct}%` }} />
                    </div>
                    <div style={{ fontSize: 11.5, color: 'var(--gray-400)', marginTop: 4 }}>{duePct}% outstanding</div>
                  </div>
                </div>
              );
            })()}
          </div>
        </div>
      </div>
    </Layout>
  );
}
