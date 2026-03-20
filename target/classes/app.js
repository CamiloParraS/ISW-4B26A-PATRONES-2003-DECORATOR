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

const TOWER_VISUALS = {
  dart: { color: "#22C55E", label: "Doro", cost: 200 },
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

const grid = createGrid();

const canvas = document.getElementById("game");
const ctx = canvas.getContext("2d");

let latestState = {
  wave: 0,
  lives: 100,
  coins: 650,
  phase: "PREP",
  enemies: [],
  towers: [],
  projectiles: [],
};
let pendingTowerType = null;
let selectedTowerId = null;
let hoverCell = null;

function towerAt(gx, gy) {
  const towers = latestState.towers || [];
  return towers.find((t) => t.gx === gx && t.gy === gy) || null;
}

function isWithinBounds(gx, gy) {
  return gx >= 0 && gx < COLS && gy >= 0 && gy < ROWS;
}

function isBuildable(gx, gy) {
  return (
    isWithinBounds(gx, gy) && grid[gx][gy] === BUILDABLE && !towerAt(gx, gy)
  );
}

function getCellFromMouse(event) {
  const rect = canvas.getBoundingClientRect();
  const scaleX = canvas.width / rect.width;
  const scaleY = canvas.height / rect.height;
  const px = (event.clientX - rect.left) * scaleX;
  const py = (event.clientY - rect.top) * scaleY;
  return {
    gx: Math.floor(px / TILE),
    gy: Math.floor(py / TILE),
  };
}

function drawMap() {
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

  if (
    pendingTowerType &&
    hoverCell &&
    isWithinBounds(hoverCell.gx, hoverCell.gy)
  ) {
    const canPlace = isBuildable(hoverCell.gx, hoverCell.gy);
    ctx.fillStyle = canPlace ? "#22C55E66" : "#EF444466";
    ctx.fillRect(hoverCell.gx * TILE, hoverCell.gy * TILE, TILE, TILE);
    ctx.strokeStyle = canPlace ? "#22C55E" : "#EF4444";
    ctx.lineWidth = 2;
    ctx.strokeRect(
      hoverCell.gx * TILE + 1,
      hoverCell.gy * TILE + 1,
      TILE - 2,
      TILE - 2,
    );
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

function drawTowers(towers) {
  towers.forEach((t) => {
    const visual = TOWER_VISUALS[t.type] || { color: "#22C55E" };
    const px = t.gx * TILE + TILE / 2;
    const py = t.gy * TILE + TILE / 2;

    ctx.beginPath();
    ctx.arc(px, py, 18, 0, Math.PI * 2);
    ctx.fillStyle = visual.color;
    ctx.fill();

    if (selectedTowerId === t.id) {
      ctx.beginPath();
      ctx.arc(px, py, t.range, 0, Math.PI * 2);
      ctx.strokeStyle = `${visual.color}55`;
      ctx.lineWidth = 2;
      ctx.stroke();
    }
  });
}

function drawProjectiles(projectiles) {
  projectiles.forEach((p) => {
    ctx.beginPath();
    ctx.arc(p.px, p.py, 5, 0, Math.PI * 2);
    ctx.fillStyle = p.type === "dart" ? "#FFFFFF" : "#FFA500";
    ctx.fill();
  });
}

function render(state) {
  drawMap();
  drawTowers(state.towers || []);
  drawEnemies(state.enemies || []);
  drawProjectiles(state.projectiles || []);
}

function updateHud(state) {
  document.getElementById("hud-lives").textContent = String(state.lives ?? 0);
  document.getElementById("hud-coins").textContent = String(state.coins ?? 0);
  document.getElementById("hud-wave").textContent = String(state.wave ?? 0);
  const overlay = document.getElementById("game-over");
  overlay.classList.toggle("hidden", state.phase !== "GAME_OVER");
}

function updateTowerPanel() {
  const modeLabel = document.getElementById("build-mode");
  modeLabel.textContent = pendingTowerType
    ? `Colocando: ${TOWER_VISUALS[pendingTowerType].label}`
    : "Colocand: ningun";

  const selected =
    (latestState.towers || []).find((t) => t.id === selectedTowerId) || null;
  const selectedPanel = document.getElementById("selected-panel");
  const selectedLabel = document.getElementById("selected-label");
  const sellBtn = document.getElementById("sell-button");

  if (!selected) {
    selectedPanel.classList.add("hidden");
    selectedLabel.textContent = "No tower selected";
    sellBtn.textContent = "Sell";
    return;
  }

  selectedPanel.classList.remove("hidden");
  selectedLabel.textContent = `${TOWER_VISUALS[selected.type]?.label || selected.type} (${selected.gx}, ${selected.gy})`;
  sellBtn.textContent = `Sell for ${selected.sellValue}c`;
}

async function startWave() {
  try {
    await fetch("/api/start-wave", { method: "POST" });
  } catch (err) {
    console.error("Failed to start wave", err);
  }
}

async function placeTower(type, gx, gy) {
  try {
    const response = await fetch("/api/place-tower", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type, gx, gy }),
    });
    if (response.ok) {
      pendingTowerType = null;
    }
  } catch (err) {
    console.error("Failed to place tower", err);
  }
}

async function sellSelectedTower() {
  if (!selectedTowerId) {
    return;
  }
  try {
    const response = await fetch("/api/sell", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ towerId: selectedTowerId }),
    });
    if (response.ok) {
      selectedTowerId = null;
    }
  } catch (err) {
    console.error("Failed to sell tower", err);
  }
}

function connectEvents() {
  const events = new EventSource("/events");
  events.onmessage = (event) => {
    try {
      const state = JSON.parse(event.data);
      latestState = state;
      if (!(latestState.towers || []).some((t) => t.id === selectedTowerId)) {
        selectedTowerId = null;
      }
      updateHud(state);
      updateTowerPanel();
      render(state);
    } catch (err) {
      console.error("Invalid SSE payload", err, event.data);
    }
  };
  events.onerror = (event) => {
    console.log("SSE error:", event);
  };
}

function connectUi() {
  document.getElementById("place-dart").addEventListener("click", () => {
    pendingTowerType = "dart";
    selectedTowerId = null;
    updateTowerPanel();
    render(latestState);
  });

  document.getElementById("cancel-place").addEventListener("click", () => {
    pendingTowerType = null;
    updateTowerPanel();
    render(latestState);
  });

  document
    .getElementById("sell-button")
    .addEventListener("click", sellSelectedTower);

  canvas.addEventListener("mousemove", (event) => {
    if (!pendingTowerType) {
      return;
    }
    hoverCell = getCellFromMouse(event);
    render(latestState);
  });

  canvas.addEventListener("mouseleave", () => {
    hoverCell = null;
    render(latestState);
  });

  canvas.addEventListener("click", async (event) => {
    const cell = getCellFromMouse(event);
    if (!isWithinBounds(cell.gx, cell.gy)) {
      return;
    }

    if (pendingTowerType) {
      await placeTower(pendingTowerType, cell.gx, cell.gy);
      updateTowerPanel();
      return;
    }

    const tower = towerAt(cell.gx, cell.gy);
    selectedTowerId = tower ? tower.id : null;
    updateTowerPanel();
    render(latestState);
  });
}

document.getElementById("start-wave").addEventListener("click", startWave);
connectUi();
updateTowerPanel();
render(latestState);
connectEvents();
