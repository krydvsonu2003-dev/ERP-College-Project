import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';

const STATUS_OPTIONS = [
  { value: 'PRESENT', label: 'Present', color: 'var(--success)', emoji: '✅' },
  { value: 'ABSENT',  label: 'Absent',  color: 'var(--danger)',  emoji: '❌' },
  { value: 'LATE',    label: 'Late',    color: 'var(--warning)', emoji: '⏰' },
  { value: 'EXCUSED', label: 'Excused', color: 'var(--info)',    emoji: '📋' },
];

export default function AttendanceEntry() {
  const [classSections, setClassSections] = useState([]);
  const [subjects, setSubjects]           = useState([]);
  const [classSectionId, setClassSectionId] = useState('');
  const [subjectId, setSubjectId]           = useState('');
  const [date, setDate]                     = useState(() => new Date().toISOString().slice(0, 10));
  const [sessionNumber, setSessionNumber]   = useState(1);
  const [roster, setRoster]                 = useState([]);
  const [statuses, setStatuses]             = useState({});
  const [toast, setToast]                   = useState(null);
  const [loading, setLoading]               = useState(false);
  const [rosterLoading, setRosterLoading]   = useState(false);

  useEffect(() => {
    axiosClient.get('/master/class-sections').then(res => setClassSections(res.data.data ?? []));
    axiosClient.get('/master/subjects').then(res => setSubjects(res.data.data ?? []));
  }, []);

const loadRoster = async () => {
  if (!classSectionId) return;

  const section = classSections.find(
    c => String(c.id) === String(classSectionId)
  );

  console.log("Selected Section =", section);

  setRosterLoading(true);

  try {
    const res = await axiosClient.get('/enrollments/roster', {
      params: {
        classSectionId,
        semester: section?.semester,
      },
    });

    console.log("API Response =", res.data);

    const students = res.data.data ?? [];
    console.log("Students =", students);

    setRoster(students);

    const init = {};
    students.forEach(s => {
      init[s.id] = "PRESENT";
    });

    setStatuses(init);

  } catch (err) {
    console.log("Error =", err.response?.data);
    setToast({
      type: "error",
      message: "Failed to load roster",
    });
  } finally {
    setRosterLoading(false);
  }
};

  const markAll = status => {
    const all = {};
    roster.forEach(s => { all[s.id] = status; });
    setStatuses(all);
  };

  const submitAttendance = async () => {
    if (!classSectionId || !subjectId) {
      return setToast({ type: 'error', message: 'Please select class section and subject' });
    }
    setLoading(true);
    try {
      await axiosClient.post('/attendance/mark', {
        classSectionId, subjectId,
        attendanceDate: date,
        sessionNumber,
        entries: roster.map(s => ({ studentId: s.id, status: statuses[s.id] || 'PRESENT' })),
      });
      setToast({ type: 'success', message: `Attendance saved for ${roster.length} students!` });
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to save attendance' });
    } finally { setLoading(false); }
  };

  const presentCount = Object.values(statuses).filter(s => s === 'PRESENT').length;
  const absentCount  = Object.values(statuses).filter(s => s === 'ABSENT').length;
console.log("Roster State =", roster);
  return (
    <Layout title="Mark Attendance" subtitle="Record student attendance for your classes">

      {/* Setup Card */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-header">
          <div className="card-title">⚙️ Session Setup</div>
        </div>
        <div className="card-body">
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Class Section <span className="required">*</span></label>
              <select className="form-select" value={classSectionId} onChange={e => setClassSectionId(e.target.value)}>
                <option value="">Select class section</option>
                {classSections.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.courseName} · Sem {c.semester} · Section {c.sectionName}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Subject <span className="required">*</span></label>
              <select className="form-select" value={subjectId} onChange={e => setSubjectId(e.target.value)}>
                <option value="">Select subject</option>
                {subjects.map(s => (
                  <option key={s.id} value={s.id}>{s.name} ({s.code})</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Date <span className="required">*</span></label>
              <input className="form-input" type="date" value={date} onChange={e => setDate(e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Session Number</label>
              <input className="form-input" type="number" min="1" max="8"
                value={sessionNumber} onChange={e => setSessionNumber(e.target.value)} />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" onClick={loadRoster} disabled={!classSectionId || rosterLoading}>
              {rosterLoading ? '⏳ Loading...' : '📋 Load Student Roster'}
            </button>
          </div>
        </div>
      </div>

      {/* Roster */}
      {roster.length > 0 && (
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">📋 Student Roster — {roster.length} Students</div>
              <div className="card-subtitle" style={{ display: 'flex', gap: 16, marginTop: 4 }}>
                <span style={{ color: 'var(--success)', fontWeight: 600 }}>✅ Present: {presentCount}</span>
                <span style={{ color: 'var(--danger)', fontWeight: 600 }}>❌ Absent: {absentCount}</span>
              </div>
            </div>
            {/* Mark All */}
            <div className="flex gap-2">
              {STATUS_OPTIONS.map(opt => (
                <button key={opt.value} className="btn btn-secondary btn-sm"
                  style={{ color: opt.color, borderColor: opt.color }}
                  onClick={() => markAll(opt.value)}>
                  {opt.emoji} All {opt.label}
                </button>
              ))}
            </div>
          </div>

          <div style={{ padding: '0 20px' }}>
            {roster.map((student, idx) => (
              <div key={student.id} style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '12px 0',
                borderBottom: idx < roster.length - 1 ? '1px solid var(--gray-100)' : 'none',
                gap: 16,
              }}>
                {/* Student Info */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, flex: 1 }}>
                  <div style={{
                    width: 36, height: 36, borderRadius: '50%',
                    background: 'linear-gradient(135deg, var(--primary-400), var(--accent-400))',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 14, fontWeight: 700, color: '#fff', flexShrink: 0,
                  }}>
                    {student.fullName?.charAt(0)}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 13.5 }}>{student.fullName}</div>
                    <div style={{ fontSize: 12, color: 'var(--gray-400)' }}>{student.studentCode}</div>
                  </div>
                </div>

                {/* Status Selector */}
                <div style={{ display: 'flex', gap: 6 }}>
                  {STATUS_OPTIONS.map(opt => (
                    <button key={opt.value}
                      onClick={() => setStatuses(prev => ({ ...prev, [student.id]: opt.value }))}
                      style={{
                        padding: '6px 14px',
                        borderRadius: 'var(--radius-md)',
                        border: `1.5px solid ${statuses[student.id] === opt.value ? opt.color : 'var(--gray-200)'}`,
                        background: statuses[student.id] === opt.value
                          ? opt.value === 'PRESENT' ? '#d1fae5'
                          : opt.value === 'ABSENT' ? '#fee2e2'
                          : opt.value === 'LATE' ? '#fef3c7'
                          : '#dbeafe'
                          : '#fff',
                        color: statuses[student.id] === opt.value ? opt.color : 'var(--gray-500)',
                        fontWeight: 600, fontSize: 12.5, cursor: 'pointer',
                        transition: 'all 0.1s',
                      }}>
                      {opt.emoji} {opt.label}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>

          <div className="card-footer">
            <div style={{ fontSize: 13, color: 'var(--gray-500)' }}>
              {presentCount}/{roster.length} students present
            </div>
            <button className="btn btn-primary" onClick={submitAttendance} disabled={loading}>
              {loading ? '⏳ Saving...' : '💾 Save Attendance'}
            </button>
          </div>
        </div>
      )}

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
