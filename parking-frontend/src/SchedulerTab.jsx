import { useState, useEffect } from "react";
import { getOptimalSchedule, addReservation, clearReservations } from "./services/api";

export default function SchedulerTab() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [vehicleId, setVehicleId] = useState("");
  const [type, setType] = useState("COMPACT");
  const [arrival, setArrival] = useState("08:00");
  const [departure, setDeparture] = useState("09:00");

  const handleAddReservation = async (e) => {
    e.preventDefault();
    if (!vehicleId || !arrival || !departure) return;
    try {
      setLoading(true);
      await addReservation({ vehicleId, type, arrival, departure });
      await loadSchedule();
      setVehicleId("");
    } catch (err) {
      setError(`Failed to add reservation: ${err.message}`);
      setLoading(false);
    }
  };

  const handleClearReservations = async () => {
    try {
      setLoading(true);
      await clearReservations();
      await loadSchedule();
    } catch (err) {
      setError(`Failed to clear reservations: ${err.message}`);
      setLoading(false);
    }
  };

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

  const renderTable = (reservations, title, emptyMsg, color, showSpot = false) => (
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
                {showSpot && <th style={{ padding: "12px 16px" }}>Floor</th>}
                {showSpot && <th style={{ padding: "12px 16px" }}>Spot</th>}
                <th style={{ padding: "12px 16px" }}>Arrival</th>
                <th style={{ padding: "12px 16px" }}>Departure</th>
                <th style={{ padding: "12px 16px" }}>Revenue</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((res, i) => {
                let floorVal = "-";
                let spotVal = "-";
                if (res.spotId) {
                  const parts = res.spotId.split(":");
                  if (parts.length >= 2) {
                    floorVal = parts[0].replace("F", "");
                    spotVal = parts[1];
                  } else {
                    spotVal = res.spotId;
                  }
                }
                return (
                  <tr key={i} style={{ borderBottom: i < reservations.length - 1 ? "1px solid #334155" : "none" }}>
                    <td style={{ padding: "12px 16px", color: "#f1f5f9" }}>{res.vehicleId}</td>
                    <td style={{ padding: "12px 16px", color: "#cbd5e1" }}>{res.type}</td>
                    {showSpot && <td style={{ padding: "12px 16px", color: "#f1f5f9" }}>{floorVal}</td>}
                    {showSpot && <td style={{ padding: "12px 16px", color: "#cbd5e1" }}>{spotVal}</td>}
                    <td style={{ padding: "12px 16px", color: "#94a3b8" }}>{res.arrival}</td>
                    <td style={{ padding: "12px 16px", color: "#94a3b8" }}>{res.departure}</td>
                    <td style={{ padding: "12px 16px", color: "#eab308", fontWeight: "bold" }}>
                      ${res.revenue ? res.revenue.toFixed(2) : "0.00"}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  const renderGanttChart = (reservations, title, isRejected) => {
    if (!reservations || reservations.length === 0) return null;
    
    const START_MINUTES = 8 * 60;
    const TOTAL_MINUTES = 8 * 60;

    const parseTime = (timeStr) => {
      const [h, m] = timeStr.split(":");
      return parseInt(h) * 60 + parseInt(m);
    };

    return (
      <div style={{ marginBottom: 24 }}>
        <h3 style={{ color: isRejected ? "#ef4444" : "#22c55e", marginBottom: 12 }}>{title} Gantt</h3>
        <div style={{ background: "#1e293b", padding: "20px 16px", borderRadius: 8, border: `1px solid ${isRejected ? "#451a1a" : "#14532d"}` }}>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, color: "#94a3b8", fontSize: 12, position: "relative", paddingBottom: 8, borderBottom: "1px solid #334155" }}>
            <span>08:00</span>
            <span>10:00</span>
            <span>12:00</span>
            <span>14:00</span>
            <span>16:00</span>
          </div>
          
          <div style={{ position: "relative" }}>
            {reservations.map((res, i) => {
              const start = parseTime(res.arrival);
              const end = parseTime(res.departure);
              
              const leftPercent = ((start - START_MINUTES) / TOTAL_MINUTES) * 100;
              const widthPercent = ((end - start) / TOTAL_MINUTES) * 100;
              
              return (
                <div key={i} style={{ 
                  height: 36, 
                  marginBottom: 8, 
                  background: isRejected ? "#ef444420" : "#22c55e20",
                  borderRadius: 4,
                  position: "relative",
                  display: "flex",
                  alignItems: "center"
                }}>
                  <div style={{
                    position: "absolute",
                    left: `${Math.max(0, leftPercent)}%`,
                    width: `${Math.min(100 - leftPercent, widthPercent)}%`,
                    height: "100%",
                    background: isRejected ? "#ef4444" : "#22c55e",
                    borderRadius: 4,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: "#fff",
                    fontSize: 12,
                    fontWeight: "bold",
                    overflow: "hidden",
                    whiteSpace: "nowrap",
                    textOverflow: "ellipsis",
                    padding: "0 8px",
                    boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
                    transition: "all 0.2s ease"
                  }}
                  title={`${res.vehicleId} | ${res.arrival} - ${res.departure} | $${res.revenue?.toFixed(2)}`}
                  >
                    {res.vehicleId} • ${res.revenue?.toFixed(2)}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div>
      <h2 style={{ color: "#38bdf8", marginBottom: 12 }}>Reservation Scheduler (Dynamic Programming)</h2>
      <p style={{ color: "#94a3b8", marginBottom: 20 }}>
        Optimal interval scheduling to maximize revenue. Resolves overlapping conflicts by refusing some reservations to maximize total revenue.
      </p>

      {error && (
        <div style={{ background: "#451a1a", border: "1px solid #ef4444", color: "#fecaca", padding: "12px 16px", borderRadius: 8, marginBottom: 16, fontSize: 13 }}>
          {error}
        </div>
      )}

      <div style={{ background: "#1e293b", padding: 20, borderRadius: 8, border: "1px solid #334155", marginBottom: 24 }}>
        <h3 style={{ color: "#f1f5f9", marginBottom: 16 }}>Add New Reservation</h3>
        <form onSubmit={handleAddReservation} style={{ display: "flex", gap: 16, flexWrap: "wrap", alignItems: "flex-end" }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, flex: 1, minWidth: 150 }}>
            <label style={{ color: "#94a3b8", fontSize: 13 }}>Vehicle ID</label>
            <input 
              type="text" 
              value={vehicleId} 
              onChange={(e) => setVehicleId(e.target.value)} 
              placeholder="e.g. V-123"
              style={{ padding: "8px 12px", borderRadius: 6, background: "#0f172a", border: "1px solid #334155", color: "#f1f5f9" }}
              required 
            />
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, flex: 1, minWidth: 150 }}>
            <label style={{ color: "#94a3b8", fontSize: 13 }}>Type</label>
            <select 
              value={type} 
              onChange={(e) => setType(e.target.value)}
              style={{ padding: "8px 12px", borderRadius: 6, background: "#0f172a", border: "1px solid #334155", color: "#f1f5f9" }}
            >
              <option value="COMPACT">Compact</option>
              <option value="STANDARD">Standard</option>
              <option value="SUV">SUV</option>
              <option value="TRUCK">Truck</option>
            </select>
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, flex: 1, minWidth: 120 }}>
            <label style={{ color: "#94a3b8", fontSize: 13 }}>Arrival Time</label>
            <input 
              type="time" 
              value={arrival} 
              onChange={(e) => setArrival(e.target.value)}
              style={{ padding: "8px 12px", borderRadius: 6, background: "#0f172a", border: "1px solid #334155", color: "#f1f5f9" }}
              required 
            />
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, flex: 1, minWidth: 120 }}>
            <label style={{ color: "#94a3b8", fontSize: 13 }}>Departure Time</label>
            <input 
              type="time" 
              value={departure} 
              onChange={(e) => setDeparture(e.target.value)}
              style={{ padding: "8px 12px", borderRadius: 6, background: "#0f172a", border: "1px solid #334155", color: "#f1f5f9" }}
              required 
            />
          </div>
          <button 
            type="submit" 
            style={{ padding: "9px 20px", borderRadius: 6, background: "#8b5cf6", color: "#fff", border: "none", cursor: "pointer", fontWeight: "bold", transition: "all 0.2s" }}
            onMouseOver={(e) => e.target.style.background = "#7c3aed"}
            onMouseOut={(e) => e.target.style.background = "#8b5cf6"}
          >
            Add
          </button>
        </form>
      </div>

      {loading && !data ? (
        <div style={{ display: "flex", justifyContent: "center", padding: 40, color: "#94a3b8" }}>
          <div style={{ fontSize: 18, display: "flex", alignItems: "center", gap: 10 }}>
            <span style={{ fontSize: 24 }}>⏳</span> Loading schedule...
          </div>
        </div>
      ) : data ? (
        <div>
          <div style={{ display: "flex", gap: 20, marginBottom: 24, background: "#1e293b", padding: 16, borderRadius: 8, border: "1px solid #334155", flexWrap: "wrap" }}>
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
            <div>
              <span style={{ color: "#94a3b8", display: "block", fontSize: 13, marginBottom: 4 }}>Max Revenue</span>
              <span style={{ color: "#eab308", fontSize: 24, fontWeight: "bold" }}>${data.totalRevenue?.toFixed(2) || "0.00"}</span>
            </div>
            <div style={{ marginLeft: "auto", display: "flex", alignItems: "center" }}>
              <button 
                onClick={handleClearReservations}
                style={{ padding: "8px 16px", borderRadius: 6, background: "#ef4444", color: "#fff", border: "none", cursor: "pointer", transition: "all 0.2s", fontWeight: "bold", marginRight: 12 }}
                onMouseOver={(e) => e.target.style.background = "#dc2626"}
                onMouseOut={(e) => e.target.style.background = "#ef4444"}
              >
                Clear All
              </button>
              <button 
                onClick={loadSchedule}
                style={{ padding: "8px 16px", borderRadius: 6, background: "#334155", color: "#fff", border: "none", cursor: "pointer", transition: "all 0.2s", fontWeight: "bold" }}
                onMouseOver={(e) => e.target.style.background = "#475569"}
                onMouseOut={(e) => e.target.style.background = "#334155"}
              >
                Refresh Data
              </button>
            </div>
          </div>

          {renderGanttChart(data.selected, "Accepted Schedule", false)}
          {renderGanttChart(data.rejected, "Rejected Reservations", true)}

          {renderTable(data.selected, "Accepted Reservations", "No reservations accepted.", "#22c55e", true)}
          {renderTable(data.rejected || [], "Refused Reservations (Conflicts)", "No conflicts found! All reservations accepted.", "#ef4444")}
        </div>
      ) : null}
    </div>
  );
}
