import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';

const EMPTY = {
  fullName:'', gender:'MALE', dateOfBirth:'', mobileNumber:'',
  email:'', address:'', category:'', idProofNumber:'',
  courseId:'', academicYearId:'', entrySemester:1,
  fatherName:'', motherName:'', guardianName:'', guardianContact:'',
  occupation:'', annualIncome:'',
  previousInstitution:'', qualification:'', boardUniversity:'',
  yearOfPassing:'', marksPercentage:'',
};

const STEPS = ['Personal Details', 'Course & Academic', 'Guardian Info', 'Review & Submit'];

export default function AdmissionForm() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [form, setForm] = useState(EMPTY);
  const [courses, setCourses] = useState([]);
  const [academicYears, setAcademicYears] = useState([]);
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axiosClient.get('/master/courses').then(r => setCourses(r.data.data ?? []));
    axiosClient.get('/master/academic-years').then(r => setAcademicYears(r.data.data ?? []));
  }, []);

  const set = key => e => setForm(f => ({ ...f, [key]: e.target.value }));

  const submit = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.post('/admissions', form);
      navigate(`/admissions/${res.data.data.id}`);
    } catch (err) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'Failed to submit application' });
    } finally { setLoading(false); }
  };

  const selectedCourse = courses.find(c => String(c.id) === String(form.courseId));

  return (
    <Layout title="New Admission Application" subtitle="Fill in the applicant's details below">

      {/* Stepper */}
      <div style={{ display: 'flex', gap: 0, marginBottom: 28, position: 'relative' }}>
        <div style={{
          position: 'absolute', top: 18, left: '10%', right: '10%',
          height: 2, background: 'var(--gray-200)', zIndex: 0,
        }} />
        {STEPS.map((label, idx) => (
          <div key={idx} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative', zIndex: 1 }}>
            <div style={{
              width: 36, height: 36, borderRadius: '50%',
              background: idx < step ? 'var(--success)' : idx === step ? 'var(--primary-600)' : '#fff',
              border: `2px solid ${idx < step ? 'var(--success)' : idx === step ? 'var(--primary-600)' : 'var(--gray-300)'}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontWeight: 700, fontSize: 14,
              color: idx <= step ? '#fff' : 'var(--gray-400)',
              boxShadow: idx === step ? '0 0 0 4px rgba(99,102,241,0.15)' : 'none',
              transition: 'all 0.2s',
            }}>
              {idx < step ? '✓' : idx + 1}
            </div>
            <div style={{
              fontSize: 12, fontWeight: idx === step ? 700 : 500, marginTop: 6,
              color: idx === step ? 'var(--primary-600)' : idx < step ? 'var(--success)' : 'var(--gray-400)',
            }}>{label}</div>
          </div>
        ))}
      </div>

      {/* Step 0 — Personal Details */}
      {step === 0 && (
        <div className="card">
          <div className="card-header">
            <div className="card-title">👤 Personal Details</div>
          </div>
          <div className="card-body">
            <div className="form-grid">
              <div className="form-group form-col-full">
                <label className="form-label">Full Name <span className="required">*</span></label>
                <input className="form-input" required value={form.fullName} onChange={set('fullName')} placeholder="As per documents" />
              </div>
              <div className="form-group">
                <label className="form-label">Gender <span className="required">*</span></label>
                <select className="form-select" value={form.gender} onChange={set('gender')}>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Date of Birth <span className="required">*</span></label>
                <input className="form-input" type="date" required value={form.dateOfBirth} onChange={set('dateOfBirth')} />
              </div>
              <div className="form-group">
                <label className="form-label">Mobile Number <span className="required">*</span></label>
                <input className="form-input" required value={form.mobileNumber} onChange={set('mobileNumber')} placeholder="+91 9876543210" />
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input className="form-input" type="email" value={form.email} onChange={set('email')} placeholder="student@email.com" />
              </div>
              <div className="form-group">
                <label className="form-label">Category</label>
                <select className="form-select" value={form.category} onChange={set('category')}>
                  <option value="">Select category</option>
                  {['GENERAL','OBC','SC','ST','EWS'].map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">ID Proof Number</label>
                <input className="form-input" value={form.idProofNumber} onChange={set('idProofNumber')} placeholder="Aadhar / PAN / Passport" />
              </div>
              <div className="form-group form-col-full">
                <label className="form-label">Address</label>
                <textarea className="form-textarea" value={form.address} onChange={set('address')} placeholder="Full residential address" rows={3} />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Step 1 — Course & Academic */}
      {step === 1 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="card">
            <div className="card-header"><div className="card-title">🎓 Course Details</div></div>
            <div className="card-body">
              <div className="form-grid-3">
                <div className="form-group">
                  <label className="form-label">Course <span className="required">*</span></label>
                  <select className="form-select" required value={form.courseId} onChange={set('courseId')}>
                    <option value="">Select course</option>
                    {courses.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Academic Year <span className="required">*</span></label>
                  <select className="form-select" required value={form.academicYearId} onChange={set('academicYearId')}>
                    <option value="">Select year</option>
                    {academicYears.map(y => <option key={y.id} value={y.id}>{y.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Entry Semester</label>
                  <input className="form-input" type="number" min="1" value={form.entrySemester} onChange={set('entrySemester')} />
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header"><div className="card-title">📚 Previous Academic Details</div></div>
            <div className="card-body">
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Previous Institution</label>
                  <input className="form-input" value={form.previousInstitution} onChange={set('previousInstitution')} />
                </div>
                <div className="form-group">
                  <label className="form-label">Qualification</label>
                  <input className="form-input" value={form.qualification} onChange={set('qualification')} placeholder="e.g. 12th Science" />
                </div>
                <div className="form-group">
                  <label className="form-label">Board / University</label>
                  <input className="form-input" value={form.boardUniversity} onChange={set('boardUniversity')} />
                </div>
                <div className="form-group">
                  <label className="form-label">Year of Passing</label>
                  <input className="form-input" type="number" value={form.yearOfPassing} onChange={set('yearOfPassing')} placeholder="2024" />
                </div>
                <div className="form-group">
                  <label className="form-label">Percentage / CGPA</label>
                  <input className="form-input" type="number" step="0.01" value={form.marksPercentage} onChange={set('marksPercentage')} placeholder="e.g. 75.50" />
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Step 2 — Guardian */}
      {step === 2 && (
        <div className="card">
          <div className="card-header"><div className="card-title">👨‍👩‍👧 Parent / Guardian Details</div></div>
          <div className="card-body">
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Father's Name</label>
                <input className="form-input" value={form.fatherName} onChange={set('fatherName')} />
              </div>
              <div className="form-group">
                <label className="form-label">Mother's Name</label>
                <input className="form-input" value={form.motherName} onChange={set('motherName')} />
              </div>
              <div className="form-group">
                <label className="form-label">Guardian Name</label>
                <input className="form-input" value={form.guardianName} onChange={set('guardianName')} />
              </div>
              <div className="form-group">
                <label className="form-label">Guardian Contact</label>
                <input className="form-input" value={form.guardianContact} onChange={set('guardianContact')} placeholder="+91 9876543210" />
              </div>
              <div className="form-group">
                <label className="form-label">Occupation</label>
                <input className="form-input" value={form.occupation} onChange={set('occupation')} />
              </div>
              <div className="form-group">
                <label className="form-label">Annual Income (₹)</label>
                <input className="form-input" type="number" value={form.annualIncome} onChange={set('annualIncome')} />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Step 3 — Review */}
      {step === 3 && (
        <div className="card">
          <div className="card-header">
            <div className="card-title">📋 Review Application</div>
            <div className="card-subtitle">Please verify all details before submitting</div>
          </div>
          <div className="card-body">
            {[
              { section: '👤 Personal', items: [
                ['Full Name', form.fullName], ['Gender', form.gender],
                ['Date of Birth', form.dateOfBirth], ['Mobile', form.mobileNumber],
                ['Email', form.email], ['Category', form.category],
              ]},
              { section: '🎓 Course', items: [
                ['Course', selectedCourse?.name], ['Entry Semester', form.entrySemester],
                ['Previous Institution', form.previousInstitution], ['Marks %', form.marksPercentage],
              ]},
              { section: '👨‍👩‍👧 Guardian', items: [
                ['Father', form.fatherName], ['Mother', form.motherName],
                ['Contact', form.guardianContact], ['Occupation', form.occupation],
              ]},
            ].map(({ section, items }) => (
              <div key={section} style={{ marginBottom: 24 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase',
                  letterSpacing: '0.05em', marginBottom: 10, paddingBottom: 8, borderBottom: '1px solid var(--gray-100)' }}>
                  {section}
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px 20px' }}>
                  {items.map(([label, val]) => (
                    <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0' }}>
                      <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>{label}</span>
                      <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--gray-800)' }}>{val || '—'}</span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Navigation */}
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 20 }}>
        <button className="btn btn-secondary" onClick={() => setStep(s => Math.max(0, s - 1))} disabled={step === 0}>
          ← Previous
        </button>
        {step < STEPS.length - 1 ? (
          <button className="btn btn-primary" onClick={() => setStep(s => s + 1)}>
            Next → {STEPS[step + 1]}
          </button>
        ) : (
          <button className="btn btn-success" onClick={submit} disabled={loading || !form.fullName || !form.courseId}>
            {loading ? '⏳ Submitting...' : '✅ Submit Application'}
          </button>
        )}
      </div>

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
