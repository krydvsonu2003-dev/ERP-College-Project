import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import axiosClient from '../../api/axiosClient';

export default function MyResults() {
  const [results, setResults] = useState([]);
  const [semResults, setSemResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axiosClient.get('/students/me').then(async res => {
      const id = res.data.data.id;
      const [r, sr] = await Promise.all([
        axiosClient.get(`/exams/students/${id}/results`),
        axiosClient.get(`/exams/students/${id}/semester-results`),
      ]);
      setResults(r.data.data ?? []);
      setSemResults(sr.data.data ?? []);
    }).finally(() => setLoading(false));
  }, []);

  const gradeColors = {
    'O': '#065f46', 'A+': '#065f46', 'A': '#047857',
    'B+': 'var(--primary-700)', 'B': 'var(--primary-500)',
    'C': '#92400e', 'F': '#991b1b',
  };

  const latestCGPA = semResults.length > 0 ? semResults[semResults.length - 1].cgpa : null;
  const latestSGPA = semResults.length > 0 ? semResults[semResults.length - 1].sgpa : null;

  return (
    <Layout title="My Results" subtitle="Your academic performance and grades">
      {loading ? (
        <div style={{ padding: 60, textAlign: 'center', color: 'var(--gray-400)' }}>⏳ Loading results...</div>
      ) : (
        <>
          {/* CGPA/SGPA Banner */}
          {latestCGPA && (
            <div style={{
              background: 'linear-gradient(135deg, var(--primary-700), var(--primary-500))',
              borderRadius: 'var(--radius-lg)',
              padding: '24px 32px',
              marginBottom: 24,
              color: '#fff',
              display: 'flex', alignItems: 'center', gap: 40,
              boxShadow: 'var(--shadow-lg)',
            }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 42, fontWeight: 800 }}>{latestCGPA}</div>
                <div style={{ fontSize: 12, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                  Current CGPA
                </div>
              </div>
              <div style={{ width: 1, height: 60, background: 'rgba(255,255,255,0.2)' }} />
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 42, fontWeight: 800 }}>{latestSGPA}</div>
                <div style={{ fontSize: 12, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                  Latest SGPA
                </div>
              </div>
              <div style={{ width: 1, height: 60, background: 'rgba(255,255,255,0.2)' }} />
              <div>
                <div style={{ fontSize: 14, opacity: 0.8, marginBottom: 6 }}>Academic Standing</div>
                <div style={{ fontWeight: 700, fontSize: 18 }}>
                  {latestCGPA >= 8 ? '🏆 Excellent' : latestCGPA >= 6.5 ? '⭐ Good' : latestCGPA >= 5 ? '📚 Average' : '⚠️ Needs Improvement'}
                </div>
              </div>
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
            {/* Semester SGPA/CGPA */}
            <div className="card">
              <div className="card-header">
                <div className="card-title">📈 Semester-wise Performance</div>
              </div>
              {semResults.length === 0 ? (
                <div className="empty-state" style={{ padding: '40px 24px' }}>
                  <span className="empty-state-emoji">📊</span>
                  <div className="empty-state-title">No results published yet</div>
                </div>
              ) : (
                <div style={{ padding: '8px 20px' }}>
                  {semResults.map(sr => (
                    <div key={sr.id} style={{
                      padding: '14px 0',
                      borderBottom: '1px solid var(--gray-100)',
                      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    }}>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: 14 }}>Semester {sr.semester}</div>
                        <div style={{ fontSize: 12, color: 'var(--gray-400)' }}>Credits: {sr.totalCredits}</div>
                      </div>
                      <div style={{ display: 'flex', gap: 20 }}>
                        <div style={{ textAlign: 'center' }}>
                          <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--primary-600)' }}>{sr.sgpa}</div>
                          <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>SGPA</div>
                        </div>
                        <div style={{ textAlign: 'center' }}>
                          <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--accent-600)' }}>{sr.cgpa}</div>
                          <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>CGPA</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Subject Results */}
            <div className="card">
              <div className="card-header">
                <div className="card-title">📚 Subject Results</div>
              </div>
              {results.length === 0 ? (
                <div className="empty-state" style={{ padding: '40px 24px' }}>
                  <span className="empty-state-emoji">🎓</span>
                  <div className="empty-state-title">No published results</div>
                </div>
              ) : (
                <div style={{ padding: '8px 20px' }}>
                  {results.map(r => (
                    <div key={r.id} style={{
                      padding: '12px 0',
                      borderBottom: '1px solid var(--gray-100)',
                      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    }}>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: 13.5 }}>{r.subjectName}</div>
                        <div style={{ fontSize: 12, color: 'var(--gray-400)' }}>
                          {r.totalMarksObtained}/{r.totalMaxMarks} marks
                        </div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <div style={{ width: 50, textAlign: 'right' }}>
                          <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--gray-600)' }}>{r.percentage}%</div>
                        </div>
                        <div style={{
                          width: 36, height: 36,
                          borderRadius: '50%',
                          background: r.gradeLetter === 'F' ? '#fee2e2' : '#d1fae5',
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                          fontFamily: 'monospace', fontWeight: 800, fontSize: 14,
                          color: gradeColors[r.gradeLetter] || 'var(--gray-600)',
                        }}>
                          {r.gradeLetter || '—'}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </Layout>
  );
}
