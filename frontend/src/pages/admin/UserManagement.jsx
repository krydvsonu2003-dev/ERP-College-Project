import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import Badge from '../../components/Badge';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';

const ROLE_OPTIONS = ['SUPER_ADMIN','PRINCIPAL','HOD','FACULTY','STUDENT','ACCOUNTANT','ADMISSION_OFFICE_STAFF'];

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ username:'', email:'', fullName:'', phone:'', roleNames:[] });
  const [toast, setToast] = useState(null);
  const [tempPass, setTempPass] = useState(null);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);

 const load = async () => {
  const res = await axiosClient.get('/users', {
    params: {
      size: 100,
      search: search || undefined
    }
  });

  console.log("Users API Response:", res.data);

  setUsers(res.data.data?.content ?? []);
};

  useEffect(() => { load(); }, [search]);

  const toggleRole = role =>
    setForm(f => ({
      ...f,
      roleNames: f.roleNames.includes(role) ? f.roleNames.filter(r=>r!==role) : [...f.roleNames, role],
    }));

  const submit = async e => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await axiosClient.post('/users', form);
      setTempPass(res.data.data.temporaryPassword);
      setForm({ username:'', email:'', fullName:'', phone:'', roleNames:[] });
      setShowForm(false);
      load();
      setToast({ type:'success', message:'User created successfully!' });
    } catch (err) {
      setToast({ type:'error', message: err?.response?.data?.message || 'Failed to create user' });
    } finally { setLoading(false); }
  };

  const toggleStatus = async user => {
    await axiosClient.patch(`/users/${user.id}/status`, { active: user.status !== 'ACTIVE' });
    load();
    setToast({ type:'success', message: user.status === 'ACTIVE' ? 'User deactivated' : 'User activated' });
  };

  const resetPwd = async user => {
    const res = await axiosClient.post(`/users/${user.id}/reset-password`);
    setTempPass(res.data.data.temporaryPassword);
    setToast({ type:'success', message:'Password reset successfully!' });
  };

