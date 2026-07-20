import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import StatCard from '../../components/StatCard';
import axiosClient from '../../api/axiosClient';
import { Link } from 'react-router-dom';

export default function AccountantDashboard() {
  const [data, setData] = useState(null);

  useEffect(() => {
    axiosClient.get('/dashboard/accountant').then(res => setData(res.data.data));
  }, []);

  return (
    <Layout
      title="Accountant Dashboard"
      subtitle="Fee collection and financial overview"
      actions={<Link to="/fees" className="btn btn-primary">💰 Collect Payment</Link>}
    >
      <div className="stats-grid">
        <StatCard label="Today's Collection" value={`₹${Number(data?.todaysCollection ?? 0).toLocaleString('en-IN')}`} icon="💰" color="green" />
        <StatCard label="Pending Due Invoices" value={data?.pendingDueInvoices ?? 0} icon="⚠️" color="amber" />
        <StatCard label="Total Receipts Generated" value={data?.receiptsGeneratedTotal ?? 0} icon="🧾" color="blue" />
      </div>

      <div className="dashboard-grid">
        <div className="card">
          <div className="card-header"><div className="card-title">⚡ Quick Actions</div></div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {[
              { to: '/fees', icon: '💰', label: 'Collect Payment' },
              { to: '/fees', icon: '🧾', label: 'Generate Invoice' },
              { to: '/fees', icon: '📋', label: 'View Due List' },
              { to: '/fees/reports', icon: '📊', label: 'View Reports' },
            ].map(item => (
              <Link key={item.label} to={item.to} style={{
                display: 'flex', alignItems: 'center', gap: 12,
                padding: '12px 16px',
                background: 'var(--gray-50)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--gray-200)',
                color: 'var(--gray-700)',
                fontWeight: 600, fontSize: 13.5,
                textDecoration: 'none',
              }}>
                <span style={{ fontSize: 20 }}>{item.icon}</span>
                {item.label}
                <span style={{ marginLeft: 'auto', color: 'var(--gray-400)' }}>›</span>
              </Link>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-header"><div className="card-title">📊 Collection Stats</div></div>
          <div className="card-body">
            {[
              { label: "Today's Collection", value: `₹${Number(data?.todaysCollection ?? 0).toLocaleString('en-IN')}`, color: 'var(--success)' },
              { label: 'Pending Invoices', value: data?.pendingDueInvoices ?? 0, color: 'var(--warning)' },
              { label: 'Receipts Generated', value: data?.receiptsGeneratedTotal ?? 0, color: 'var(--primary-500)' },
            ].map(item => (
              <div key={item.label} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '14px 0', borderBottom: '1px solid var(--gray-100)',
              }}>
                <span style={{ fontSize: 13.5, color: 'var(--gray-600)' }}>{item.label}</span>
                <span style={{ fontSize: 16, fontWeight: 800, color: item.color }}>{item.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </Layout>
  );
}
