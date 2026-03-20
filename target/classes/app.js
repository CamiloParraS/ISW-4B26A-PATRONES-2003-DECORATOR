const COLS = 24;
const ROWS = 16;
const TILE = 48;

const PATH = 0;
const BUILDABLE = 1;
const SCENERY = 2;

const doroImage = new Image();
doroImage.src = "./assets/doro.webp";

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

const UPGRADE_META = {
  RapidFire: { label: "Disparo Rapido", cost: 350, color: "#FACC15" },
  Sniper: { label: "Sniper", cost: 500, color: "#A855F7" },
  Explosive: { label: "Explosivo", cost: 600, color: "#EF4444" },
  Freezing: { label: "Congelante", cost: 450, color: "#60A5FA" },
  Piercing: { label: "Perforante", cost: 400, color: "#F97316" },
  Laser: { label: "Laser", cost: 700, color: "#FBBF24" },
  CamoDetector: { label: "Detector Camuflaje", cost: 300, color: "#10B981" },
};

const PROJ_STYLES = {
  dart: { color: "#FFFFFF", radius: 5 },
  bomb: { color: "#1C1917", radius: 8, outline: "#F97316" },
  freeze: { color: "#BAE6FD", radius: 6 },
  laser: { color: "#FDE047", radius: 3 },
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
    const x = t.gx * TILE;
    const y = t.gy * TILE;
    const centerX = x + TILE / 2;
    const centerY = y + TILE / 2;

    if (t.type === "dart") {
      // Draw the image scaled to the tile size
      ctx.drawImage(doroImage, x, y, TILE, TILE);
    } else {
      // Fallback for other tower types
      ctx.beginPath();
      ctx.arc(centerX, centerY, 18, 0, Math.PI * 2);
      ctx.fillStyle = visual.color;
      ctx.fill();
    }

    // Keep the range indicator logic
    if (selectedTowerId === t.id) {
      ctx.beginPath();
      ctx.arc(centerX, centerY, t.range, 0, Math.PI * 2);
      ctx.strokeStyle = `${visual.color}55`;
      ctx.lineWidth = 2;
      ctx.stroke();
    }

    drawUpgradePips(t);
  });
}

function drawUpgradePips(tower) {
  // to decide whether to show pips for *installed* upgrades. Just resolve
  // the installed list directly.
  const upgrades = resolveUpgradeKeys(tower.upgrades || []);
  upgrades.forEach((u, i) => {
    const angle = (i / 7) * Math.PI * 2;
    const pipX = tower.gx * TILE + TILE / 2 + Math.cos(angle) * 22;
    const pipY = tower.gy * TILE + TILE / 2 + Math.sin(angle) * 22;
    ctx.beginPath();
    ctx.arc(pipX, pipY, 4, 0, Math.PI * 2);
    ctx.fillStyle = UPGRADE_META[u]?.color || "#FFFFFF";
    ctx.fill();
  });
}

function drawProjectiles(projectiles) {
  projectiles.forEach((p) => {
    const s = PROJ_STYLES[p.type] || PROJ_STYLES.dart;
    ctx.beginPath();
    ctx.arc(p.px, p.py, s.radius, 0, Math.PI * 2);
    ctx.fillStyle = s.color;
    ctx.fill();
    if (s.outline) {
      ctx.strokeStyle = s.outline;
      ctx.lineWidth = 2;
      ctx.stroke();
    }
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
    : "Colocando: ningun";

  const selected =
    (latestState.towers || []).find((t) => t.id === selectedTowerId) || null;
  const selectedPanel = document.getElementById("selected-panel");
  const selectedLabel = document.getElementById("selected-label");
  const selectedStats = document.getElementById("selected-stats");
  const installedContainer = document.getElementById("installed-upgrades");
  const availableContainer = document.getElementById("available-upgrades");
  const sellBtn = document.getElementById("sell-button");

  if (!selected) {
    selectedPanel.classList.add("hidden");
    selectedLabel.textContent = "No Doro seleccionado";
    selectedStats.textContent = "";
    installedContainer.innerHTML = "Ninguna";
    availableContainer.innerHTML = "";
    sellBtn.textContent = "Vender";
    return;
  }

  selectedPanel.classList.remove("hidden");
  selectedLabel.textContent = `${TOWER_VISUALS[selected.type]?.label || selected.type} (${selected.gx}, ${selected.gy})`;
  selectedStats.textContent = `Rango: ${Math.round(selected.range)} px | Disparo/s: ${(selected.fireRate || 0).toFixed(2)}/s`;

  const installed = resolveUpgradeKeys(selected.upgrades || []);
  installedContainer.innerHTML = installed.length
    ? installed
        .map(
          (key) =>
            `<span class="upgrade-chip">\u2705 ${UPGRADE_META[key]?.label || key}</span>`,
        )
        .join("")
    : "Ninguna";

  const available = selected.availableUpgrades || [];
  availableContainer.innerHTML = available.length
    ? available
        .map((key) => {
          const cost = UPGRADE_META[key]?.cost ?? 0;
          const canAfford = (latestState.coins || 0) >= cost;
          const label = UPGRADE_META[key]?.label || key;
          return `<button class="upgrade-btn" data-upgrade="${key}" ${canAfford ? "" : "disabled"}>${label} - ${cost}🍊</button>`;
        })
        .join("")
    : '<span class="status">Sin mejoras disponibles</span>';

  sellBtn.textContent = `Vender por ${selected.sellValue}🍊`;
}

function resolveUpgradeKeys(upgradeLabels) {
  const labelToKey = {
    "Rapid Fire": "RapidFire",
    Sniper: "Sniper",
    Explosive: "Explosive",
    Freezing: "Freezing",
    Piercing: "Piercing",
    Laser: "Laser",
    "Camo Detector": "CamoDetector",
  };
  return (upgradeLabels || []).map((label) => labelToKey[label] || label);
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

async function upgradeSelectedTower(upgrade) {
  if (!selectedTowerId) {
    return;
  }

  try {
    const response = await fetch("/api/upgrade", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ towerId: selectedTowerId, upgrade }),
    });
    if (!response.ok) {
      const text = await response.text();
      console.error("Upgrade request failed", response.status, text);
    }
  } catch (err) {
    console.error("Failed to upgrade tower", err);
  }
}

function connectEvents() {
  // The panel is only rebuilt when the selected tower's data or coin count
  // actually changes, so the upgrade buttons are never destroyed mid-click
  // by the 50 Hz SSE stream.
  let lastSelectedSnapshot = "";

  const events = new EventSource("/events");
  events.onmessage = (event) => {
    try {
      const state = JSON.parse(event.data);
      latestState = state;

      if (!(latestState.towers || []).some((t) => t.id === selectedTowerId)) {
        selectedTowerId = null;
      }

      updateHud(state);

      const selectedTower =
        (state.towers || []).find((t) => t.id === selectedTowerId) ?? null;
      const snapshot = JSON.stringify({
        selectedTowerId,
        tower: selectedTower,
        coins: state.coins,
      });
      if (snapshot !== lastSelectedSnapshot) {
        lastSelectedSnapshot = snapshot;
        updateTowerPanel();
      }

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

  document
    .getElementById("available-upgrades")
    .addEventListener("click", async (event) => {
      const target = event.target;
      if (!(target instanceof Element)) {
        return;
      }

      const button = target.closest("button[data-upgrade]");
      if (!(button instanceof HTMLButtonElement) || button.disabled) {
        return;
      }

      const upgrade = button.getAttribute("data-upgrade");
      if (!upgrade) {
        return;
      }

      await upgradeSelectedTower(upgrade);
    });

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