const deleteUser = async (user) => {

  if (!window.confirm(`Delete user ${user.fullName}?`)) return;

  try {

    await axiosClient.delete(`/users/${user.id}`);

    setToast({
      type: 'success',
      message: 'User deleted successfully!'
    });

    load();

  } catch (err) {

    setToast({
      type: 'error',
      message: err?.response?.data?.message || 'Delete failed'
    });

  }

};

  return (
    <Layout
      title="User Management"
      subtitle="Manage system users and their roles"
      actions={
        <button className="btn btn-primary" onClick={() => setShowForm(s => !s)}>
          {showForm ? '✕ Cancel' : '+ Create User'}
        </button>
      }
    >

      {/* Temp Password Banner */}
      {tempPass && (
        <div style={{
          background: 'linear-gradient(135deg, var(--primary-50), #fff)',
          border: '1px solid var(--primary-200)',
          borderRadius: 'var(--radius-lg)',
          padding: '16px 20px',
          marginBottom: 20,
          display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12,
        }}>
          <div>
            <div style={{ fontWeight: 700, color: 'var(--primary-700)', marginBottom: 4 }}>🔑 Temporary Password</div>
            <div style={{ fontSize: 13, color: 'var(--gray-600)' }}>
              Share this with the user. They must change it on first login:
              <code style={{
                marginLeft: 10, fontFamily: 'monospace', fontWeight: 800, fontSize: 15,
                background: 'var(--primary-100)', color: 'var(--primary-800)',
                padding: '3px 10px', borderRadius: 6,
              }}>{tempPass}</code>
            </div>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={() => setTempPass(null)}>✕ Dismiss</button>
        </div>
      )}

      {/* Create User Form */}
      {showForm && (
        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-header">
            <div className="card-title">➕ Create New User</div>
          </div>
          <div className="card-body">
            <form onSubmit={submit}>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Username <span className="required">*</span></label>
                  <input className="form-input" required value={form.username}
                    onChange={e => setForm({...form, username: e.target.value})}
                    placeholder="e.g. john.doe" />
                </div>
                <div className="form-group">
                  <label className="form-label">Full Name <span className="required">*</span></label>
                  <input className="form-input" required value={form.fullName}
                    onChange={e => setForm({...form, fullName: e.target.value})}
                    placeholder="e.g. John Doe" />
                </div>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input className="form-input" type="email" value={form.email}
                    onChange={e => setForm({...form, email: e.target.value})}
                    placeholder="john@college.edu" />
                </div>
                <div className="form-group">
                  <label className="form-label">Phone</label>
                  <input className="form-input" value={form.phone}
                    onChange={e => setForm({...form, phone: e.target.value})}
                    placeholder="+91 9876543210" />
                </div>
              </div>

              <div className="form-group" style={{ marginTop: 16 }}>
                <label className="form-label">Assign Roles <span className="required">*</span></label>
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginTop: 6 }}>
                  {ROLE_OPTIONS.map(role => (
                    <label key={role} style={{
                      display: 'flex', alignItems: 'center', gap: 6,
                      padding: '7px 14px',
                      border: `1.5px solid ${form.roleNames.includes(role) ? 'var(--primary-400)' : 'var(--gray-300)'}`,
                      borderRadius: 'var(--radius-md)',
                      background: form.roleNames.includes(role) ? 'var(--primary-50)' : '#fff',
                      cursor: 'pointer',
                      transition: 'all 0.15s',
                      fontSize: 13, fontWeight: 500,
                      color: form.roleNames.includes(role) ? 'var(--primary-700)' : 'var(--gray-600)',
                    }}>
                      <input type="checkbox" style={{ display: 'none' }}
                        checked={form.roleNames.includes(role)}
                        onChange={() => toggleRole(role)} />
                      {form.roleNames.includes(role) ? '✅' : '⬜'} {role.replace(/_/g,' ')}
                    </label>
                  ))}
                </div>
              </div>

              <div className="form-actions">
                <button className="btn btn-primary" type="submit" disabled={loading}>
                  {loading ? '⏳ Creating...' : '✅ Create User'}
                </button>
                <button className="btn btn-ghost" type="button" onClick={() => setShowForm(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Users Table */}
      <div className="card">
        <div className="card-header">
          <div>
            <div className="card-title">👥 All Users</div>
            <div className="card-subtitle">{users.length} users found</div>
          </div>
          <div className="search-wrapper">
            <span className="search-icon">🔍</span>
            <input className="search-input" placeholder="Search users..."
              value={search} onChange={e => setSearch(e.target.value)} />
          </div>
        </div>
        <DataTable
          columns={[
            { key:'fullName', header:'Name', render: u => (
              <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                <div style={{
                  width:32, height:32, borderRadius:'50%',
                  background:'linear-gradient(135deg, var(--primary-400), var(--accent-400))',
                  display:'flex', alignItems:'center', justifyContent:'center',
                  fontSize:13, fontWeight:700, color:'#fff', flexShrink:0,
                }}>
                  {u.fullName?.charAt(0)}
                </div>
                <div>
                  <div style={{ fontWeight:600, fontSize:13.5 }}>{u.fullName}</div>
                  <div style={{ fontSize:12, color:'var(--gray-400)' }}>{u.email}</div>
                </div>
              </div>
            )},
            { key:'username', header:'Username', render: u => (
              <code style={{ background:'var(--gray-100)', padding:'2px 8px', borderRadius:4, fontSize:12.5 }}>{u.username}</code>
            )},
            { key:'roles', header:'Roles', render: u =>
              u.roles?.map(r => (
                <span key={r} style={{
                  display:'inline-block', marginRight:4,
                  background:'var(--primary-100)', color:'var(--primary-700)',
                  fontSize:11, fontWeight:600, padding:'2px 8px', borderRadius:99,
                }}>
                  {r.replace(/_/g,' ')}
                </span>
              ))
            },
            { key:'status', header:'Status', render: u => <Badge value={u.status} /> },
            { key:'actions', header:'Actions', render: u => (
              <div className="flex gap-2">
                <button className={`btn btn-sm ${u.status==='ACTIVE'?'btn-danger':'btn-success'}`}
                  onClick={() => toggleStatus(u)}>
                  {u.status==='ACTIVE'?'🔒 Deactivate':'🔓 Activate'}
                </button>
                <button className="btn btn-secondary btn-sm" onClick={() => resetPwd(u)}>
                  🔑 Reset Pwd
                </button>
                <button
  className="btn btn-danger btn-sm"
  onClick={() => deleteUser(u)}
>
  🗑 Delete
</button>
              </div>
            )},
          ]}
          rows={users}
          emptyText="No users found. Create one above."
          emptyIcon="👤"
        />
      </div>

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
