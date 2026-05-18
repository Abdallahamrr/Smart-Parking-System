import ParkingCellComponent from "./ParkingCell";

export default function FloorView({ cells, pathIds, startId, endId, onCellClick, isLoading }) {
  if (isLoading) return (
    <div style={{ display: "flex", justifyContent: "center", padding: 40, color: "#94a3b8" }}>
      <div style={{ fontSize: 18, display: "flex", alignItems: "center", gap: 10 }}>
        <span style={{ fontSize: 24 }}>⏳</span> Loading floor data...
      </div>
    </div>
  );
  if (!cells.length) return <div style={{ color: "#94a3b8", padding: 40, textAlign: "center" }}>No floor data available.</div>;

  const maxRow = Math.max(...cells.map((c) => c.row));
  const maxCol = Math.max(...cells.map((c) => c.col));

  const grid = Array.from({ length: maxRow + 1 }, () =>
    Array(maxCol + 1).fill(null)
  );
  cells.forEach((cell) => {
    grid[cell.row][cell.col] = cell;
  });

  return (
    <div style={{ display: "inline-block" }}>
      {grid.map((row, rowIndex) => (
        <div key={rowIndex} style={{ display: "flex", gap: 3, marginBottom: 3 }}>
          {row.map((cell, colIndex) => (
            cell ? (
              <ParkingCellComponent
                key={cell.id}
                cell={cell}
                isOnPath={pathIds.has(cell.id)}
                isStart={cell.id === startId}
                isEnd={cell.id === endId}
                onClick={onCellClick}
              />
            ) : (
              <div key={`empty-${rowIndex}-${colIndex}`} style={{ width: 52, height: 52 }} />
            )
          ))}
        </div>
      ))}
    </div>
  );
}
