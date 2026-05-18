import { useState, useEffect } from "react";
import { getOptimalSchedule } from "./services/api";

export default function SchedulerTab() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadSchedule = async () => {
    setLoading(true);
    setError("");
    try {
      const result = await getOptimalSchedule();
      setData(result);
    } catch (err) {
      setError(`Failed to load schedule: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSchedule();
  }, []);

  const renderTable = (reservations, title, emptyMsg, color) => (
    <div style={{ marginBottom: 24 }}>
      <h3 style={{ color, marginBottom: 12 }}>{title} ({reservations.length})</h3>
      {reservations.length === 0 ? (
        <p style={{ color: "#94a3b8" }}>{emptyMsg}</p>
      ) : (
        <div style={{ overflowX: "auto" }}>
          <table style={{ width: "100%", borderCollapse: "collapse", background: "#1e293b", borderRadius: 8, overflow: "hidden" }}>
            <thead>
              <tr style={{ background: "#334155", color: "#e2e8f0", textAlign: "left" }}>
                <th style={{ padding: "12px 16px" }}>Vehicle ID</th>
                <th style={{ padding: "12px 16px" }}>Type</th>
                <th style={{ padding: "12px 16px" }}>Arrival</th>
                <th style={{ padding: "12px 16px" }}>Departure</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((res, i) => (
                <tr key={i} style={{ borderBottom: i < reservations.length - 1 ? "1px solid #334155" : "none" }}>
                  <td style={{ padding: "12px 16px", color: "#f1f5f9" }}>{res.vehicleId}</td>
                  <td style={{ padding: "12px 16px", color: "#cbd5e1" }}>{res.type}</td>
                  <td style={{ padding: "12px 16px", color: "#94a3b8" }}>{res.arrival}</td>
                  <td style={{ padding: "12px 16px", color: "#94a3b8" }}>{res.departure}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  return (
    <div>
      <h2 style={{ color: "#a855f7", marginBottom: 12 }}>Reservation Scheduler (Dynamic Programming)</h2>
      <p style={{ color: "#94a3b8", marginBottom: 20 }}>
        Optimal interval scheduling to maximize capacity. Resolves overlapping conflicts by refusing some reservations.
      </p>

      {error && (
        <div style={{ background: "#451a1a", border: "1px solid #ef4444", color: "#fecaca", padding: "12px 16px", borderRadius: 8, marginBottom: 16, fontSize: 13 }}>
          {error}
        </div>
      )}

      {loading && !data ? (
        <p style={{ color: "#94a3b8" }}>Loading schedule...</p>
      ) : data ? (
        <div>
          <div style={{ display: "flex", gap: 20, marginBottom: 24, background: "#1e293b", padding: 16, borderRadius: 8, border: "1px solid #334155" }}>
            <div>
              <span style={{ color: "#94a3b8", display: "block", fontSize: 13, marginBottom: 4 }}>Total Requests</span>
              <span style={{ color: "#f1f5f9", fontSize: 24, fontWeight: "bold" }}>{data.totalReservations}</span>
            </div>
            <div>
              <span style={{ color: "#94a3b8", display: "block", fontSize: 13, marginBottom: 4 }}>Accepted (Optimal)</span>
              <span style={{ color: "#22c55e", fontSize: 24, fontWeight: "bold" }}>{data.optimalCount}</span>
            </div>
            <div>
              <span style={{ color: "#94a3b8", display: "block", fontSize: 13, marginBottom: 4 }}>Refused (Conflicts)</span>
              <span style={{ color: "#ef4444", fontSize: 24, fontWeight: "bold" }}>{data.rejected ? data.rejected.length : (data.totalReservations - data.optimalCount)}</span>
            </div>
            <div style={{ marginLeft: "auto", display: "flex", alignItems: "center" }}>
              <button 
                onClick={loadSchedule}
                style={{ padding: "8px 16px", borderRadius: 6, background: "#334155", color: "#fff", border: "none", cursor: "pointer" }}
              >
                Refresh
              </button>
            </div>
          </div>

          {renderTable(data.selected, "Accepted Reservations", "No reservations accepted.", "#22c55e")}
          {renderTable(data.rejected || [], "Refused Reservations (Conflicts)", "No conflicts found! All reservations accepted.", "#ef4444")}
        </div>
      ) : null}
    </div>
  );
}
