import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';

export default function RoleManagement() {
  const [roles, setRoles] = useState([]);
  const [allPrivileges, setAllPrivileges] = useState([]);
  const [selectedRole, setSelectedRole] = useState(null);
  const [selectedPrivs, setSelectedPrivs] = useState([]);
  const [toast, setToast] = useState(null);

  const load = () => {
    axiosClient.get('/roles').then(res => setRoles(res.data.data));
    axiosClient.get('/roles/privileges').then(res => setAllPrivileges(res.data.data));
  };
  useEffect(() => { load(); }, []);

  const openRole = role => {
    setSelectedRole(role);
    setSelectedPrivs(role.privilegeCodes || []);
  };

  const togglePriv = code =>
    setSelectedPrivs(prev => prev.includes(code) ? prev.filter(p=>p!==code) : [...prev, code]);

  const save = async () => {
    await axiosClient.put(`/roles/${selectedRole.id}/privileges`, { privilegeCodes: selectedPrivs });
    setToast({ type:'success', message:'Privileges updated!' });
    setSelectedRole(null);
    load();
  };

  // Group by module
  const grouped = allPrivileges.reduce((acc, code) => {
    const mod = code.split('_').slice(0,-1).join('_') || code;
    acc[mod] = acc[mod] || [];
    acc[mod].push(code);
    return acc;
  }, {});

  const moduleEmojis = { USER:'👤', ROLE:'🛡️', STUDENT:'🎓', FACULTY:'👨‍🏫', ADMISSION:'📝',
    ATTENDANCE:'✅', EXAMINATION:'📊', FEES:'💰', REPORTS:'📈', AUDIT:'📋',
    DASHBOARD:'🏠', MASTER_DATA:'⚙️' };

  return (
    <Layout title="Roles & Privileges" subtitle="Configure access control for each role">
      <div style={{ display:'grid', gridTemplateColumns:'280px 1fr', gap:20, alignItems:'start' }}>

        {/* Roles List */}
        <div className="card" style={{ position:'sticky', top:80 }}>
          <div className="card-header"><div className="card-title">🛡️ System Roles</div></div>
          <div style={{ padding:'8px 0' }}>
            {roles.map(role => (
              <div key={role.id}
                onClick={() => !role.systemRole && openRole(role)}
                style={{
                  padding:'12px 20px',
                  cursor: role.systemRole ? 'default' : 'pointer',
                  borderLeft: selectedRole?.id === role.id ? '3px solid var(--primary-500)' : '3px solid transparent',
                  background: selectedRole?.id === role.id ? 'var(--primary-50)' : 'transparent',
                  transition:'all 0.15s',
                }}
                onMouseEnter={e => { if (!role.systemRole) e.currentTarget.style.background='var(--gray-50)'; }}
                onMouseLeave={e => { if (selectedRole?.id !== role.id) e.currentTarget.style.background='transparent'; }}
              >
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                  <div style={{ fontWeight:600, fontSize:13.5, color:'var(--gray-800)' }}>
                    {role.name.replace(/_/g,' ')}
                  </div>
                  {role.systemRole && (
                    <span style={{ fontSize:10, background:'var(--gray-100)', color:'var(--gray-500)', padding:'2px 7px', borderRadius:99, fontWeight:600 }}>
                      SYSTEM
                    </span>
                  )}
                </div>
                <div style={{ fontSize:12, color:'var(--gray-400)', marginTop:2 }}>
                  {role.privilegeCodes?.length || 0} privileges
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Privileges Editor */}
        <div className="card">
          {!selectedRole ? (
            <div className="empty-state" style={{ padding:'80px 24px' }}>
              <span className="empty-state-emoji">🛡️</span>
              <div className="empty-state-title">Select a Role</div>
              <div className="empty-state-desc">Click a non-system role on the left to edit its privileges</div>
            </div>
          ) : (
            <>
              <div className="card-header">
                <div>
                  <div className="card-title">Editing: {selectedRole.name.replace(/_/g,' ')}</div>
                  <div className="card-subtitle">{selectedPrivs.length} privileges selected</div>
                </div>
                <div className="flex gap-2">
                  <button className="btn btn-primary btn-sm" onClick={save}>💾 Save</button>
                  <button className="btn btn-ghost btn-sm" onClick={() => setSelectedRole(null)}>✕</button>
                </div>
              </div>
              <div className="card-body">
                {Object.entries(grouped).map(([mod, codes]) => (
                  <div key={mod} style={{ marginBottom:20 }}>
                    <div style={{
                      fontSize:12, fontWeight:700, textTransform:'uppercase',
                      letterSpacing:'0.05em', color:'var(--gray-500)',
                      marginBottom:10, display:'flex', alignItems:'center', gap:6,
                    }}>
                      {moduleEmojis[mod] || '⚙️'} {mod.replace(/_/g,' ')}
                    </div>
                    <div style={{ display:'flex', gap:8, flexWrap:'wrap' }}>
                      {codes.map(code => {
                        const action = code.split('_').slice(-1)[0];
                        const selected = selectedPrivs.includes(code);
                        return (
                          <label key={code} style={{
                            display:'flex', alignItems:'center', gap:6,
                            padding:'6px 14px',
                            border:`1.5px solid ${selected?'var(--primary-400)':'var(--gray-200)'}`,
                            borderRadius:'var(--radius-md)',
                            background: selected?'var(--primary-50)':'#fff',
                            cursor:'pointer',
                            fontSize:12.5, fontWeight:600,
                            color: selected?'var(--primary-700)':'var(--gray-500)',
                            transition:'all 0.15s',
                          }}>
                            <input type="checkbox" style={{ display:'none' }}
                              checked={selected} onChange={() => togglePriv(code)} />
                            {selected ? '✅' : '⬜'} {action}
                          </label>
                        );
                      })}
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
