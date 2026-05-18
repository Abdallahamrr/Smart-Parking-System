import ParkingCellComponent from "./ParkingCell";

export default function FloorView({ cells, pathIds, startId, endId, onCellClick, isLoading }) {
  if (isLoading) return <div style={{ color: "#94a3b8" }}>Loading floor...</div>;
  if (!cells.length) return <div style={{ color: "#94a3b8" }}>No floor data available.</div>;

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
