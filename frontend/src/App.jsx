import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';

// Auth
import Login from './pages/auth/Login';

// Dashboards
import DashboardRouter from './pages/dashboards/DashboardRouter';

// Admin
import UserManagement from './pages/admin/UserManagement';
import RoleManagement from './pages/admin/RoleManagement';
import AuditLogs      from './pages/admin/AuditLogs';

// Admission
import AdmissionList   from './pages/admission/AdmissionList';
import AdmissionForm   from './pages/admission/AdmissionForm';
import AdmissionDetail from './pages/admission/AdmissionDetail';
import StudentDetail   from './pages/admission/StudentDetail';

// Attendance
import AttendanceEntry from './pages/attendance/AttendanceEntry';
import MyAttendance    from './pages/attendance/MyAttendance';

// Exams
import MarksEntry   from './pages/exam/MarksEntry';
import ResultsView  from './pages/exam/ResultsView';
import MyResults    from './pages/exam/MyResults';

// Fees
import FeeCollection from './pages/fees/FeeCollection';
import MyFees        from './pages/fees/MyFees';

// Misc
import NotFound from './pages/NotFound';

function LoginRoute() {
  const { user } = useAuth();
  return user ? <Navigate to="/" replace /> : <Login />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginRoute />} />

      {/* Protected — all logged-in users */}
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<DashboardRouter />} />

        {/* Super Admin only */}
        <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN']} />}>
          <Route path="/admin/users"      element={<UserManagement />} />
          <Route path="/admin/roles"      element={<RoleManagement />} />
          <Route path="/admin/audit-logs" element={<AuditLogs />} />
        </Route>

        {/* Admissions — admin/staff */}
        <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN','PRINCIPAL','HOD','ADMISSION_OFFICE_STAFF']} />}>
          <Route path="/admissions"      element={<AdmissionList />} />
          <Route path="/admissions/new"  element={<AdmissionForm />} />
          <Route path="/admissions/:id"  element={<AdmissionDetail />} />
          <Route path="/students/:id"    element={<StudentDetail />} />
        </Route>

        {/* Attendance — faculty/admin */}
        <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN','PRINCIPAL','HOD','FACULTY']} />}>
          <Route path="/attendance" element={<AttendanceEntry />} />
        </Route>

        {/* Exams — faculty/admin */}
        <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN','PRINCIPAL','HOD','FACULTY']} />}>
          <Route path="/exams"              element={<ResultsView />} />
          <Route path="/exams/marks-entry"  element={<MarksEntry />} />
        </Route>

        {/* Fees — accountant/admin */}
        <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN','PRINCIPAL','ACCOUNTANT']} />}>
          <Route path="/fees"         element={<FeeCollection />} />
          <Route path="/fees/reports" element={<FeeCollection />} />
        </Route>

        {/* Student self-service */}
        <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
          <Route path="/my/attendance" element={<MyAttendance />} />
          <Route path="/my/results"    element={<MyResults />} />
          <Route path="/my/fees"       element={<MyFees />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
