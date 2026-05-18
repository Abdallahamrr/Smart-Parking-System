const TYPE_STYLES = {
  PARKING_SPOT: { background: "#22c55e", border: "1px solid #16a34a" },
  ROAD: { background: "#374151", border: "1px solid #4b5563" },
  RAMP_UP: { background: "#f97316", border: "1px solid #ea580c" },
  RAMP_DOWN: { background: "#fb923c", border: "1px solid #f97316" },
  RAMP_BOTH: { background: "#f59e0b", border: "1px solid #d97706" },
  ELEVATOR: { background: "#3b82f6", border: "1px solid #2563eb" },
  INTERSECTION: { background: "#4b5563", border: "1px solid #6b7280" },
  WALL: { background: "#1f2937", border: "1px solid #111827" },
};

const TYPE_LABELS = {
  ELEVATOR: "EL",
  RAMP_UP: "UP",
  RAMP_DOWN: "DN",
  RAMP_BOTH: "UD",
  INTERSECTION: "+",
  ROAD: "",
};

export default function ParkingCellComponent({ cell, isOnPath, isStart, isEnd, onClick }) {
  const isSpot = cell.type === "PARKING_SPOT";
  const baseStyle = TYPE_STYLES[cell.type] || TYPE_STYLES.WALL;

  let background = baseStyle.background;
  if (isSpot && cell.reserved) background = "#3b82f6"; // Blue for reserved spots
  if (isSpot && cell.occupied) background = "#ef4444";
  if (isOnPath) background = "#facc15";
  if (isStart) background = "#22c55e";
  if (isEnd) background = "#a855f7";

  return (
    <div
      onClick={() => isSpot && onClick?.(cell)}
      onMouseEnter={(event) => {
        if (isSpot) event.currentTarget.style.transform = "scale(1.08)";
      }}
      onMouseLeave={(event) => {
        event.currentTarget.style.transform = "scale(1)";
      }}
      title={cell.id}
      style={{
        ...baseStyle,
        background,
        width: 52,
        height: 52,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        borderRadius: 4,
        cursor: isSpot ? "pointer" : "default",
        fontSize: 9,
        fontWeight: 700,
        color: "#fff",
        transition: "all 0.15s",
        boxShadow: isOnPath ? "0 0 8px #facc15" : isStart || isEnd ? "0 0 8px #a855f7" : "none",
        userSelect: "none",
      }}
    >
      {isSpot && (
        <span style={{
          fontSize: 7,
          fontWeight: 800,
          background: "rgba(15, 23, 42, 0.75)",
          color: cell.maxSize === "COMPACT" ? "#38bdf8" :
                 cell.maxSize === "STANDARD" ? "#a855f7" :
                 cell.maxSize === "SUV" ? "#22c55e" : "#facc15",
          padding: "1px 3px",
          borderRadius: 2,
          marginBottom: 1,
          textTransform: "uppercase",
          letterSpacing: "0.5px"
        }}>
          {cell.maxSize === "COMPACT" ? "COMP" :
           cell.maxSize === "STANDARD" ? "STD" :
           cell.maxSize === "SUV" ? "SUV" : "TRK"}
        </span>
      )}
      <span style={{ fontSize: 12 }}>{TYPE_LABELS[cell.type] ?? null}</span>
      <span style={{ fontSize: 9, marginTop: 1, color: "#e2e8f0" }}>{cell.label}</span>
      {isSpot && cell.occupied && (
        <span style={{ fontSize: 7, color: "#fecaca" }}>{cell.vehicleId?.slice(0, 6)}</span>
      )}
    </div>
  );
}
