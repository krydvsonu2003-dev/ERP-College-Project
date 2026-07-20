import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import StatCard from '../../components/StatCard';
import DataTable from '../../components/DataTable';
import axiosClient from '../../api/axiosClient';
import { Link } from 'react-router-dom';

export default function FacultyDashboard() {
  const [data, setData] = useState(null);

  useEffect(() => {
    axiosClient.get('/dashboard/faculty').then(res => setData(res.data.data));
  }, []);

  const today = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

  return (
    <Layout
      title="Faculty Dashboard"
      subtitle={today}
      actions={
        <div className="flex gap-2">
          <Link to="/attendance" className="btn btn-primary">✅ Mark Attendance</Link>
          <Link to="/exams/marks-entry" className="btn btn-secondary">📊 Enter Marks</Link>
        </div>
      }
    >
      <div className="stats-grid">
        <StatCard label="Assigned Classes" value={data?.assignedClassCount ?? 0} icon="🏫" color="blue" />
        <StatCard label="Today's Date" value={new Date().getDate()} icon="📅" color="purple" />
      </div>

      <div className="card">
        <div className="card-header">
          <div>
            <div className="card-title">📚 My Subject Assignments</div>
            <div className="card-subtitle">Classes you are assigned to teach</div>
          </div>
        </div>
        <DataTable
          columns={[
            { key: 'subjectName', header: 'Subject' },
            { key: 'semester', header: 'Semester', render: r => `Semester ${r.semester}` },
            { key: 'classSectionId', header: 'Section ID' },
            {
              key: 'actions', header: 'Actions',
              render: () => (
                <div className="flex gap-2">
                  <Link to="/attendance" className="btn btn-success btn-sm">✅ Attendance</Link>
                  <Link to="/exams/marks-entry" className="btn btn-secondary btn-sm">📊 Marks</Link>
                </div>
              )
            },
          ]}
          rows={data?.assignments ?? []}
          emptyText="No subject assignments found"
          emptyIcon="📚"
          rowKey="classSectionId"
        />
      </div>
    </Layout>
  );
}
