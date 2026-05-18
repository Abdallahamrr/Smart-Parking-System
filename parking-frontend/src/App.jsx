import { useEffect, useState } from "react";
import { getFloor, getPath, occupySpot, releaseSpot } from "./services/api";
import FloorView from "./FloorView";
import AllocationTab from "./AllocationTab";
import SchedulerTab from "./SchedulerTab";
import DashboardTab from "./DashboardTab";

const FLOORS = [1, 2, 3];
const generateVehicleId = () => `VH-${Date.now()}`;

export default function App() {
  const [activeTab, setActiveTab] = useState("navigation");
  const [currentFloor, setCurrentFloor] = useState(1);
  const [floorData, setFloorData] = useState({});
  const [pathIds, setPathIds] = useState(new Set());
  const [startId, setStartId] = useState("");
  const [endId, setEndId] = useState("");
  const [pathResult, setPathResult] = useState(null);
  const [mode, setMode] = useState("view");
  const [travelerMode, setTravelerMode] = useState("FOOT");
  const [vehicleInput, setVehicleInput] = useState("");
  const [loadingFloor, setLoadingFloor] = useState(false);
  const [error, setError] = useState("");

  const loadFloor = async (floor, force = false) => {
    if (!force && floorData[floor]) return;

    setLoadingFloor(true);
    setError("");
    try {
      const data = await getFloor(floor);
      setFloorData((prev) => ({ ...prev, [floor]: data }));
    } catch (err) {
      setFloorData((prev) => ({ ...prev, [floor]: [] }));
      setError(
        `Could not load floor ${floor}. Make sure the backend is running on http://localhost:8080. ${err.message}`
      );
    } finally {
      setLoadingFloor(false);
    }
  };

  useEffect(() => {
    loadFloor(currentFloor);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentFloor]);

  const handleCellClick = async (cell) => {
    if (mode === "selectStart") {
      setStartId(cell.id);
      setMode("selectEnd");
      return;
    }

    if (mode === "selectEnd") {
      setEndId(cell.id);
      setMode("view");
      await findPath(startId, cell.id, travelerMode);
      return;
    }

    try {
      if (cell.occupied) {
        await releaseSpot(cell.id);
      } else {
        await occupySpot(cell.id, vehicleInput || generateVehicleId());
      }
      await loadFloor(currentFloor, true);
    } catch (err) {
      setError(`Could not update ${cell.id}. ${err.message}`);
    }
  };

  const findPath = async (from, to, currentTravelerMode = travelerMode) => {
    setError("");
    try {
      const result = await getPath(from, to, currentTravelerMode);
      setPathResult(result);
      setPathIds(result.found ? new Set(result.path.map((step) => step.id)) : new Set());
    } catch (err) {
      setPathIds(new Set());
      setError(`Could not calculate path. ${err.message}`);
    }
  };

  const clearPath = () => {
    setPathIds(new Set());
    setStartId("");
    setEndId("");
    setPathResult(null);
    setMode("view");
  };

  const cells = floorData[currentFloor] || [];

  return (
    <div style={{ minHeight: "100vh", background: "#0f172a", color: "#f1f5f9", fontFamily: "sans-serif", padding: 24 }}>
      <h1 style={{ color: "#38bdf8", marginBottom: 4 }}>Smart Parking System</h1>
      <p style={{ color: "#94a3b8", marginBottom: 20, fontSize: 13 }}>Graph-based multi-floor navigation</p>

      <div style={{ display: "flex", gap: 10, marginBottom: 24, borderBottom: "1px solid #334155", paddingBottom: 16 }}>
        <button
          onClick={() => setActiveTab("navigation")}
          style={{ padding: "8px 16px", borderRadius: 6, border: "none", cursor: "pointer", background: activeTab === "navigation" ? "#38bdf8" : "transparent", color: activeTab === "navigation" ? "#0f172a" : "#94a3b8", fontWeight: "bold", transition: "0.2s" }}
        >
          Navigation & Floor Map
        </button>
        <button
          onClick={() => setActiveTab("allocation")}
          style={{ padding: "8px 16px", borderRadius: 6, border: "none", cursor: "pointer", background: activeTab === "allocation" ? "#38bdf8" : "transparent", color: activeTab === "allocation" ? "#0f172a" : "#94a3b8", fontWeight: "bold", transition: "0.2s" }}
        >
          Spot Allocation
        </button>
        <button
          onClick={() => setActiveTab("scheduler")}
          style={{ padding: "8px 16px", borderRadius: 6, border: "none", cursor: "pointer", background: activeTab === "scheduler" ? "#38bdf8" : "transparent", color: activeTab === "scheduler" ? "#0f172a" : "#94a3b8", fontWeight: "bold", transition: "0.2s" }}
        >
          Reservation Scheduler
        </button>
        <button
          onClick={() => setActiveTab("dashboard")}
          style={{ padding: "8px 16px", borderRadius: 6, border: "none", cursor: "pointer", background: activeTab === "dashboard" ? "#38bdf8" : "transparent", color: activeTab === "dashboard" ? "#0f172a" : "#94a3b8", fontWeight: "bold", transition: "0.2s" }}
        >
          Utilization Dashboard
        </button>
      </div>

      {activeTab === "navigation" && (
        <>
          <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
            {FLOORS.map((floor) => (
              <button
                key={floor}
                onClick={() => setCurrentFloor(floor)}
                style={{
                  padding: "8px 20px",
                  borderRadius: 8,
                  border: "none",
                  cursor: "pointer",
                  background: currentFloor === floor ? "#38bdf8" : "#1e293b",
                  color: currentFloor === floor ? "#0f172a" : "#94a3b8",
                  fontWeight: "bold",
                }}
              >
                Floor {floor}
              </button>
            ))}
          </div>

          <div style={{ display: "flex", gap: 10, marginBottom: 20, flexWrap: "wrap", alignItems: "center" }}>
            <select
              value={travelerMode}
              onChange={(e) => {
                const newMode = e.target.value;
                setTravelerMode(newMode);
                if (startId && endId) {
                  findPath(startId, endId, newMode);
                }
              }}
              style={{ padding: "8px 12px", borderRadius: 6, border: "1px solid #334155", background: "#1e293b", color: "#f1f5f9" }}
            >
              <option value="FOOT">🚶 Walking Mode</option>
              <option value="CAR">🚗 Driving Mode</option>
            </select>
            <input
              placeholder="Vehicle ID (for parking)"
              value={vehicleInput}
              onChange={(event) => setVehicleInput(event.target.value)}
              style={{ padding: "8px 12px", borderRadius: 6, border: "1px solid #334155", background: "#1e293b", color: "#f1f5f9", width: 200 }}
            />
            <button onClick={() => setMode("selectStart")} style={{ padding: "8px 16px", borderRadius: 6, background: mode === "selectStart" ? "#22c55e" : "#334155", color: "#fff", border: "none", cursor: "pointer" }}>
              {mode === "selectStart" ? "Click Start..." : "Set Start"}
            </button>
            <button onClick={() => setMode("selectEnd")} disabled={!startId} style={{ padding: "8px 16px", borderRadius: 6, background: mode === "selectEnd" ? "#a855f7" : "#334155", color: "#fff", border: "none", cursor: startId ? "pointer" : "not-allowed", opacity: startId ? 1 : 0.55 }}>
              {mode === "selectEnd" ? "Click End..." : "Set End"}
            </button>
            <button onClick={clearPath} style={{ padding: "8px 16px", borderRadius: 6, background: "#ef4444", color: "#fff", border: "none", cursor: "pointer" }}>
              Clear Path
            </button>
          </div>

          {error && (
            <div style={{ background: "#451a1a", border: "1px solid #ef4444", color: "#fecaca", padding: "12px 16px", borderRadius: 8, marginBottom: 16, fontSize: 13 }}>
              {error}
            </div>
          )}

          {pathResult && (
            <div style={{ background: "#1e293b", padding: "16px", borderRadius: 8, marginBottom: 16, fontSize: 13, border: "1px solid #334155" }}>
              {pathResult.found ? (
                <>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                    <span style={{ color: "#22c55e", fontWeight: "bold", fontSize: 15 }}>✅ Path Found</span>
                    <span style={{ color: "#38bdf8", fontWeight: "bold", background: "#0f172a", padding: "6px 12px", borderRadius: 6 }}>
                      ⏱️ {Math.floor(pathResult.totalTimeSeconds / 60) > 0 ? `${Math.floor(pathResult.totalTimeSeconds / 60)}m ` : ""}{pathResult.totalTimeSeconds % 60}s
                    </span>
                  </div>
                  <div style={{ color: "#94a3b8", wordBreak: "break-all", marginBottom: 12, lineHeight: 1.5 }}>
                    <strong style={{ color: "#e2e8f0" }}>Route:</strong> {pathResult.path.map(step => step.id).join(" → ")}
                  </div>
                  {(pathResult.elevatorWalkSeconds > 0 || pathResult.rampTimeSeconds > 0) && (
                    <div style={{ display: "flex", gap: 12, color: "#cbd5e1", fontSize: 12, background: "#0f172a", padding: "8px 12px", borderRadius: 6, display: "inline-flex" }}>
                      {pathResult.elevatorWalkSeconds > 0 && <span>🛗 Elevator Time: {pathResult.elevatorWalkSeconds}s</span>}
                      {pathResult.rampTimeSeconds > 0 && <span>🛬 Ramp Time: {pathResult.rampTimeSeconds}s</span>}
                    </div>
                  )}
                </>
              ) : (
                <span style={{ color: "#ef4444" }}>No path found between selected points.</span>
              )}
            </div>
          )}

          <div style={{ display: "flex", gap: 14, marginBottom: 16, flexWrap: "wrap" }}>
            {[
              ["#22c55e", "Available spot"],
              ["#ef4444", "Occupied"],
              ["#374151", "Road"],
              ["#f97316", "Ramp"],
              ["#3b82f6", "Elevator"],
              ["#facc15", "Path"],
              ["#a855f7", "Start/End"],
            ].map(([color, label]) => (
              <div key={label} style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <div style={{ width: 14, height: 14, borderRadius: 3, background: color }} />
                <span style={{ fontSize: 12, color: "#94a3b8" }}>{label}</span>
              </div>
            ))}
          </div>

          <div style={{ background: "#1e293b", padding: 20, borderRadius: 8, overflowX: "auto" }}>
            <FloorView
              cells={cells}
              pathIds={pathIds}
              startId={startId}
              endId={endId}
              onCellClick={handleCellClick}
              isLoading={loadingFloor}
            />
          </div>

          <p style={{ marginTop: 12, fontSize: 12, color: "#64748b" }}>
            {mode === "view" && "Click a parking spot to park/release. Use Set Start -> Set End to find a path."}
            {mode === "selectStart" && "Click any cell to set as navigation start point."}
            {mode === "selectEnd" && "Click any cell to set as navigation end point. The path will calculate automatically."}
          </p>
        </>
      )}

      {activeTab === "allocation" && <AllocationTab />}
      {activeTab === "scheduler" && <SchedulerTab />}
      {activeTab === "dashboard" && <DashboardTab />}
    </div>
  );
}
