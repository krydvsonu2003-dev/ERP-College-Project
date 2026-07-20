const COLOR_MAP = {
  ACTIVE: 'green',     PAID: 'green',        PUBLISHED: 'green',
  APPROVED: 'green',   PRESENT: 'green',      STUDENT_CREATED: 'green',
  ADMITTED: 'blue',    GRADUATED: 'purple',
  DUE: 'amber',        PARTIALLY_PAID: 'amber', SUBMITTED: 'amber',
  UNDER_REVIEW: 'amber', PENDING: 'amber',    LATE: 'amber',  SCHEDULED: 'amber',
  REJECTED: 'red',     OVERDUE: 'red',        SUSPENDED: 'red',
  ABSENT: 'red',       INACTIVE: 'red',       LOCKED: 'red',  WITHDRAWN: 'red',
  ONGOING: 'blue',     MARKS_ENTRY: 'blue',   COMPLETED: 'green',
  EXCUSED: 'purple',   POSTED: 'green',       REVERSED: 'red',
};

export default function Badge({ value }) {
  if (!value) return null;
  const color = COLOR_MAP[value] || 'gray';
  return (
    <span className={`badge badge-${color}`}>
      <span className="badge-dot" />
      {String(value).replace(/_/g, ' ')}
    </span>
  );
}
