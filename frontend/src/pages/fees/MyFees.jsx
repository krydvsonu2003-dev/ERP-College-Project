import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import Badge from '../../components/Badge';
import axiosClient from '../../api/axiosClient';

export default function MyFees() {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axiosClient.get('/students/me').then(async res => {
      const studentId = res.data.data.id;
      const inv = await axiosClient.get(`/fees/students/${studentId}/invoices`);
      setInvoices(inv.data.data ?? []);
    }).finally(() => setLoading(false));
  }, []);

  const totalDue = invoices.reduce((sum, i) => sum + Number(i.dueAmount || 0), 0);
  const totalPaid = invoices.reduce((sum, i) => sum + Number(i.paidAmount || 0), 0);

  return (
    <Layout title="My Fee Dues" subtitle="Your semester-wise fee details">

      {/* Summary Cards */}
      <div className="stats-grid" style={{ marginBottom: 24 }}>
        <div className="stat-card green">
          <div className="stat-icon green">✅</div>
          <div className="stat-value">₹{totalPaid.toLocaleString('en-IN')}</div>
          <div className="stat-label">Total Paid</div>
        </div>
        <div className="stat-card amber">
          <div className="stat-icon amber">⚠️</div>
          <div className="stat-value">₹{totalDue.toLocaleString('en-IN')}</div>
          <div className="stat-label">Total Outstanding</div>
        </div>
        <div className="stat-card blue">
          <div className="stat-icon blue">🧾</div>
          <div className="stat-value">{invoices.length}</div>
          <div className="stat-label">Total Invoices</div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <div>
            <div className="card-title">💳 Fee Invoice History</div>
            <div className="card-subtitle">Semester-wise breakdown of your fees</div>
          </div>
        </div>

        {loading ? (
          <div style={{ padding: 60, textAlign: 'center', color: 'var(--gray-400)' }}>⏳ Loading your fees...</div>
        ) : (
          <DataTable
            columns={[
              { key: 'semester', header: 'Semester', render: i => (
                <span style={{
                  background: 'var(--primary-50)', color: 'var(--primary-700)',
                  padding: '4px 12px', borderRadius: 99, fontWeight: 700, fontSize: 13,
                }}>
                  Semester {i.semester}
                </span>
              )},
              { key: 'totalAmount', header: 'Total Fee', render: i => (
                <span style={{ fontWeight: 700, color: 'var(--gray-800)' }}>
                  ₹{Number(i.totalAmount).toLocaleString('en-IN')}
                </span>
              )},
              { key: 'paidAmount', header: 'Paid', render: i => (
                <span style={{ fontWeight: 700, color: 'var(--success)' }}>
                  ₹{Number(i.paidAmount).toLocaleString('en-IN')}
                </span>
              )},
              { key: 'dueAmount', header: 'Due', render: i => (
                <span style={{ fontWeight: 700, color: Number(i.dueAmount) > 0 ? 'var(--danger)' : 'var(--success)' }}>
                  ₹{Number(i.dueAmount).toLocaleString('en-IN')}
                </span>
              )},
              { key: 'status', header: 'Status', render: i => <Badge value={i.status} /> },
              { key: 'dueDate', header: 'Due Date', render: i => (
                <span style={{ fontSize: 12.5, color: 'var(--gray-500)' }}>{i.dueDate || '—'}</span>
              )},
              { key: 'progress', header: 'Progress', render: i => {
                const total = Number(i.totalAmount) || 1;
                const paid = Number(i.paidAmount) || 0;
                const pct = Math.round((paid / total) * 100);
                return (
                  <div style={{ width: 100 }}>
                    <div className="progress-bar">
                      <div className="progress-fill green" style={{ width: `${pct}%` }} />
                    </div>
                    <div style={{ fontSize: 11, color: 'var(--gray-400)', marginTop: 2 }}>{pct}% paid</div>
                  </div>
                );
              }},
            ]}
            rows={invoices}
            emptyText="No fee invoices found"
            emptyIcon="💳"
          />
        )}
      </div>
    </Layout>
  );
}
