import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import Badge from '../../components/Badge';
import axiosClient from '../../api/axiosClient';

const STATUSES = ['', 'SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','STUDENT_CREATED'];

export default function AdmissionList() {
  const [applications, setApplications] = useState([]);
  const [status, setStatus] = useState('');
  const [counts, setCounts] = useState({});

  const load = () =>
    axiosClient.get('/admissions', { params: { status: status||undefined, size:100 } })
      .then(res => setApplications(res.data.data?.content ?? []));
const handleDelete = async (id) => {

  const ok = window.confirm("Are you sure you want to delete this admission?");

  if (!ok) return;

  try {

    await axiosClient.delete(`/admissions/${id}`);

    alert("Admission deleted successfully.");

    load();

  } catch (err) {
    console.error(err);
    alert("Delete failed.");
  }
};
  useEffect(() => { load(); }, [status]);

  const statusIcons = { SUBMITTED:'📝', UNDER_REVIEW:'🔍', APPROVED:'✅', REJECTED:'❌', STUDENT_CREATED:'🎓' };
  const statusColors = { SUBMITTED:'var(--warning)', UNDER_REVIEW:'var(--info)', APPROVED:'var(--success)', REJECTED:'var(--danger)', STUDENT_CREATED:'var(--success)' };

  return (
    <Layout
      title="Admissions"
      subtitle="Manage student admission applications"
      actions={<Link to="/admissions/new" className="btn btn-primary">+ New Application</Link>}
    >

      {/* Status Filter Pills */}
      <div style={{ display:'flex', gap:8, flexWrap:'wrap', marginBottom:24 }}>
        {STATUSES.map(s => (
          <button key={s}
            onClick={() => setStatus(s)}
            style={{
              padding:'7px 16px',
              borderRadius:99,
              border:`1.5px solid ${status===s ? 'var(--primary-500)' : 'var(--gray-300)'}`,
              background: status===s ? 'var(--primary-500)' : '#fff',
              color: status===s ? '#fff' : 'var(--gray-600)',
              fontWeight:600, fontSize:13, cursor:'pointer',
              transition:'all 0.15s',
            }}>
            {s ? `${statusIcons[s]} ${s.replace(/_/g,' ')}` : '📋 All Applications'}
          </button>
        ))}
      </div>

      <div className="card">
        <div className="card-header">
          <div>
            <div className="card-title">📝 Applications</div>
            <div className="card-subtitle">{applications.length} {status ? status.replace(/_/g,' ') : 'total'} applications</div>
          </div>
        </div>
        <DataTable
          columns={[
            { key:'admissionRefNo', header:'Ref. No.', render: a => (
              <code style={{ fontFamily:'monospace', fontWeight:700, fontSize:12.5, color:'var(--primary-600)' }}>
                {a.admissionRefNo}
              </code>
            )},
            { key:'fullName', header:'Applicant', render: a => (
              <div>
                <div style={{ fontWeight:600 }}>{a.fullName}</div>
                <div style={{ fontSize:12, color:'var(--gray-400)' }}>{a.mobileNumber}</div>
              </div>
            )},
            { key:'courseName', header:'Course', render: a => (
              <span style={{ background:'var(--primary-50)', color:'var(--primary-700)', padding:'3px 10px', borderRadius:99, fontSize:12, fontWeight:600 }}>
                {a.courseName}
              </span>
            )},
            { key:'createdAt', header:'Applied On', render: a => (
              <span style={{ fontSize:12.5, color:'var(--gray-500)' }}>
                {a.createdAt ? new Date(a.createdAt).toLocaleDateString('en-IN') : '—'}
              </span>
            )},
            { key:'status', header:'Status', render: a => <Badge value={a.status} /> },
           {
  key: 'actions',
  header: '',
  render: a => (
    <div style={{ display: 'flex', gap: '8px' }}>

      <Link
        to={`/admissions/${a.id}`}
        className="btn btn-secondary btn-sm"
      >
        View Details ›
      </Link>

      <button
        className="btn btn-danger btn-sm"
        onClick={() => handleDelete(a.id)}
      >
        Delete
      </button>

    </div>
  )
},
          ]}
          rows={applications}
          emptyText="No applications found"
          emptyIcon="📝"
        />
      </div>
    </Layout>
  );
}
