const COLS = 24;
const ROWS = 16;
const TILE = 48;

const PATH = 0;
const BUILDABLE = 1;
const SCENERY = 2;

const COLORS = {
  [PATH]: "#C8A96E",
  [BUILDABLE]: "#2D5A1B",
  [SCENERY]: "#1A3A0A",
};

const waypoints = [
  [0, 7],
  [5, 7],
  [5, 3],
  [11, 3],
  [11, 11],
  [16, 11],
  [16, 5],
  [21, 5],
  [21, 9],
  [23, 9],
];

function createGrid() {
  const grid = Array.from({ length: COLS }, () => Array(ROWS).fill(BUILDABLE));

  for (let i = 0; i < waypoints.length - 1; i++) {
    const [fromCol, fromRow] = waypoints[i];
    const [toCol, toRow] = waypoints[i + 1];
    const dc = Math.sign(toCol - fromCol);
    const dr = Math.sign(toRow - fromRow);

    let col = fromCol;
    let row = fromRow;
    grid[col][row] = PATH;

    while (col !== toCol || row !== toRow) {
      col += dc;
      row += dr;
      grid[col][row] = PATH;
    }
  }

  return grid;
}

function drawMap() {
  const canvas = document.getElementById("game");
  const ctx = canvas.getContext("2d");
  const grid = createGrid();

  for (let col = 0; col < COLS; col++) {
    for (let row = 0; row < ROWS; row++) {
      const type = grid[col][row] ?? SCENERY;
      const x = col * TILE;
      const y = row * TILE;

      ctx.fillStyle = COLORS[type] || COLORS[SCENERY];
      ctx.fillRect(x, y, TILE, TILE);

      ctx.strokeStyle = "#00000033";
      ctx.lineWidth = 1;
      ctx.strokeRect(x + 0.5, y + 0.5, TILE - 1, TILE - 1);
    }
  }
}

function connectEvents() {
  const events = new EventSource("/events");
  events.onmessage = (event) => {
    console.log("SSE message:", event.data);
  };
  events.onerror = (event) => {
    console.log("SSE error:", event);
  };
}

drawMap();
connectEvents();
