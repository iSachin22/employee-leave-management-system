import { useEffect, useState } from 'react';
import axios from 'axios';

const fallbackData = [
  {
    id: 1,
    employeeName: 'Sachin Rathod',
    leaveType: 'Annual Leave',
    startDate: '2026-09-20',
    endDate: '2026-09-22',
    status: 'PENDING_MANAGER',
    days: 3,
  },
  {
    id: 2,
    employeeName: 'Aarav Patel',
    leaveType: 'Sick Leave',
    startDate: '2026-09-25',
    endDate: '2026-09-25',
    status: 'APPROVED',
    days: 1,
  },
];

function App() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    employeeId: 1,
    leaveType: 'Annual Leave',
    startDate: '2026-09-20',
    endDate: '2026-09-22',
    reason: 'Family function',
  });

  const loadRequests = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/leave/employee/1');
      setRequests(response.data);
      setError('');
    } catch (err) {
      setRequests(fallbackData);
      setError('Backend is not running yet. Showing demo data for UI preview.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post(`/api/leave/apply/${form.employeeId}`, {
        leaveType: form.leaveType,
        startDate: form.startDate,
        endDate: form.endDate,
        reason: form.reason,
      });
      await loadRequests();
      setForm({
        employeeId: 1,
        leaveType: 'Annual Leave',
        startDate: '2026-09-20',
        endDate: '2026-09-22',
        reason: '',
      });
    } catch (err) {
      setError('Could not submit leave request. Check the backend server first.');
    }
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">HR Portal</p>
          <h1>Employee Leave Management</h1>
        </div>
        <button className="primary-btn" onClick={loadRequests}>Refresh</button>
      </header>

      <section className="stats-grid">
        <div className="stat-card">
          <span>Total Requests</span>
          <strong>{requests.length}</strong>
        </div>
        <div className="stat-card">
          <span>Approved</span>
          <strong>{requests.filter((r) => r.status === 'APPROVED').length}</strong>
        </div>
        <div className="stat-card">
          <span>Pending</span>
          <strong>{requests.filter((r) => r.status === 'PENDING_MANAGER' || r.status === 'PENDING_HR').length}</strong>
        </div>
      </section>

      <section className="content-grid">
        <form className="panel" onSubmit={handleSubmit}>
          <h2>Apply Leave</h2>
          <label>
            Employee ID
            <input
              type="number"
              value={form.employeeId}
              onChange={(e) => setForm({ ...form, employeeId: Number(e.target.value) })}
            />
          </label>
          <label>
            Leave Type
            <input
              type="text"
              value={form.leaveType}
              onChange={(e) => setForm({ ...form, leaveType: e.target.value })}
            />
          </label>
          <div className="two-column">
            <label>
              Start Date
              <input
                type="date"
                value={form.startDate}
                onChange={(e) => setForm({ ...form, startDate: e.target.value })}
              />
            </label>
            <label>
              End Date
              <input
                type="date"
                value={form.endDate}
                onChange={(e) => setForm({ ...form, endDate: e.target.value })}
              />
            </label>
          </div>
          <label>
            Reason
            <textarea
              rows="3"
              value={form.reason}
              onChange={(e) => setForm({ ...form, reason: e.target.value })}
            />
          </label>
          <button className="submit-btn" type="submit">Submit Request</button>
          {error && <p className="error-text">{error}</p>}
        </form>

        <div className="panel">
          <h2>Recent Requests</h2>
          {loading ? (
            <p>Loading...</p>
          ) : (
            <div className="request-list">
              {requests.map((request) => (
                <div className="request-item" key={request.id}>
                  <div>
                    <strong>{request.employeeName || 'Employee'}</strong>
                    <p>{request.leaveType}</p>
                  </div>
                  <div className="meta">
                    <span>{request.startDate}</span>
                    <span>{request.days} days</span>
                  </div>
                  <span className={`status status-${request.status}`}>
                    {request.status.replace('_', ' ')}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}

export default App;
