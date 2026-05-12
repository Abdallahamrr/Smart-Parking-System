import { useState, useEffect } from "react";
import axios from "axios";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, LineChart, Line, ResponsiveContainer
} from "recharts";

const API = "http://localhost:8080/api";

const VEHICLE_TYPES = ["COMPACT", "STANDARD", "SUV", "TRUCK"];
const STRATEGIES = ["firstfit", "bestfit", "ffd", "bruteforce"];

const spotColor = (spot) => {
  if (spot.occupied) return "#ef4444";
  if (spot.reserved) return "#3b82f6";
  if (spot.accessible) return "#f97316";
  return "#22c55e";
};

export default function App() {
  const [tab, setTab] = useState("floorplan");
  const [spots, setSpots] = useState([]);
  const [vehicleType, setVehicleType] = useState("COMPACT");
  const [strategy, setStrategy] = useState("firstfit");
  const [allocationResult, setAllocationResult] = useState(null);
  const [compareResult, setCompareResult] = useState(null);
  const [navZone, setNavZone] = useState("F1-A");
  const [navResult, setNavResult] = useState(null);
  const [reservations, setReservations] = useState(null);
  const [stats, setStats] = useState(null);

const fetchStats = async () => {
    const res = await axios.get(`${API}/stats`);
    setStats(res.data);
};  

  useEffect(() => {
    fetchStatus();
  }, []);

  const fetchStatus = async () => {
    const res = await axios.get(`${API}/status`);
    setSpots(res.data);
  };

  const allocate = async () => {
    const res = await axios.post(
      `${API}/allocate?vehicleType=${vehicleType}&strategy=${strategy}`
    );
    setAllocationResult(res.data);
    fetchStatus();
    fetchStats();
  };

  const compare = async () => {
    const res = await axios.get(`${API}/compare?vehicleType=${vehicleType}`);
    setCompareResult(res.data);
  };

  const navigate = async () => {
    const res = await axios.get(`${API}/navigate/${navZone}`);
    setNavResult(res.data);
  };

  const fetchReservations = async () => {
    const res = await axios.get(`${API}/reservations`);
    setReservations(res.data);
  };

  const releaseSpot = async (spotId) => {
    await axios.post(`${API}/release?spotId=${spotId}`);
    fetchStatus();
  };

  const resetLot = async () => {
  await axios.post(`${API}/reset`);
  fetchStatus();
  fetchStats();
  };

  const floors = [1, 2, 3];

  const compareChartData = compareResult
    ? STRATEGIES.map((s) => ({
        name: s,
        executionTimeNs: compareResult[s]?.executionTimeNs || 0,
        spotFound: compareResult[s]?.spotFound ? 1 : 0,
      }))
    : [];

  return (
    <div style={{ fontFamily: "sans-serif", background: "#0f172a", minHeight: "100vh", color: "#f1f5f9" }}>
      {/* Header */}
      <div style={{ background: "#1e293b", padding: "16px 32px", borderBottom: "1px solid #334155" }}>
        <h1 style={{ margin: 0, color: "#38bdf8", fontSize: "22px" }}>
          Smart Parking Allocation System
        </h1>
        <p style={{ margin: "4px 0 0", color: "#94a3b8", fontSize: "13px" }}>
          Dijkstra • Dynamic Programming • Bin Packing • Brute Force
        </p>
      </div>

      {/* Tabs */}
      <div style={{ display: "flex", gap: "8px", padding: "16px 32px", borderBottom: "1px solid #334155" }}>
        {["floorplan", "allocate", "navigate", "reservations"].map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: "8px 18px",
              borderRadius: "6px",
              border: "none",
              cursor: "pointer",
              background: tab === t ? "#38bdf8" : "#1e293b",
              color: tab === t ? "#0f172a" : "#94a3b8",
              fontWeight: tab === t ? "bold" : "normal",
              textTransform: "capitalize"
            }}
          >
            {t}
          </button>
        ))}
      </div>

      <button
  onClick={resetLot}
  style={{
    marginBottom: "20px",
    padding: "8px 18px",
    borderRadius: "6px",
    background: "#ef4444",
    color: "#fff",
    border: "none",
    cursor: "pointer",
    fontWeight: "bold"
  }}
