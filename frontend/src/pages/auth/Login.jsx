import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(err?.response?.data?.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-bg-shapes" />

      {/* Left Panel */}
      <div className="login-left">
        <div className="login-brand">
          Nabarangpur<br /><span>Pharmacy</span> College
        </div>
        <p className="login-tagline">
          Complete ERP system for managing admissions, attendance, examinations,
          and fees — all in one place.
        </p>

        <div className="login-features">
          <div className="login-feature">
            <div className="login-feature-icon">🎓</div>
            <div className="login-feature-text">
              <strong>Academic Management</strong>
              Admissions, attendance, exams and results
            </div>
          </div>
          <div className="login-feature">
            <div className="login-feature-icon">💰</div>
            <div className="login-feature-text">
              <strong>Fee Management</strong>
              Collections, receipts and due tracking
            </div>
          </div>
          <div className="login-feature">
            <div className="login-feature-icon">🔐</div>
            <div className="login-feature-text">
              <strong>Role-Based Access</strong>
              Secure access for every staff member
            </div>
          </div>
        </div>
      </div>

      {/* Right Panel - Login Form */}
      <div className="login-right">
        <div className="login-card">
          <div className="login-header">
            <div style={{ fontSize: 32, marginBottom: 12 }}>💊</div>
            <h2>Welcome back</h2>
            <p>Sign in to your ERP account to continue</p>
          </div>

          {error && (
            <div className="alert alert-error" style={{ marginBottom: 16 }}>
              ⚠️ {error}
            </div>
          )}

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input
                className="form-input"
                placeholder="Enter your username"
                value={username}
                onChange={e => setUsername(e.target.value)}
                autoFocus
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  className="form-input"
                  type={showPass ? 'text' : 'password'}
                  placeholder="Enter your password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  required
                  style={{ paddingRight: 44 }}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  style={{
                    position: 'absolute', right: 12, top: '50%',
                    transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer',
                    fontSize: 16, color: 'var(--gray-400)',
                  }}
                >
                  {showPass ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <button
              className="btn btn-primary login-submit"
              type="submit"
              disabled={loading}
            >
              {loading ? '⏳ Signing in...' : '🔑 Sign In'}
            </button>
          </form>

          <div className="login-footer">
            <div className="divider" />
            Default: <strong>superadmin</strong> / <strong>Admin@12345</strong><br />
            (Change password after first login)
          </div>
        </div>
      </div>
    </div>
  );
}
