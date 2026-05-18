import { useState, useEffect } from "react";
import { compareAllocation } from "./services/api";

export default function AllocationTab() {
  const [vehicleType, setVehicleType] = useState("COMPACT");
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleCompare = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await compareAllocation(vehicleType);
      setResults(data);
    } catch (err) {
      setError(`Failed to compare algorithms: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 style={{ color: "#38bdf8", marginBottom: 12 }}>Vehicle Allocation Comparison (Bin Packing)</h2>
      <p style={{ color: "#94a3b8", marginBottom: 20 }}>Compare performance and spot selection across different Bin Packing algorithms.</p>
      
      <div style={{ display: "flex", gap: 10, marginBottom: 20 }}>
        <select 
          value={vehicleType} 
          onChange={(e) => setVehicleType(e.target.value)}
          style={{ padding: "8px 12px", borderRadius: 6, border: "1px solid #334155", background: "#1e293b", color: "#f1f5f9" }}
        >
          <option value="COMPACT">Compact</option>
          <option value="STANDARD">Standard</option>
          <option value="SUV">SUV</option>
          <option value="TRUCK">Truck</option>
        </select>
        <button 
          onClick={handleCompare}
          disabled={loading}
          style={{ padding: "8px 16px", borderRadius: 6, background: "#38bdf8", color: "#0f172a", border: "none", cursor: loading ? "not-allowed" : "pointer", fontWeight: "bold" }}
        >
          {loading ? "Comparing..." : "Compare Algorithms"}
        </button>
      </div>

      {error && (
        <div style={{ background: "#451a1a", border: "1px solid #ef4444", color: "#fecaca", padding: "12px 16px", borderRadius: 8, marginBottom: 16, fontSize: 13 }}>
          {error}
        </div>
      )}

      {loading && !results && (
        <div style={{ display: "flex", justifyContent: "center", padding: 40, color: "#94a3b8" }}>
          <div style={{ fontSize: 18, display: "flex", alignItems: "center", gap: 10 }}>
            <span style={{ fontSize: 24 }}>⏳</span> Running algorithms...
          </div>
        </div>
      )}

      {results && (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: 16 }}>
          {Object.entries(results).map(([algo, data]) => (
            <div key={algo} style={{ background: "#1e293b", padding: 16, borderRadius: 8, border: "1px solid #334155" }}>
              <h3 style={{ margin: "0 0 12px 0", color: "#e2e8f0", textTransform: "capitalize" }}>{algo.replace(/([A-Z])/g, ' $1').trim()}</h3>
              <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <div style={{ display: "flex", justifyContent: "space-between" }}>
                  <span style={{ color: "#94a3b8" }}>Spot:</span>
                  <span style={{ color: data.spot === "None" ? "#ef4444" : "#22c55e", fontWeight: "bold" }}>{data.spot}</span>
                </div>
                <div style={{ display: "flex", justifyContent: "space-between" }}>
                  <span style={{ color: "#94a3b8" }}>Execution Time:</span>
                  <span style={{ color: "#f1f5f9" }}>{data.timeNs} ns</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
