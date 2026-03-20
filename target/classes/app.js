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

const ENEMY_COLORS = {
  RED: "#DC2626",
  BLUE: "#2563EB",
  GREEN: "#16A34A",
  YELLOW: "#EAB308",
  PINK: "#EC4899",
  CAMO: "#65A30D",
  LEAD: "#6B7280",
  ZEBRA: "#1C1917",
  RAINBOW: "#7C3AED",
  CERAMIC: "#92400E",
  MOAB: "#1E40AF",
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

const canvas = document.getElementById("game");
const ctx = canvas.getContext("2d");

function drawMap() {
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

function drawEnemies(enemies) {
  enemies.forEach((e) => {
    const radius = e.type === "MOAB" ? 24 : e.type === "CERAMIC" ? 16 : 14;

    ctx.beginPath();
    ctx.arc(e.px, e.py, radius, 0, Math.PI * 2);
    ctx.fillStyle = ENEMY_COLORS[e.type] ?? "#FFFFFF";
    ctx.fill();
    ctx.strokeStyle = "#FFFFFF44";
    ctx.lineWidth = 2;
    ctx.stroke();

    if (e.hp < e.maxHp) {
      const barW = 28;
      const barH = 4;
      const filled = (e.hp / e.maxHp) * barW;
      ctx.fillStyle = "#FF000088";
      ctx.fillRect(e.px - barW / 2, e.py - 20, barW, barH);
      ctx.fillStyle = "#00FF0088";
      ctx.fillRect(e.px - barW / 2, e.py - 20, filled, barH);
    }
  });
}

function render(state) {
  drawMap();
  drawEnemies(state.enemies || []);
}

function updateHud(state) {
  document.getElementById("hud-lives").textContent = String(state.lives ?? 0);
  document.getElementById("hud-coins").textContent = String(state.coins ?? 0);
  document.getElementById("hud-wave").textContent = String(state.wave ?? 0);
  const overlay = document.getElementById("game-over");
  overlay.classList.toggle("hidden", state.phase !== "GAME_OVER");
}

async function startWave() {
  try {
    await fetch("/api/start-wave", { method: "POST" });
  } catch (err) {
    console.error("Failed to start wave", err);
  }
}

function connectEvents() {
  const events = new EventSource("/events");
  events.onmessage = (event) => {
    try {
      const state = JSON.parse(event.data);
      updateHud(state);
      render(state);
    } catch (err) {
      console.error("Invalid SSE payload", err, event.data);
    }
  };
  events.onerror = (event) => {
    console.log("SSE error:", event);
  };
}

document.getElementById("start-wave").addEventListener("click", startWave);
render({ enemies: [] });
connectEvents();
