package com.nightcity.web;

import com.nightcity.GameState;
import com.nightcity.model.Enemy;
import com.nightcity.tower.PlacedTower;
import com.nightcity.tower.Tower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builds the complete HTML page for the game UI.
 * Pure function: takes a GameState snapshot and returns an HTML string.
 * No JavaScript frameworks — just plain HTML forms + CSS.
 */
public class HtmlBuilder {

    private HtmlBuilder() {}

    public static String build(GameState s) {
        StringBuilder sb = new StringBuilder(8000);
        head(sb);
        banner(sb);
        statusBar(sb, s);
        pathMap(sb, s);
        towerTable(sb, s);
        actionGrid(sb, s);
        simControls(sb);
        quickRef(sb);
        logPanel(sb, s);
        sb.append("</body></html>");
        return sb.toString();
    }

    // ── Section builders ──────────────────────────────────────────────

    private static void head(StringBuilder sb) {
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
          .append("<title>NCPD TOWER DEFENSE // NIGHT CITY</title><style>")
          .append("*{box-sizing:border-box}")
          .append("body{background:#07070e;color:#00ff9f;")
          .append(     "font-family:'Courier New',monospace;padding:16px;margin:0;font-size:13px}")
          .append("h1{color:#ff00aa;text-shadow:0 0 14px #ff00aa;margin:0 0 10px;font-size:20px}")
          .append("h2{color:#00cfff;margin:14px 0 4px;font-size:13px;letter-spacing:1px}")
          .append(".panel{border:1px solid #1a4a2a;padding:10px 14px;margin:6px 0;background:#0b0b18}")
          .append(".stat{color:#ffee00}")
          .append(".warn{color:#ff8800}")
          .append(".dead{color:#ff2244;font-weight:bold}")
          .append("input,select{background:#0e0e1e;color:#00ff9f;border:1px solid #00ff9f;")
          .append(             "padding:3px 7px;font-family:inherit;font-size:12px;margin:2px}")
          .append("input[type=submit]{background:#110920;color:#ff00aa;")
          .append(                  "border:1px solid #ff00aa;padding:5px 13px;")
          .append(                  "cursor:pointer;font-weight:bold;margin:2px}")
          .append("input[type=submit]:hover{background:#ff00aa;color:#07070e}")
          .append("table{border-collapse:collapse;width:100%;font-size:11px}")
          .append("td,th{border:1px solid #1a3a2a;padding:3px 8px}")
          .append("th{color:#00cfff;background:#0e0e1e}")
          .append("pre{color:#88ffcc;font-size:11px;max-height:280px;overflow-y:auto;")
          .append(   "margin:0;white-space:pre-wrap;word-break:break-all;line-height:1.4}")
          .append("form{display:inline}")
          .append(".grid{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:6px}")
          .append(".lbl{color:#556655}")
          .append("</style></head><body>");
    }

    private static void banner(StringBuilder sb) {
        sb.append("<h1>// NIGHT CITY TOWER DEFENSE — NCPD SECTOR 7 //</h1>");
    }

    private static void statusBar(StringBuilder sb, GameState s) {
        String hpClass = s.coreHp <= 30 ? "dead" : "stat";
        sb.append("<div class='panel'>")
          .append("<span class='lbl'>CORE HP:</span> ")
          .append("<span class='").append(hpClass).append("'>").append(s.coreHp).append("</span>")
          .append("  &nbsp;|&nbsp;  <span class='lbl'>WAVE:</span> ")
          .append("<span class='stat'>").append(s.wave).append("</span>")
          .append("  &nbsp;|&nbsp;  <span class='lbl'>EDDIES:</span> ")
          .append("<span class='stat'>").append(s.eddies).append("</span>")
          .append("  &nbsp;|&nbsp;  <span class='lbl'>RAM:</span> ")
          .append("<span class='stat'>").append(s.ram).append("</span>");
        if (s.running)    sb.append("  &nbsp;|&nbsp;  <span class='warn'>[[ WAVE ACTIVE ]]</span>");
        if (s.coreHp <= 0) sb.append("  &nbsp;|&nbsp;  <span class='dead'>[[ CORE DESTROYED ]]</span>");
        sb.append("</div>");
    }

    private static void pathMap(StringBuilder sb, GameState s) {
        sb.append("<h2>// PATH MAP  S=Spawn  T=Tower  E=Enemy  C=Core</h2>")
          .append("<div class='panel'><pre>");

        char[] path = new char[101];
        Arrays.fill(path, '.');
        path[0]   = 'S';
        path[100] = 'C';
        for (int slot : GameState.SLOTS) path[slot] = 'T';
        for (Enemy e : s.enemies) {
            int p = (int) Math.min(99, Math.max(0, e.position));
            if (path[p] == '.' || path[p] == 'S') path[p] = 'E';
        }
        sb.append(new String(path)).append("\n");
        sb.append("0         10        20        30        40        50")
          .append("        60        70        80        90       100\n\n");

        if (s.enemies.isEmpty()) {
            sb.append("[ No active enemies ]\n");
        } else {
            for (Enemy e : s.enemies) sb.append(e.status()).append("\n");
        }
        sb.append("</pre></div>");
    }

