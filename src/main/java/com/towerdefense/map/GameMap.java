package com.towerdefense.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameMap {
    public static final int COLS = 24;
    public static final int ROWS = 16;
    public static final int TILE = 48;

    private final CellType[][] grid;
    private final List<int[]> waypoints;

    private GameMap(CellType[][] grid, List<int[]> waypoints) {
        this.grid = grid;
        this.waypoints = Collections.unmodifiableList(waypoints);
    }

    public static GameMap createDefault() {
        CellType[][] grid = new CellType[COLS][ROWS];
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                grid[col][row] = CellType.BUILDABLE;
            }
        }

        List<int[]> waypoints = new ArrayList<>();
        waypoints.add(new int[] {0, 7});
        waypoints.add(new int[] {5, 7});
        waypoints.add(new int[] {5, 3});
        waypoints.add(new int[] {11, 3});
        waypoints.add(new int[] {11, 11});
        waypoints.add(new int[] {16, 11});
        waypoints.add(new int[] {16, 5});
        waypoints.add(new int[] {21, 5});
        waypoints.add(new int[] {21, 9});
        waypoints.add(new int[] {23, 9});

        for (int i = 0; i < waypoints.size() - 1; i++) {
            int[] from = waypoints.get(i);
            int[] to = waypoints.get(i + 1);
            markSegment(grid, from[0], from[1], to[0], to[1]);
        }

        return new GameMap(grid, waypoints);
    }

    public CellType getCell(int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) {
            return CellType.SCENERY;
        }
        return grid[col][row];
    }

    public boolean canPlace(int col, int row) {
        return getCell(col, row) == CellType.BUILDABLE;
    }

    public List<int[]> getWaypoints() {
        return waypoints;
    }

    private static void markSegment(CellType[][] grid, int fromCol, int fromRow, int toCol,
            int toRow) {
        if (fromCol != toCol && fromRow != toRow) {
            throw new IllegalArgumentException("Only orthogonal segments are supported");
        }

        int dc = Integer.compare(toCol, fromCol);
        int dr = Integer.compare(toRow, fromRow);

        int col = fromCol;
        int row = fromRow;
        grid[col][row] = CellType.PATH;

        while (col != toCol || row != toRow) {
            col += dc;
            row += dr;
            grid[col][row] = CellType.PATH;
        }
    }
}
