import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import Badge from '../../components/Badge';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';

export default function AdmissionDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPrivilege } = useAuth();
  const [app, setApp] = useState(null);
  const [history, setHistory] = useState([]);
  const [rejectReason, setRejectReason] = useState('');
  const [showReject, setShowReject] = useState(false);
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);

  const load = () => {
    axiosClient.get(`/admissions/${id}`).then(r => setApp(r.data.data));
    axiosClient.get(`/admissions/${id}/history`).then(r => setHistory(r.data.data ?? []));
  };
  useEffect(load, [id]);

  const markUnderReview = async () => {
    await axiosClient.patch(`/admissions/${id}/under-review`, { remarks: 'Document verification started' });
    setToast({ type: 'success', message: 'Application moved to Under Review' });
    load();
  };

  const approve = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.post(`/admissions/${id}/approve`);
      setToast({ type: 'success', message: '🎉 Application approved! Student record created.' });
      setTimeout(() => navigate(`/students/${res.data.data.id}`), 1500);
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to approve' });
    } finally { setLoading(false); }
  };

  const reject = async () => {
    if (!rejectReason.trim()) return setToast({ type: 'error', message: 'Rejection remarks are required' });
    try {
      await axiosClient.post(`/admissions/${id}/reject`, { remarks: rejectReason });
      setToast({ type: 'success', message: 'Application rejected' });
      setShowReject(false);
      load();
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to reject' });
    }
  };

  if (!app) return (
    <Layout title="Admission Detail">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 300, color: 'var(--gray-400)' }}>
        ⏳ Loading application...
      </div>
    </Layout>
  );

  const statusSteps = [
    { key: 'SUBMITTED', label: 'Submitted', icon: '📝' },
    { key: 'UNDER_REVIEW', label: 'Under Review', icon: '🔍' },
    { key: 'APPROVED', label: 'Approved', icon: '✅' },
    { key: 'STUDENT_CREATED', label: 'Enrolled', icon: '🎓' },
  ];
  const statusOrder = ['SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'STUDENT_CREATED'];
  const currentIdx = app.status === 'REJECTED' ? -1 : statusOrder.indexOf(app.status);

  return (
    <Layout
      title={`Application — ${app.admissionRefNo}`}
      subtitle={`${app.courseName} · ${app.fullName}`}
      actions={<Link to="/admissions" className="btn btn-secondary">← Back to Admissions</Link>}
    >

      {/* Status Tracker */}
      {app.status !== 'REJECTED' ? (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-body">
            <div style={{ display: 'flex', position: 'relative' }}>
              <div style={{
                position: 'absolute', top: 18, left: '10%', right: '10%',
                height: 2, background: 'var(--gray-200)', zIndex: 0,
              }} />
              {statusSteps.map((st, idx) => {
                const done = idx <= currentIdx;
                const active = idx === currentIdx;
                return (
                  <div key={st.key} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative', zIndex: 1 }}>
                    <div style={{
                      width: 40, height: 40, borderRadius: '50%',
                      background: done ? 'var(--success)' : '#fff',
                      border: `2px solid ${done ? 'var(--success)' : 'var(--gray-300)'}`,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: 18,
                      boxShadow: active ? '0 0 0 4px rgba(16,185,129,0.15)' : 'none',
                      transition: 'all 0.3s',
                    }}>
                      {done ? '✓' : st.icon}
                    </div>
                    <div style={{ fontSize: 12, marginTop: 6, fontWeight: active ? 700 : 500,
                      color: done ? 'var(--success)' : 'var(--gray-400)' }}>
                      {st.label}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      ) : (
        <div style={{
          background: 'var(--danger-bg)', border: '1px solid var(--danger)',
          borderRadius: 'var(--radius-lg)', padding: '16px 20px', marginBottom: 20,
          display: 'flex', alignItems: 'center', gap: 12,
        }}>
          <span style={{ fontSize: 24 }}>❌</span>
          <div>
            <div style={{ fontWeight: 700, color: '#991b1b' }}>Application Rejected</div>
            <div style={{ fontSize: 13, color: '#b91c1c', marginTop: 2 }}>
              Reason: {app.rejectionRemarks}
            </div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        {/* Applicant Details */}
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">👤 Applicant Details</div>
            </div>
            <Badge value={app.status} />
          </div>
          <div className="card-body">
            {[
              ['Full Name', app.fullName],
              ['Gender', app.gender],
              ['Date of Birth', app.dateOfBirth],
              ['Mobile', app.mobileNumber],
              ['Email', app.email || '—'],
              ['Category', app.category || '—'],
              ['Course Applied', app.courseName],
              ['Entry Semester', `Semester ${app.entrySemester}`],
            ].map(([label, val]) => (
              <div key={label} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '10px 0', borderBottom: '1px solid var(--gray-100)',
              }}>
                <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>{label}</span>
                <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--gray-800)' }}>{val}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Actions & History */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Action Panel */}
          {(hasPrivilege('ADMISSION_UPDATE') || hasPrivilege('ADMISSION_APPROVE')) &&
            app.status !== 'REJECTED' && app.status !== 'STUDENT_CREATED' && (
            <div className="card">
              <div className="card-header"><div className="card-title">⚡ Actions</div></div>
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {hasPrivilege('ADMISSION_UPDATE') && app.status === 'SUBMITTED' && (
                  <button className="btn btn-secondary w-full" style={{ justifyContent: 'center' }}
                    onClick={markUnderReview}>
                    🔍 Mark Under Review
                  </button>
                )}
                {hasPrivilege('ADMISSION_APPROVE') &&
                  (app.status === 'SUBMITTED' || app.status === 'UNDER_REVIEW') && (
                  <>
                    <button className="btn btn-success w-full" style={{ justifyContent: 'center' }}
                      onClick={approve} disabled={loading}>
                      {loading ? '⏳ Processing...' : '✅ Approve & Create Student'}
                    </button>
                    <button className="btn btn-danger w-full" style={{ justifyContent: 'center' }}
                      onClick={() => setShowReject(true)}>
                      ❌ Reject Application
                    </button>
                  </>
                )}

                {showReject && (
                  <div style={{
                    background: 'var(--danger-bg)', borderRadius: 'var(--radius-md)',
                    padding: 16, border: '1px solid #fecaca',
                  }}>
                    <div className="form-group" style={{ marginBottom: 10 }}>
                      <label className="form-label" style={{ color: '#991b1b' }}>
                        Rejection Reason <span className="required">*</span>
                      </label>
                      <textarea className="form-textarea" rows={3}
                        placeholder="Enter detailed reason for rejection..."
                        value={rejectReason}
                        onChange={e => setRejectReason(e.target.value)}
                        style={{ borderColor: 'var(--danger)' }}
                      />
                    </div>
                    <div className="flex gap-2">
                      <button className="btn btn-danger btn-sm" onClick={reject}>Confirm Reject</button>
                      <button className="btn btn-ghost btn-sm" onClick={() => setShowReject(false)}>Cancel</button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Status History */}
          <div className="card" style={{ flex: 1 }}>
            <div className="card-header"><div className="card-title">📋 Status History</div></div>
            <div style={{ padding: '8px 20px' }}>
              {history.length === 0 ? (
                <div style={{ padding: '24px 0', textAlign: 'center', color: 'var(--gray-400)', fontSize: 13 }}>
                  No history yet
                </div>
              ) : history.map((h, idx) => (
                <div key={idx} className="activity-item">
                  <div className="activity-dot" style={{
                    background: h.toStatus === 'APPROVED' ? 'var(--success)'
                      : h.toStatus === 'REJECTED' ? 'var(--danger)'
                      : h.toStatus === 'STUDENT_CREATED' ? 'var(--primary-500)'
                      : 'var(--warning)',
                  }} />
                  <div className="activity-content">
                    <div className="activity-text">
                      <span style={{ color: 'var(--gray-400)', fontSize: 12 }}>{h.fromStatus || 'NEW'}</span>
                      <span style={{ margin: '0 6px' }}>→</span>
                      <strong>{h.toStatus}</strong>
                      {h.remarks && <span style={{ color: 'var(--gray-500)', fontSize: 12 }}> — {h.remarks}</span>}
                    </div>
                    <div className="activity-time">
                      {h.changedAt ? new Date(h.changedAt).toLocaleString('en-IN') : '—'}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