    private static void towerTable(StringBuilder sb, GameState s) {
        sb.append("<h2>// DEPLOYED TURRETS</h2><div class='panel'>");
        if (s.towers.isEmpty()) {
            sb.append("<span class='warn'>No turrets deployed. Use DEPLOY below.</span>");
        } else {
            sb.append("<table><tr><th>#</th><th>Slot</th><th>DMG</th>")
              .append("<th>Range</th><th>Fire/s</th><th>Module Stack</th></tr>");
            for (int i = 0; i < s.towers.size(); i++) {
                PlacedTower pt = s.towers.get(i);
                Tower t = pt.tower;
                sb.append("<tr>")
                  .append("<td>T").append(i).append("</td>")
                  .append("<td>").append(pt.pos).append("</td>")
                  .append("<td>").append(String.format("%.0f", t.getDamage())).append("</td>")
                  .append("<td>").append(String.format("%.0f", t.getRange())).append("</td>")
                  .append("<td>").append(String.format("%.2f", t.getFireRate())).append("</td>")
                  .append("<td>").append(t.getDescription()).append("</td>")
                  .append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("</div>");
    }

    private static void actionGrid(StringBuilder sb, GameState s) {
        sb.append("<div class='grid'>");

        // Deploy form
        sb.append("<div><h2>// DEPLOY TURRET  [50 Eddies]</h2><div class='panel'>")
          .append("<form method='POST' action='/action'>")
          .append("<input type='hidden' name='cmd' value='place'>")
          .append("<span class='lbl'>Slot: </span>")
          .append("<select name='pos'>");
        for (int slot : GameState.SLOTS) {
            boolean free = s.isSlotFree(slot);
            sb.append("<option value='").append(slot).append("'")
              .append(free ? "" : " disabled").append(">")
              .append(slot).append(free ? " [free]" : " [occupied]")
              .append("</option>");
        }
        sb.append("</select> &nbsp;")
          .append("<input type='submit' value='DEPLOY TURRET'>")
          .append("</form></div></div>");

        // Install mod form
        sb.append("<div><h2>// INSTALL MODULE</h2><div class='panel'>")
          .append("<form method='POST' action='/action'>")
          .append("<input type='hidden' name='cmd' value='upgrade'>")
          .append("<span class='lbl'>Tower: </span>")
          .append("<select name='idx'>");
        if (s.towers.isEmpty()) {
            sb.append("<option value='-1'>-- no turrets --</option>");
        } else {
            for (int i = 0; i < s.towers.size(); i++) {
                sb.append("<option value='").append(i).append("'>T")
                  .append(i).append(" (slot=").append(s.towers.get(i).pos)
                  .append(")</option>");
            }
        }
        sb.append("</select> ")
          .append("<span class='lbl'>Mod: </span>")
          .append("<select name='dec'>")
          .append("<option value='quickhack'>Contagion Quickhack  [3 RAM  | +5 DoT]</option>")
          .append("<option value='smartlink'>Militech SmartLink   [40E    | DMG x1.5]</option>")
          .append("<option value='techshot' >Kang Tao Railgun Mod [60E    | Range +15]</option>")
          .append("<option value='sandevistan'>QianT Sandevistan  [5 RAM  | FRate x2]</option>")
          .append("</select> &nbsp;")
          .append("<input type='submit' value='INSTALL MOD'>")
          .append("</form></div></div>");

        sb.append("</div>"); // end grid
    }

    private static void simControls(StringBuilder sb) {
        sb.append("<h2>// SIMULATION CONTROL</h2><div class='panel'>")
          .append(formBtn("wave",  "START NEXT WAVE")).append("  &nbsp; ")
          .append(formBtn("tick",  "STEP 1 TICK"))    .append("  &nbsp; ")
          .append(formBtn("run5",  "RUN 5 TICKS"))    .append("  &nbsp; ")
          .append(formBtn("run20", "RUN 20 TICKS"))
          .append("</div>");
    }

    private static String formBtn(String cmd, String label) {
        return "<form method='POST' action='/action'>"
             + "<input type='hidden' name='cmd' value='" + cmd + "'>"
             + "<input type='submit' value='" + label + "'>"
             + "</form>";
    }

    private static void quickRef(StringBuilder sb) {
        sb.append("<h2>// QUICK REFERENCE</h2><div class='panel'><pre>")
          .append("Deploy Basic Turret           50 Eddies   slots: 20, 40, 60, 80\n")
          .append("Contagion Quickhack Node       3 RAM       +5 flat DoT per shot\n")
          .append("Militech SmartLink            40 Eddies   Damage x1.5\n")
          .append("Kang Tao Tech Railgun Mod     60 Eddies   Range +15 (piercing)\n")
          .append("QianT Sandevistan Overclock    5 RAM       Fire rate x2\n")
          .append("Kill reward: +10 Eddies  |  Enemy breach: -20 Core HP\n")
          .append("New wave bonus: +25 Eddies +2 RAM  |  Mods stack freely\n")
          .append("</pre></div>");
    }

    private static void logPanel(StringBuilder sb, GameState s) {
        sb.append("<h2>// NETRUNNER LOG  (newest first)</h2>")
          .append("<div class='panel'><pre>");
        List<String> reversed = new ArrayList<>(s.log);
        Collections.reverse(reversed);
        if (reversed.isEmpty()) {
            sb.append("[ Log empty — deploy turrets and start a wave ]\n");
        } else {
            for (String line : reversed) sb.append(line).append("\n");
        }
        sb.append("</pre></div>");
    }
}
