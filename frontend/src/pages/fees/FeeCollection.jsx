import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import DataTable from '../../components/DataTable';
import Badge from '../../components/Badge';
import Toast from '../../components/Toast';
import axiosClient from '../../api/axiosClient';

export default function FeeCollection() {
  const [students, setStudents] = useState([]);
  const [dueInvoices, setDueInvoices] = useState([]);
  const [academicYears, setAcademicYears] = useState([]);
  const [toast, setToast] = useState(null);
  const [receipt, setReceipt] = useState(null);
  const [invoice, setInvoice] = useState(null);
  const [activeTab, setActiveTab] = useState('collect');

  // Invoice form
  const [invForm, setInvForm] = useState({ studentId:'', academicYearId:'', semester:1 });
  // Payment form
  const [payForm, setPayForm] = useState({ invoiceId:'', amount:'', paymentMode:'CASH', paymentReference:'' });

  const loadDues = () =>
    axiosClient.get('/fees/dues').then(res => setDueInvoices(res.data.data ?? []));

 useEffect(() => {
  loadDues();

  axiosClient.get('/master/academic-years')
    .then(res => setAcademicYears(res.data.data ?? []));

  axiosClient.get('/students')
    .then(res => {
      setStudents(res.data.data.content ?? []);
    });

}, []);
 

 const generateInvoice = async e => {
  e.preventDefault();
  try {

    const res = await axiosClient.post('/fees/invoices', invForm);

    const invoice = res.data.data;
    setInvoice(invoice);
    setReceipt(null);

    setToast({
      type: 'success',
      message:
        `Invoice Generated Successfully!
Invoice ID: ${invoice.id}
Invoice Number: ${invoice.invoiceNumber}
Student Name: ${invoice.studentName}
Student ID: ${invoice.studentId}
Total Amount: ₹${invoice.totalAmount}
Status: ${invoice.status}`
    });

    setInvForm({ studentId:'', academicYearId:'', semester:1 });

    loadDues();

  } catch (err) {
    setToast({
      type:'error',
      message: err?.response?.data?.message || 'Failed to generate invoice'
    });
  }
};

  const collectPayment = async e => {
    e.preventDefault();
    setReceipt(null);
    try {
      const res = await axiosClient.post('/fees/payments', {
        ...payForm, amount: Number(payForm.amount),
      });
      setReceipt(res.data.data);
      setPayForm({ invoiceId:'', amount:'', paymentMode:'CASH', paymentReference:'' });
      setToast({ type:'success', message:'Payment collected! Receipt generated.' });
      loadDues();
    } catch (err) {
      setToast({ type:'error', message: err?.response?.data?.message || 'Failed to record payment' });
    }
  };

  const tabs = [
    { id:'collect', label:'💰 Collect Payment' },
    { id:'invoice', label:'🧾 Generate Invoice' },
    { id:'dues', label:`⚠️ Due List (${dueInvoices.length})` },
  ];

  return (
    <Layout title="Fee Management" subtitle="Collection, invoices and outstanding dues">

      {/* Receipt Banner */}
      {receipt && (
        <div style={{
          background:'linear-gradient(135deg, #d1fae5, #a7f3d0)',
          border:'1px solid var(--success)',
          borderRadius:'var(--radius-lg)',
          padding:'20px 24px',
          marginBottom:24,
          display:'flex', alignItems:'center', justifyContent:'space-between',
        }}>
          <div>
            <div style={{ fontWeight:800, fontSize:16, color:'#065f46', marginBottom:6 }}>
              ✅ Payment Collected Successfully!
            </div>
            <div style={{ fontSize:13.5, color:'#047857', lineHeight:1.6 }}>
              Receipt: <strong>{receipt.receiptNumber}</strong> &nbsp;|&nbsp;
              Student: <strong>{receipt.studentName} ({receipt.studentCode})</strong> &nbsp;|&nbsp;
              Amount: <strong>₹{Number(receipt.amount).toLocaleString('en-IN')}</strong> via <strong>{receipt.paymentMode}</strong><br/>
              Remaining Due: <strong>₹{Number(receipt.invoiceDueAmountAfter).toLocaleString('en-IN')}</strong> · Status: <strong>{receipt.invoiceStatusAfter}</strong>
            </div>
          </div>
          <div className="flex gap-2">
            <button className="btn btn-success btn-sm" onClick={() => window.print()}>🖨️ Print</button>
            <button className="btn btn-ghost btn-sm" onClick={() => setReceipt(null)}>✕</button>
          </div>
        </div>
      )}
      {invoice && (
  <div
    style={{
      background: "#eefbf3",
      border: "1px solid #22c55e",
      borderRadius: 10,
      padding: 20,
      marginBottom: 20
    }}

  >
    <h3>🧾 Invoice Generated Successfully</h3>

    <p><b>Invoice No:</b> {invoice.invoiceNumber}</p>
    <p><b>Invoice ID:</b> {invoice.id}</p>
    <p><b>Student:</b> {invoice.studentName}</p>
    <p><b>Student Code:</b> {invoice.studentCode}</p>
    <p><b>Semester:</b> {invoice.semester}</p>

    <p><b>Total Amount:</b> ₹{invoice.totalAmount}</p>
    <p><b>Paid:</b> ₹{invoice.paidAmount}</p>
    <p><b>Due:</b> ₹{invoice.dueAmount}</p>

    <p><b>Status:</b> {invoice.status}</p>
     <div style={{ marginTop: 20 }}>
      <button
        className="btn btn-primary"
        onClick={() => window.print()}
      >
        🖨️ Print Invoice
      </button>

      <button
        className="btn btn-secondary"
        style={{ marginLeft: 10 }}
        onClick={() => setInvoice(null)}
      >
        ✖ Close
      </button>
    </div>

  </div>
)}

      {/* Tabs */}
      <div style={{ display:'flex', gap:0, marginBottom:24, background:'var(--gray-100)', borderRadius:'var(--radius-md)', padding:4 }}>
        {tabs.map(tab => (
          <button key={tab.id} onClick={() => setActiveTab(tab.id)} style={{
            flex:1, padding:'9px 16px',
            borderRadius:'var(--radius-sm)',
            border:'none', cursor:'pointer',
            background: activeTab===tab.id ? '#fff' : 'transparent',
            color: activeTab===tab.id ? 'var(--gray-900)' : 'var(--gray-500)',
            fontWeight: activeTab===tab.id ? 700 : 500,
            fontSize:13.5,
            boxShadow: activeTab===tab.id ? 'var(--shadow-sm)' : 'none',
            transition:'all 0.15s',
          }}>{tab.label}</button>
        ))}
      </div>

      {/* Collect Payment Tab */}
      {activeTab === 'collect' && (
        <div className="card">
          <div className="card-header"><div className="card-title">💰 Record a Payment</div></div>
          <div className="card-body">
            <form onSubmit={collectPayment}>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Invoice ID <span className="required">*</span></label>
                  <input className="form-input" required placeholder="e.g. 42"
                    value={payForm.invoiceId} onChange={e => setPayForm({...payForm, invoiceId: e.target.value})} />
                  <span className="form-helper">Find the Invoice ID from the Due List tab</span>
                </div>
                <div className="form-group">
                  <label className="form-label">Amount (₹) <span className="required">*</span></label>
                  <input className="form-input" type="number" required min="1" placeholder="e.g. 5000"
                    value={payForm.amount} onChange={e => setPayForm({...payForm, amount: e.target.value})} />
                </div>
                <div className="form-group">
                  <label className="form-label">Payment Mode <span className="required">*</span></label>
                  <select className="form-select" value={payForm.paymentMode}
                    onChange={e => setPayForm({...payForm, paymentMode: e.target.value})}>
                    {['CASH','CARD','UPI','NEFT','CHEQUE','ONLINE'].map(m => (
                      <option key={m} value={m}>{m}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Reference No.</label>
                  <input className="form-input" placeholder="Cheque/UPI/Transaction ref."
                    value={payForm.paymentReference} onChange={e => setPayForm({...payForm, paymentReference: e.target.value})} />
                </div>
              </div>
              <div className="form-actions">
                <button className="btn btn-primary" type="submit">💰 Collect & Generate Receipt</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Generate Invoice Tab */}
      {activeTab === 'invoice' && (
        <div className="card">
          <div className="card-header"><div className="card-title">🧾 Generate Fee Invoice</div></div>
          <div className="card-body">
            <form onSubmit={generateInvoice}>
              <div className="form-grid-3">
                <div className="form-group">
  <label className="form-label">
    Student <span className="required">*</span>
  </label>

  <select
    className="form-select"
    required
    value={invForm.studentId}
    onChange={e =>
      setInvForm({
        ...invForm,
        studentId: e.target.value
      })
    }
  >
    <option value="">Select Student</option>

    {students.map(student => (
      <option key={student.id} value={student.id}>
        {student.studentCode} - {student.fullName}
      </option>
    ))}
  </select>
</div>
                <div className="form-group">
                  <label className="form-label">Academic Year <span className="required">*</span></label>
                  <select className="form-select" required value={invForm.academicYearId}
                    onChange={e => setInvForm({...invForm, academicYearId: e.target.value})}>
                    <option value="">Select Year</option>
                    {academicYears.map(y => <option key={y.id} value={y.id}>{y.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Semester <span className="required">*</span></label>
                  <input className="form-input" type="number" min="1" max="8" required
                    value={invForm.semester} onChange={e => setInvForm({...invForm, semester: e.target.value})} />
                </div>
              </div>
              <div className="form-actions">
                <button className="btn btn-primary" type="submit">🧾 Generate Invoice</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Due List Tab */}
      {activeTab === 'dues' && (
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">⚠️ Outstanding Fee Dues</div>
              <div className="card-subtitle">{dueInvoices.length} invoices with pending amounts</div>
            </div>
          </div>
          <DataTable
            columns={[
              { key:'id', header:'Invoice ID', render: i => (
                <code style={{ fontFamily:'monospace', fontWeight:700, color:'var(--primary-600)' }}>#{i.id}</code>
              )},
              { key:'studentCode', header:'Student', render: i => (
                <div>
                  <div style={{ fontWeight:600 }}>{i.studentName}</div>
                  <div style={{ fontSize:12, color:'var(--gray-400)' }}>{i.studentCode}</div>
                </div>
              )},
              { key:'semester', header:'Semester', render: i => `Semester ${i.semester}` },
              { key:'totalAmount', header:'Total', render: i => (
                <span style={{ fontWeight:700 }}>₹{Number(i.totalAmount).toLocaleString('en-IN')}</span>
              )},
              { key:'paidAmount', header:'Paid', render: i => (
                <span style={{ color:'var(--success)', fontWeight:600 }}>₹{Number(i.paidAmount).toLocaleString('en-IN')}</span>
              )},
              { key:'dueAmount', header:'Due', render: i => (
                <span style={{ color:'var(--danger)', fontWeight:700 }}>₹{Number(i.dueAmount).toLocaleString('en-IN')}</span>
              )},
              { key:'status', header:'Status', render: i => <Badge value={i.status} /> },
              { key:'dueDate', header:'Due Date', render: i => (
                <span style={{ fontSize:12.5, color:i.dueDate && new Date(i.dueDate) < new Date() ? 'var(--danger)' : 'var(--gray-500)' }}>
                  {i.dueDate || '—'}
                </span>
              )},
            ]}
            rows={dueInvoices}
            emptyText="No pending dues"
            emptyIcon="✅"
          />
        </div>
      )}

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </Layout>
  );
}
