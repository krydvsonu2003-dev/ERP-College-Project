import { useAuth } from '../../context/AuthContext';
import PrincipalDashboard  from './PrincipalDashboard';
import FacultyDashboard    from './FacultyDashboard';
import StudentDashboard    from './StudentDashboard';
import AccountantDashboard from './AccountantDashboard';

export default function DashboardRouter() {
  const { primaryRole } = useAuth();
  switch (primaryRole) {
    case 'SUPER_ADMIN':
    case 'PRINCIPAL':
    case 'HOD':           return <PrincipalDashboard />;
    case 'FACULTY':       return <FacultyDashboard />;
    case 'STUDENT':       return <StudentDashboard />;
    case 'ACCOUNTANT':    return <AccountantDashboard />;
    default:              return <PrincipalDashboard />;
  }
}
