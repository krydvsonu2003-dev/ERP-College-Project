import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import Badge from '../../components/Badge';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';

export default function ResultsView() {
  const { hasPrivilege } = useAuth();
  const [examinations, setExaminations] = useState([]);
  const [examinationId, setExaminationId] = useState('');
  const [results, setResults] = useState([]);
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axiosClient.get('/examinations').then(res => setExaminations(res.data.data ?? []));
  }, []);

  const loadResults = async id => {
    setExaminationId(id);
    if (!id) return setResults([]);
    const res = await axiosClient.get(`/exams/${id}/results`);
    setResults(res.data.data ?? []);
  };

  const publish = async () => {
    setLoading(true);
    try {
      await axiosClient.post(`/exams/${examinationId}/publish`, { remarks: 'Published via ERP' });
      setToast({ type: 'success', message: '🎉 Results published successfully! Students can now view them.' });
      loadResults(examinationId);
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to publish' });
    } finally { setLoading(false); }
  };

  const gradeColors = {
    'O': 'var(--success)', 'A+': 'var(--success)', 'A': 'var(--accent-500)',
    'B+': 'var(--primary-500)', 'B': 'var(--primary-400)', 'C': 'var(--warning)', 'F': 'var(--danger)',
  };

  const selectedExam = examinations.find(e => String(e.id) === String(examinationId));

  return (
    <Layout title="Examination Results" subtitle="View and publish examination results">

      {/* Filter Bar */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-body">
          <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label className="form-label">Select Examination</label>
              <select className="form-select" value={examinationId}
                onChange={e => loadResults(e.target.value)}>
                <option value="">Choose an examination to view results</option>
                {examinations.map(e => (
                  <option key={e.id} value={e.id}>
                    {e.name} — Sem {e.semester} [{e.status}]
                  </option>
                ))}
              </select>
            </div>
            {hasPrivilege('EXAMINATION_PUBLISH') && examinationId &&
              selectedExam?.status !== 'PUBLISHED' && (
              <button className="btn btn-success" onClick={publish} disabled={loading}>
                {loading ? '⏳ Publishing...' : '🚀 Publish Results'}
              </button>
            )}
            {selectedExam?.status === 'PUBLISHED' && (
              <div style={{
                display: 'flex', alignItems: 'center', gap: 8,
                padding: '9px 16px', borderRadius: 'var(--radius-md)',
                background: '#d1fae5', color: '#065f46', fontWeight: 600, fontSize: 13,
              }}>
                ✅ Results Published
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Results Table */}
      {results.length > 0 && (
        <>
          {/* Summary Bar */}
          <div className="stats-grid" style={{ marginBottom: 20 }}>
            {[
              { label: 'Total Students', value: results.length, icon: '👨‍🎓', color: 'blue' },
              { label: 'Passed', value: results.filter(r => r.gradeLetter !== 'F').length, icon: '✅', color: 'green' },
              { label: 'Failed', value: results.filter(r => r.gradeLetter === 'F').length, icon: '❌', color: 'red' },
              {
                label: 'Avg Percentage',
                value: `${Math.round(results.reduce((a, r) => a + Number(r.percentage || 0), 0) / results.length)}%`,
                icon: '📊', color: 'purple',
              },
            ].map(s => (
              <div key={s.label} className={`stat-card ${s.color}`}>
                <div className={`stat-icon ${s.color}`}>{s.icon}</div>
                <div className="stat-value">{s.value}</div>
                <div className="stat-label">{s.label}</div>
              </div>
            ))}
          </div>

          <div className="card">
            <div className="card-header">
              <div>
                <div className="card-title">📊 Result Cards</div>
                <div className="card-subtitle">{results.length} results loaded</div>
              </div>
            </div>
            <DataTable
              columns={[
                { key: 'studentName', header: 'Student', render: r => (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <div style={{
                      width: 32, height: 32, borderRadius: '50%',
                      background: 'linear-gradient(135deg, var(--primary-400), var(--accent-400))',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: 13, fontWeight: 700, color: '#fff', flexShrink: 0,
                    }}>{r.studentName?.charAt(0)}</div>
                    <span style={{ fontWeight: 600 }}>{r.studentName}</span>
                  </div>
                )},
                { key: 'subjectName', header: 'Subject', render: r => (
                  <span style={{ color: 'var(--gray-600)' }}>{r.subjectName}</span>
                )},
                { key: 'totalMarksObtained', header: 'Marks', render: r => (
                  <span style={{ fontFamily: 'monospace', fontWeight: 700 }}>
                    {r.totalMarksObtained}/{r.totalMaxMarks}
                  </span>
                )},
                { key: 'percentage', header: 'Percentage', render: r => (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ width: 60 }}>
                      <div className="progress-bar">
                        <div className="progress-fill" style={{
                          width: `${r.percentage}%`,
                          background: Number(r.percentage) >= 75 ? 'var(--success)'
                            : Number(r.percentage) >= 50 ? 'var(--warning)' : 'var(--danger)',
                        }} />
                      </div>
                    </div>
                    <span style={{ fontWeight: 700, fontSize: 13 }}>{r.percentage}%</span>
                  </div>
                )},
                { key: 'gradeLetter', header: 'Grade', render: r => r.gradeLetter ? (
                  <span style={{
                    fontFamily: 'monospace', fontWeight: 800, fontSize: 16,
                    color: gradeColors[r.gradeLetter] || 'var(--gray-600)',
                  }}>{r.gradeLetter}</span>
                ) : '—'},
                { key: 'gradePoint', header: 'Grade Point', render: r => (
                  <span style={{ fontWeight: 600, color: 'var(--primary-600)' }}>{r.gradePoint ?? '—'}</span>
                )},
                { key: 'status', header: 'Status', render: r => <Badge value={r.status} /> },
              ]}
              rows={results}
              emptyText="No results found"
            />
          </div>
        </>
      )}

      {examinationId && results.length === 0 && (
        <div className="empty-state" style={{ background: '#fff', borderRadius: 'var(--radius-lg)', padding: 60 }}>
          <span className="empty-state-emoji">📊</span>
          <div className="empty-state-title">No Results Yet</div>
          <div className="empty-state-desc">Enter marks and compute results to see them here</div>
        </div>
      )}

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
