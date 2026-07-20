export default function DataTable({ columns, rows = [], emptyText = 'No data found', emptyIcon = '📭', rowKey = 'id' }) {
  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            <th style={{ width: 48 }}>#</th>
            {columns.map(col => <th key={col.key}>{col.header}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length + 1}>
                <div className="table-empty">
                  <div className="table-empty-icon">{emptyIcon}</div>
                  <div className="table-empty-text">{emptyText}</div>
                </div>
              </td>
            </tr>
          ) : rows.map((row, idx) => (
            <tr key={row[rowKey] ?? idx}>
              <td style={{ color: 'var(--gray-400)', fontWeight: 600, fontSize: 12 }}>{idx + 1}</td>
              {columns.map(col => (
                <td key={col.key}>{col.render ? col.render(row) : (row[col.key] ?? '—')}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
