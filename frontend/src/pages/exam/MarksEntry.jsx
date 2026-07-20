import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';

export default function MarksEntry() {
  const [examinations, setExaminations]   = useState([]);
  const [subjects, setSubjects]           = useState([]);
  const [classSections, setClassSections] = useState([]);
  const [componentOptions, setComponentOptions] = useState([]);
  const [examinationId, setExaminationId] = useState('');
  const [subjectId, setSubjectId]         = useState('');
  const [componentCode, setComponentCode] = useState('');
  const [classSectionId, setClassSectionId] = useState('');
  const [maxMarks, setMaxMarks]           = useState(20);
  const [roster, setRoster]               = useState([]);
  const [marks, setMarks]                 = useState({});
  const [toast, setToast]                 = useState(null);
  const [loading, setLoading]             = useState(false);

  useEffect(() => {
  axiosClient.get('/examinations').then(res => {
    console.log("Full Response =", res.data);
    console.log("Exams =", res.data.data);
    console.log("Length =", res.data.data?.length);

    setExaminations(res.data.data ?? []);
});
    axiosClient.get('/master/subjects').then(res => setSubjects(res.data.data ?? []));
    axiosClient.get('/master/class-sections').then(res => setClassSections(res.data.data ?? []));
    axiosClient.get('/master/mark-components').then(res => {
      const data = res.data.data ?? [];
      setComponentOptions(data);
      if (data.length > 0) setComponentCode(data[0].code);
    });
  }, []);

  const loadRoster = async () => {
    try {
      const section = classSections.find(c => String(c.id) === String(classSectionId));
      const res = await axiosClient.get('/enrollments/roster', {
        params: { classSectionId, semester: section?.semester },
      });
      const students = res.data.data ?? [];
      console.log("Roster API =", students);
      setRoster(students);
      const init = {};
      students.forEach(s => { init[s.id] = ''; });
      setMarks(init);
    } catch {
      setToast({ type: 'error', message: 'Failed to load roster' });
    }
  };

  const saveMarks = async () => {
    if (!examinationId || !subjectId || !componentCode || roster.length === 0) {
      return setToast({ type: 'error', message: 'Please fill all required fields and load the roster' });
    }
    const component = componentOptions.find(c => c.code === componentCode);
    const entries = roster
      .filter(s => marks[s.id] !== '' && marks[s.id] !== undefined)
      .map(s => ({ studentId: s.id, marksObtained: Number(marks[s.id]) }));

    if (entries.length === 0) {
      return setToast({ type: 'error', message: 'Please enter marks for at least one student' });
    }
    setLoading(true);
    try {
      await axiosClient.post('/exams/marks/bulk', {
        examinationId, subjectId,
        componentId: component?.id,
        maxMarks: Number(maxMarks),
        entries,
      });
      setToast({ type: 'success', message: `Marks saved for ${entries.length} students!` });
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to save marks' });
    } finally { setLoading(false); }
  };

  const computeResults = async () => {
    if (!examinationId || !subjectId || roster.length === 0) {
      return setToast({ type: 'error', message: 'Select examination and subject first' });
    }
    setLoading(true);
    try {
      for (const s of roster) {
        await axiosClient.post('/exams/compute-result', null, {
          params: { examinationId, subjectId, studentId: s.id },
        });
      }
      setToast({ type: 'success', message: 'Results computed! Status: DRAFT (publish when ready)' });
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to compute results' });
    } finally { setLoading(false); }
  };

  const getGradeColor = (obtained, max) => {
    if (!obtained || !max) return 'var(--gray-300)';
    const pct = (obtained / max) * 100;
    if (pct >= 75) return 'var(--success)';
    if (pct >= 50) return 'var(--warning)';
    return 'var(--danger)';
  };

  return (
    <Layout title="Marks Entry" subtitle="Enter student marks for examinations">

      {/* Setup */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-header"><div className="card-title">⚙️ Examination Setup</div></div>
        <div className="card-body">
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Examination <span className="required">*</span></label>
              <select className="form-select" value={examinationId} onChange={e => setExaminationId(e.target.value)}>
                <option value="">Select examination</option>
                {examinations.map(e => (
                  <option key={e.id} value={e.id}>{e.name} — Sem {e.semester}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Subject <span className="required">*</span></label>
              <select className="form-select" value={subjectId} onChange={e => setSubjectId(e.target.value)}>
                <option value="">Select subject</option>
                {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Mark Component <span className="required">*</span></label>
              <select className="form-select" value={componentCode} onChange={e => setComponentCode(e.target.value)}>
                {componentOptions.map(c => (
                  <option key={c.code} value={c.code}>{c.name} ({c.weightPercentage}%)</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Max Marks</label>
              <input className="form-input" type="number" value={maxMarks}
                onChange={e => setMaxMarks(e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Class Section <span className="required">*</span></label>
              <select className="form-select" value={classSectionId} onChange={e => setClassSectionId(e.target.value)}>
                <option value="">Select class section</option>
                {classSections.map(c => (
                  <option key={c.id} value={c.id}>{c.courseName} · Sem {c.semester} · {c.sectionName}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" onClick={loadRoster} disabled={!classSectionId}>
              📋 Load Student Roster
            </button>
          </div>
        </div>
      </div>

      {/* Marks Entry Table */}
      {roster.length > 0 && (
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">📊 Enter Marks — {roster.length} Students</div>
              <div className="card-subtitle">Max marks: {maxMarks}</div>
            </div>
          </div>

          <div style={{ padding: '0 20px' }}>
            {roster.map((student, idx) => {
              const val = marks[student.id];
              const color = getGradeColor(val, maxMarks);
              return (
                <div key={student.id} style={{
                  display: 'flex', alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '12px 0',
                  borderBottom: idx < roster.length - 1 ? '1px solid var(--gray-100)' : 'none',
                  gap: 16,
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12, flex: 1 }}>
                    <div style={{
                      width: 36, height: 36, borderRadius: '50%',
                      background: 'linear-gradient(135deg, var(--primary-400), var(--accent-400))',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: 14, fontWeight: 700, color: '#fff', flexShrink: 0,
                    }}>{student.fullName?.charAt(0)}</div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 13.5 }}>{student.fullName}</div>
                      <div style={{ fontSize: 12, color: 'var(--gray-400)' }}>{student.studentCode}</div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <div style={{ position: 'relative' }}>
                      <input
                        type="number"
                        min="0"
                        max={maxMarks}
                        placeholder="—"
                        value={val}
                        onChange={e => setMarks(prev => ({ ...prev, [student.id]: e.target.value }))}
                        style={{
                          width: 90, padding: '8px 12px',
                          border: `2px solid ${val ? color : 'var(--gray-300)'}`,
                          borderRadius: 'var(--radius-md)',
                          textAlign: 'center',
                          fontSize: 15, fontWeight: 700,
                          color: val ? color : 'var(--gray-600)',
                          outline: 'none',
                          transition: 'border-color 0.15s',
                        }}
                      />
                    </div>
                    <span style={{ color: 'var(--gray-400)', fontSize: 13 }}>/ {maxMarks}</span>
                    {val && (
                      <span style={{
                        minWidth: 42, textAlign: 'center',
                        fontSize: 12, fontWeight: 700,
                        background: color === 'var(--success)' ? '#d1fae5'
                          : color === 'var(--warning)' ? '#fef3c7' : '#fee2e2',
                        color,
                        padding: '3px 8px', borderRadius: 99,
                      }}>
                        {Math.round((val / maxMarks) * 100)}%
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          <div className="card-footer">
            <div style={{ fontSize: 13, color: 'var(--gray-500)' }}>
              {Object.values(marks).filter(v => v !== '').length}/{roster.length} marks entered
            </div>
            <div className="flex gap-2">
              <button className="btn btn-secondary" onClick={computeResults} disabled={loading}>
                🧮 Compute Results
              </button>
              <button className="btn btn-primary" onClick={saveMarks} disabled={loading}>
                {loading ? '⏳ Saving...' : '💾 Save Marks'}
              </button>
            </div>
          </div>
        </div>
      )}

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
