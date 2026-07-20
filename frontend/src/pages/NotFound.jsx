import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'var(--gray-50)',
      gap: 16,
    }}>
      <div style={{
        fontSize: 80,
        background: 'linear-gradient(135deg, var(--primary-700), var(--primary-500))',
        WebkitBackgroundClip: 'text',
        WebkitTextFillColor: 'transparent',
        fontWeight: 900,
        lineHeight: 1,
      }}>404</div>

      <div style={{ fontSize: 20, fontWeight: 700, color: 'var(--gray-700)' }}>
        Page Not Found
      </div>
      <div style={{ fontSize: 14, color: 'var(--gray-400)', maxWidth: 300, textAlign: 'center' }}>
        The page you're looking for doesn't exist or you don't have permission to access it.
      </div>
      <Link to="/" className="btn btn-primary" style={{ marginTop: 8 }}>
        🏠 Back to Dashboard
      </Link>
    </div>
  );
}
