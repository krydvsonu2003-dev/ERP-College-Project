import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import axiosClient from '../../api/axiosClient';

export default function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [entity, setEntity] = useState('');

  useEffect(() => {
    axiosClient.get('/audit-logs', { params: { size:100, sort:'createdAt,desc', entityName: entity||undefined } })
      .then(res => setLogs(res.data.data?.content ?? []));
  }, [entity]);

  const entities = ['', 'User', 'Student', 'AdmissionApplication', 'AttendanceSession', 'Payment', 'Examination'];

  return (
    <Layout title="Audit Logs" subtitle="Complete record of all administrative actions">
      <div className="card">
        <div className="card-header">
          <div>
            <div className="card-title">📋 System Audit Trail</div>
            <div className="card-subtitle">{logs.length} records loaded</div>
          </div>
          <select className="form-select" style={{ width: 200 }} value={entity} onChange={e => setEntity(e.target.value)}>
            {entities.map(e => <option key={e} value={e}>{e || 'All Entities'}</option>)}
          </select>
        </div>
        <DataTable
          columns={[
            { key:'createdAt', header:'Timestamp', render: l => (
              <span style={{ fontFamily:'monospace', fontSize:12, color:'var(--gray-500)' }}>
                {new Date(l.createdAt).toLocaleString('en-IN')}
              </span>
            )},
            { key:'username', header:'User', render: l => (
              <div>
                <div style={{ fontWeight:600, fontSize:13 }}>{l.username || '—'}</div>
                <div style={{ fontSize:11, color:'var(--gray-400)' }}>{l.roleNames}</div>
              </div>
            )},
            { key:'action', header:'Action', render: l => (
              <code style={{ background:'var(--primary-50)', color:'var(--primary-700)', padding:'3px 8px', borderRadius:4, fontSize:12 }}>
                {l.action}
              </code>
            )},
            { key:'entityName', header:'Entity', render: l => (
              <span style={{ fontWeight:600, color:'var(--gray-700)' }}>{l.entityName}</span>
            )},
            { key:'entityId', header:'ID', render: l => (
              <span style={{ fontFamily:'monospace', fontSize:12 }}>#{l.entityId}</span>
            )},
            { key:'ipAddress', header:'IP', render: l => (
              <span style={{ fontFamily:'monospace', fontSize:11.5, color:'var(--gray-500)' }}>{l.ipAddress || '—'}</span>
            )},
          ]}
          rows={logs}
          emptyText="No audit logs found"
          emptyIcon="📋"
        />
      </div>
    </Layout>
  );
}