>
  Reset Lot
</button> 

      <div style={{ padding: "24px 32px" }}>

        {/* FLOOR PLAN */}
        {tab === "floorplan" && (
          <div>
            <h2 style={{ color: "#38bdf8" }}>Floor Plan</h2>
            <div style={{ display: "flex", gap: "16px", marginBottom: "16px" }}>
              {[
                { color: "#22c55e", label: "Available" },
                { color: "#ef4444", label: "Occupied" },
                { color: "#3b82f6", label: "Reserved" },
                { color: "#f97316", label: "Accessible" },
              ].map((l) => (
                <div key={l.label} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <div style={{ width: 14, height: 14, borderRadius: 3, background: l.color }} />
                  <span style={{ fontSize: "13px", color: "#94a3b8" }}>{l.label}</span>
                </div>
              ))}
            </div>
            {floors.map((floor) => (
              <div key={floor} style={{ marginBottom: "24px" }}>
                <h3 style={{ color: "#94a3b8", marginBottom: "10px" }}>Floor {floor}</h3>
                <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
                  {spots
                    .filter((s) => s.floor === floor)
                    .map((spot) => (
                      <div
                        key={spot.id}
                        onClick={() => spot.occupied && releaseSpot(spot.id)}
                        style={{
                          width: "80px",
                          height: "60px",
                          borderRadius: "8px",
                          background: spotColor(spot),
                          display: "flex",
                          flexDirection: "column",
                          alignItems: "center",
                          justifyContent: "center",
                          cursor: spot.occupied ? "pointer" : "default",
                          fontSize: "11px",
                          fontWeight: "bold",
                          color: "#0f172a",
                          border: "2px solid rgba(255,255,255,0.1)"
                        }}
                        title={spot.occupied ? "Click to release" : spot.id}
                      >
                        <span>{spot.id}</span>
                        <span style={{ fontSize: "10px", marginTop: "2px" }}>
                          {spot.occupied ? spot.currentVehicle?.type : spot.maxSize}
                        </span>
                      </div>
                    ))}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* ALLOCATE */}
        {tab === "allocate" && (
          <div>
            <h2 style={{ color: "#38bdf8" }}>Vehicle Allocation</h2>

            <div style={{ display: "flex", gap: "12px", marginBottom: "20px", flexWrap: "wrap" }}>
              <div>
                <label style={{ color: "#94a3b8", fontSize: "13px" }}>Vehicle Type</label>
                <select
                  value={vehicleType}
                  onChange={(e) => setVehicleType(e.target.value)}
                  style={{ display: "block", marginTop: "6px", padding: "8px 12px", borderRadius: "6px", background: "#1e293b", color: "#f1f5f9", border: "1px solid #334155" }}
                >
                  {VEHICLE_TYPES.map((v) => <option key={v}>{v}</option>)}
                </select>
              </div>
              <div>
                <label style={{ color: "#94a3b8", fontSize: "13px" }}>Strategy</label>
                <select
                  value={strategy}
                  onChange={(e) => setStrategy(e.target.value)}
                  style={{ display: "block", marginTop: "6px", padding: "8px 12px", borderRadius: "6px", background: "#1e293b", color: "#f1f5f9", border: "1px solid #334155" }}
                >
                  {STRATEGIES.map((s) => <option key={s}>{s}</option>)}
                </select>
              </div>
            </div>

            <div style={{ display: "flex", gap: "10px", marginBottom: "24px" }}>
              <button onClick={allocate} style={{ padding: "10px 20px", borderRadius: "6px", background: "#38bdf8", color: "#0f172a", border: "none", cursor: "pointer", fontWeight: "bold" }}>
                Allocate Vehicle
              </button>
              <button onClick={compare} style={{ padding: "10px 20px", borderRadius: "6px", background: "#334155", color: "#f1f5f9", border: "none", cursor: "pointer" }}>
                Compare All Strategies
              </button>
            </div>
            {stats && (
    <div style={{ display: "flex", gap: "12px", marginBottom: "20px" }}>
        {[
            { label: "Total Vehicles", value: stats.totalVehicles, color: "#38bdf8" },
            { label: "Served", value: stats.totalServed, color: "#22c55e" },
            { label: "Rejected", value: stats.totalRejections, color: "#ef4444" },
            { label: "Rejection Rate", value: stats.rejectionRate.toFixed(1) + "%", color: "#f97316" },
        ].map((s) => (
            <div key={s.label} style={{ background: "#1e293b", padding: "12px 20px", borderRadius: "8px", textAlign: "center", minWidth: "120px" }}>
                <p style={{ margin: 0, color: "#94a3b8", fontSize: "12px" }}>{s.label}</p>
                <p style={{ margin: "4px 0 0", color: s.color, fontSize: "24px", fontWeight: "bold" }}>{s.value}</p>
            </div>
        ))}
    </div>
)}
            {allocationResult && (
              <div style={{ background: "#1e293b", padding: "16px", borderRadius: "8px", marginBottom: "20px" }}>
                <h3 style={{ color: allocationResult.success ? "#22c55e" : "#ef4444", margin: "0 0 10px" }}>
                  {allocationResult.success ? "✓ Allocated Successfully" : "✗ Allocation Failed"}
                </h3>
                {allocationResult.success && (
                  <>
                    <p style={{ margin: "4px 0", color: "#94a3b8" }}>Spot: <span style={{ color: "#f1f5f9" }}>{allocationResult.spot?.id}</span></p>
                    <p style={{ margin: "4px 0", color: "#94a3b8" }}>Floor: <span style={{ color: "#f1f5f9" }}>{allocationResult.spot?.floor}</span></p>
                    <p style={{ margin: "4px 0", color: "#94a3b8" }}>Zone: <span style={{ color: "#f1f5f9" }}>{allocationResult.spot?.zone}</span></p>
                    <p style={{ margin: "4px 0", color: "#94a3b8" }}>Execution Time: <span style={{ color: "#f1f5f9" }}>{allocationResult.executionTimeNs} ns</span></p>
                  </>
                )}
              </div>
            )}

            {compareResult && (
              <div>
                <h3 style={{ color: "#38bdf8" }}>Strategy Comparison</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={compareChartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                    <XAxis dataKey="name" stroke="#94a3b8" />
                    <YAxis stroke="#94a3b8" />
                    <Tooltip contentStyle={{ background: "#1e293b", border: "none" }} />
                    <Legend />
                    <Bar dataKey="executionTimeNs" fill="#38bdf8" name="Execution Time (ns)" />
                  </BarChart>
                </ResponsiveContainer>
                <div style={{ display: "flex", gap: "12px", flexWrap: "wrap", marginTop: "16px" }}>
                  {STRATEGIES.map((s) => (
                    <div key={s} style={{ background: "#1e293b", padding: "12px 16px", borderRadius: "8px", minWidth: "140px" }}>
                      <p style={{ margin: "0 0 6px", color: "#38bdf8", fontWeight: "bold", textTransform: "uppercase", fontSize: "12px" }}>{s}</p>
                      <p style={{ margin: "2px 0", color: "#94a3b8", fontSize: "13px" }}>
                        Spot: <span style={{ color: compareResult[s]?.spotFound ? "#22c55e" : "#ef4444" }}>
                          {compareResult[s]?.spotId}
                        </span>
                      </p>
                      <p style={{ margin: "2px 0", color: "#94a3b8", fontSize: "13px" }}>
                        Time: <span style={{ color: "#f1f5f9" }}>{compareResult[s]?.executionTimeNs} ns</span>
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* NAVIGATE */}
        {tab === "navigate" && (
          <div>
            <h2 style={{ color: "#38bdf8" }}>Navigation</h2>
            <div style={{ display: "flex", gap: "12px", alignItems: "flex-end", marginBottom: "20px" }}>
              <div>
                <label style={{ color: "#94a3b8", fontSize: "13px" }}>Target Zone</label>
                <select
                  value={navZone}
                  onChange={(e) => setNavZone(e.target.value)}
                  style={{ display: "block", marginTop: "6px", padding: "8px 12px", borderRadius: "6px", background: "#1e293b", color: "#f1f5f9", border: "1px solid #334155" }}
                >
                  {["F1-A", "F1-B", "F2-C", "F2-D", "F3-E"].map((z) => (
                    <option key={z}>{z}</option>
                  ))}
                </select>
              </div>
              <button onClick={navigate} style={{ padding: "10px 20px", borderRadius: "6px", background: "#38bdf8", color: "#0f172a", border: "none", cursor: "pointer", fontWeight: "bold" }}>
                Find Route
              </button>
            </div>

            {navResult && (
              <div>
                <div style={{ background: "#1e293b", padding: "16px", borderRadius: "8px", marginBottom: "16px" }}>
                  <p style={{ margin: "0 0 8px", color: "#94a3b8" }}>
                    Total Time: <span style={{ color: "#38bdf8", fontWeight: "bold" }}>{navResult.totalTimeSeconds} seconds</span>
                  </p>
                  <p style={{ margin: "0 0 12px", color: "#94a3b8" }}>Path:</p>
                  <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
                    {navResult.path?.map((node, i) => (
                      <div key={i} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                        <div style={{
                          padding: "6px 12px",
                          borderRadius: "6px",
                          background: i === 0 ? "#22c55e" : i === navResult.path.length - 1 ? "#ef4444" : "#334155",
                          color: "#f1f5f9",
                          fontSize: "13px",
                          fontWeight: "bold"
                        }}>
                          {node}
                        </div>
                        {i < navResult.path.length - 1 && <span style={{ color: "#38bdf8" }}>→</span>}
                      </div>
                    ))}
                  </div>
                </div>
                <h3 style={{ color: "#94a3b8" }}>Turn-by-Turn Instructions</h3>
                {navResult.instructions?.map((inst, i) => (
                  <div key={i} style={{ background: "#1e293b", padding: "10px 16px", borderRadius: "6px", marginBottom: "8px", color: "#f1f5f9", fontSize: "14px" }}>
                    <span style={{ color: "#38bdf8", marginRight: "10px" }}>{i + 1}.</span>{inst}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* RESERVATIONS */}
        {tab === "reservations" && (
          <div>
            <h2 style={{ color: "#38bdf8" }}>DP Reservation Scheduler</h2>
            <button onClick={fetchReservations} style={{ padding: "10px 20px", borderRadius: "6px", background: "#38bdf8", color: "#0f172a", border: "none", cursor: "pointer", fontWeight: "bold", marginBottom: "20px" }}>
              Run DP Scheduler
            </button>

            {reservations && (
              <div>
                <div style={{ display: "flex", gap: "16px", marginBottom: "20px" }}>
                  <div style={{ background: "#1e293b", padding: "16px 24px", borderRadius: "8px", textAlign: "center" }}>
                    <p style={{ margin: 0, color: "#94a3b8", fontSize: "13px" }}>Total Reservations</p>
                    <p style={{ margin: "4px 0 0", color: "#f1f5f9", fontSize: "28px", fontWeight: "bold" }}>{reservations.totalReservations}</p>
                  </div>
                  <div style={{ background: "#1e293b", padding: "16px 24px", borderRadius: "8px", textAlign: "center" }}>
                    <p style={{ margin: 0, color: "#94a3b8", fontSize: "13px" }}>Optimal Selected</p>
                    <p style={{ margin: "4px 0 0", color: "#22c55e", fontSize: "28px", fontWeight: "bold" }}>{reservations.optimalCount}</p>
                  </div>
                </div>

                <h3 style={{ color: "#94a3b8" }}>Selected Reservations</h3>
                <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                  {reservations.selected?.map((r, i) => (
                    <div key={i} style={{ background: "#1e293b", padding: "12px 16px", borderRadius: "8px", display: "flex", gap: "24px", alignItems: "center" }}>
                      <span style={{ color: "#38bdf8", fontWeight: "bold", minWidth: "40px" }}>{r.vehicleId}</span>
                      <span style={{ color: "#94a3b8", fontSize: "13px" }}>Arrival: <span style={{ color: "#f1f5f9" }}>{r.arrival}</span></span>
                      <span style={{ color: "#94a3b8", fontSize: "13px" }}>Departure: <span style={{ color: "#f1f5f9" }}>{r.departure}</span></span>
                      <span style={{ background: "#334155", padding: "2px 10px", borderRadius: "4px", fontSize: "12px", color: "#f1f5f9" }}>{r.type}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

      </div>
    </div>
  );
}