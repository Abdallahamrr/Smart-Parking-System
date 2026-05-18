import { useEffect, useState } from "react";
import { getUtilization } from "./services/api";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from "recharts";

const COLORS = ['#38bdf8', '#a855f7', '#22c55e', '#facc15', '#f43f5e'];

export default function DashboardTab() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await getUtilization();
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ color: "#94a3b8" }}>Loading dashboard...</div>;
  if (error) return <div style={{ color: "#ef4444" }}>Error: {error}</div>;
  if (!data) return null;

  const floorData = Object.keys(data.floorFill).map(f => ({
    name: `Floor ${f}`,
    fill: data.floorFill[f]
  }));

  const zoneData = Object.keys(data.zoneFill).map(z => ({
    name: z,
    fill: data.zoneFill[z]
  }));

  const vehicleData = Object.keys(data.vehicleTypeBreakdown).map(v => ({
    name: v,
    value: data.vehicleTypeBreakdown[v]
  }));

  return (
    <div style={{ padding: 20, background: "#1e293b", borderRadius: 8, color: "#f1f5f9" }}>
      <div style={{ display: "flex", gap: 20, marginBottom: 24 }}>
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 1, textAlign: "center" }}>
          <h3 style={{ margin: 0, color: "#94a3b8", fontSize: 14 }}>Overall Utilization</h3>
          <div style={{ fontSize: 36, fontWeight: "bold", color: "#38bdf8", marginTop: 10 }}>
            {data.overallFillPercent}%
          </div>
          <div style={{ fontSize: 12, color: "#64748b", marginTop: 5 }}>
            {data.occupiedSpots} / {data.totalSpots} Spots Occupied
          </div>
        </div>
      </div>

      {/* Available Spots Breakdown */}
      <div style={{ display: "flex", gap: 20, marginBottom: 24, flexWrap: "wrap" }}>
        {/* Designated Spot Availability */}
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 1, minWidth: 300 }}>
          <h3 style={{ margin: "0 0 8px 0", color: "#38bdf8", fontSize: 15, display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ fontSize: 18 }}>📊</span> Designated Spot Availability
          </h3>
          <p style={{ color: "#64748b", fontSize: 12, margin: "0 0 16px 0" }}>
            Number of free spots specifically designated for each size.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {data.availableSpotsByDesignatedType && Object.entries(data.availableSpotsByDesignatedType).map(([type, count]) => (
              <div key={type} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", background: "#1e293b", padding: "10px 14px", borderRadius: 6, borderLeft: `4px solid ${type === "COMPACT" ? "#38bdf8" : type === "STANDARD" ? "#a855f7" : type === "SUV" ? "#22c55e" : "#facc15"}` }}>
                <span style={{ fontWeight: 600, color: "#cbd5e1", fontSize: 13 }}>{type} Spot</span>
                <span style={{
                  background: count > 0 ? "rgba(34, 197, 94, 0.15)" : "rgba(239, 68, 68, 0.15)",
                  color: count > 0 ? "#4ade80" : "#f87171",
                  padding: "4px 10px",
                  borderRadius: 4,
                  fontWeight: "bold",
                  fontSize: 12
                }}>
                  {count} Free
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Fitting Spot Availability */}
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 1, minWidth: 300 }}>
          <h3 style={{ margin: "0 0 8px 0", color: "#a855f7", fontSize: 15, display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ fontSize: 18 }}>🚗</span> Compatible Spot Availability
          </h3>
          <p style={{ color: "#64748b", fontSize: 12, margin: "0 0 16px 0" }}>
            Total number of free spots that can fit each vehicle type.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {data.availableSpotsByFittingVehicleType && Object.entries(data.availableSpotsByFittingVehicleType).map(([type, count]) => (
              <div key={type} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", background: "#1e293b", padding: "10px 14px", borderRadius: 6, borderLeft: `4px solid ${type === "COMPACT" ? "#38bdf8" : type === "STANDARD" ? "#a855f7" : type === "SUV" ? "#22c55e" : "#facc15"}` }}>
                <span style={{ fontWeight: 600, color: "#cbd5e1", fontSize: 13 }}>{type} Vehicle</span>
                <span style={{
                  background: count > 0 ? "rgba(56, 189, 248, 0.15)" : "rgba(239, 68, 68, 0.15)",
                  color: count > 0 ? "#38bdf8" : "#f87171",
                  padding: "4px 10px",
                  borderRadius: 4,
                  fontWeight: "bold",
                  fontSize: 12
                }}>
                  {count} Fits
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div style={{ display: "flex", gap: 20, marginBottom: 24, flexWrap: "wrap" }}>
        {/* Floor Fill */}
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 1, minWidth: 300 }}>
          <h3 style={{ margin: "0 0 20px 0", color: "#e2e8f0", fontSize: 15 }}>Fill % per Floor</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={floorData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" domain={[0, 100]} fontSize={12} />
              <Tooltip contentStyle={{ background: "#1e293b", border: "none", color: "#f8fafc", borderRadius: 8 }} />
              <Bar dataKey="fill" fill="#38bdf8" radius={[4, 4, 0, 0]} name="Fill %" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Zone Fill */}
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 1, minWidth: 300 }}>
          <h3 style={{ margin: "0 0 20px 0", color: "#e2e8f0", fontSize: 15 }}>Fill % per Zone</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={zoneData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" domain={[0, 100]} fontSize={12} />
              <Tooltip contentStyle={{ background: "#1e293b", border: "none", color: "#f8fafc", borderRadius: 8 }} />
              <Bar dataKey="fill" fill="#a855f7" radius={[4, 4, 0, 0]} name="Fill %" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div style={{ display: "flex", gap: 20, flexWrap: "wrap" }}>
        {/* Time Series */}
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 2, minWidth: 400 }}>
          <h3 style={{ margin: "0 0 20px 0", color: "#e2e8f0", fontSize: 15 }}>Time-Series Occupancy</h3>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={data.timeSeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="time" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" domain={[0, 'dataMax + 5']} fontSize={12} />
              <Tooltip contentStyle={{ background: "#1e293b", border: "none", color: "#f8fafc", borderRadius: 8 }} />
              <Line type="monotone" dataKey="occupancy" stroke="#22c55e" strokeWidth={3} dot={{ r: 4 }} name="Occupied Spots" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Vehicle Breakdown */}
        <div style={{ background: "#0f172a", padding: 20, borderRadius: 8, flex: 1, minWidth: 300 }}>
          <h3 style={{ margin: "0 0 20px 0", color: "#e2e8f0", fontSize: 15 }}>Spot Breakdown (By Max Size)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie data={vehicleData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value" label={({name, percent}) => `${name} ${(percent * 100).toFixed(0)}%`} labelLine={false} style={{ fontSize: 12 }}>
                {vehicleData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ background: "#1e293b", border: "none", color: "#f8fafc", borderRadius: 8 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
