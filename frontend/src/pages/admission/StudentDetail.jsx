import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import Badge from '../../components/Badge';
import axiosClient from '../../api/axiosClient';

export default function StudentDetail() {
  const { id } = useParams();
  const [student, setStudent] = useState(null);

  useEffect(() => {
    axiosClient.get(`/students/${id}`).then(res => setStudent(res.data.data));
  }, [id]);

  if (!student) return (
    <Layout title="Student Profile">
      <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:300, color:'var(--gray-400)' }}>
        ⏳ Loading...
      </div>
    </Layout>
  );

  const initials = student.fullName?.split(' ').map(n=>n[0]).slice(0,2).join('');

  return (
    <Layout title="Student Profile" subtitle={student.studentCode}
      actions={<Link to="/admissions" className="btn btn-secondary">← Back to Admissions</Link>}
    >
      {/* Profile Header Card */}
      <div style={{
        background:'linear-gradient(135deg, var(--primary-700), var(--primary-500))',
        borderRadius:'var(--radius-lg)',
        padding:'28px',
        marginBottom:20,
        color:'#fff',
        display:'flex', alignItems:'center', gap:24,
        boxShadow:'var(--shadow-xl)',
      }}>
        <div style={{
          width:80, height:80, borderRadius:'50%',
          background:'rgba(255,255,255,0.2)',
          display:'flex', alignItems:'center', justifyContent:'center',
          fontSize:32, fontWeight:800,
          border:'3px solid rgba(255,255,255,0.4)',
          flexShrink:0,
        }}>{initials}</div>
        <div style={{ flex:1 }}>
          <div style={{ fontSize:24, fontWeight:800, marginBottom:6 }}>{student.fullName}</div>
          <div style={{ display:'flex', gap:20, flexWrap:'wrap', opacity:0.85, fontSize:13.5 }}>
            <span>🎓 {student.courseName}</span>
            <span>🏛️ {student.departmentName}</span>
            <span>📚 Semester {student.currentSemester}</span>
            <span>📅 {student.academicYearName}</span>
          </div>
        </div>
        <div style={{ textAlign:'center' }}>
          <code style={{
            background:'rgba(255,255,255,0.15)',
            borderRadius:8, padding:'10px 18px',
            fontSize:16, fontWeight:800, display:'block',
          }}>{student.studentCode}</code>
          <div style={{ fontSize:11, opacity:0.7, marginTop:4 }}>Student Code</div>
        </div>
      </div>

      {/* Details Grid */}
      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:20 }}>
        <div className="card">
          <div className="card-header"><div className="card-title">👤 Personal Information</div></div>
          <div className="card-body">
            {[
              { label:'Full Name', value:student.fullName },
              { label:'Gender', value:student.gender },
              { label:'Date of Birth', value:student.dateOfBirth },
              { label:'Mobile Number', value:student.mobileNumber },
              { label:'Email', value:student.email },
              { label:'Category', value:student.category },
            ].map(item => (
              <div key={item.label} style={{
                display:'flex', justifyContent:'space-between',
                padding:'10px 0', borderBottom:'1px solid var(--gray-100)',
              }}>
                <span style={{ fontSize:13, color:'var(--gray-500)', fontWeight:500 }}>{item.label}</span>
                <span style={{ fontSize:13, fontWeight:600, color:'var(--gray-800)' }}>{item.value || '—'}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-header"><div className="card-title">🎓 Academic Information</div></div>
          <div className="card-body">
            {[
              { label:'Course', value:student.courseName },
              { label:'Department', value:student.departmentName },
              { label:'Current Semester', value:student.currentSemester },
              { label:'Academic Year', value:student.academicYearName },
              { label:'Admitted On', value:student.admittedOn },
              { label:'Status', value:<Badge value={student.status} /> },
            ].map(item => (
              <div key={item.label} style={{
                display:'flex', justifyContent:'space-between', alignItems:'center',
                padding:'10px 0', borderBottom:'1px solid var(--gray-100)',
              }}>
                <span style={{ fontSize:13, color:'var(--gray-500)', fontWeight:500 }}>{item.label}</span>
                <span style={{ fontSize:13, fontWeight:600, color:'var(--gray-800)' }}>{item.value || '—'}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </Layout>
  );
}
