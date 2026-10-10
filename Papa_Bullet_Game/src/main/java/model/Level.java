package model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Datos de un nivel ya leído: la cuadrícula de baldosas '#' y '=', dónde nacen las entidades
 * y las zonas de jetpack. Lo crea {@link LevelLoader}; lo consultan todos vía world.getLevel().
 *
 * Fuera del nivel: izquierda y derecha cuentan como PARED, arriba y abajo como AIRE (hoyos).
 */
public class Level {

    /** Una letra de entidad del mapa y su baldosa (col, fila). */
    public record Spawn(char letter, int col, int row) {}

    private final String name;
    private final char[][] tiles; // [fila][col]: '#', '=' o '.'
    private final int cols, rows;
    private final List<Spawn> spawns;
    private final List<Rectangle2D> jetpackZones;

    Level(String name, char[][] tiles, List<Spawn> spawns, List<Rectangle2D> jetpackZones) {
        this.name = name;
        this.tiles = tiles;
        this.rows = tiles.length;
        this.cols = rows == 0 ? 0 : tiles[0].length;
        this.spawns = spawns;
        this.jetpackZones = jetpackZones;
    }

    public String getName() { return name; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public int getWidthPx() { return cols * Config.TILE; }
    public int getHeightPx() { return rows * Config.TILE; }
    public List<Spawn> getSpawns() { return Collections.unmodifiableList(spawns); }

    /** Zonas 'J' (una caja por tramo horizontal de letras J), en píxeles del mundo. */
    public List<Rectangle2D> getJetpackZones() { return Collections.unmodifiableList(jetpackZones); }

    public char tileAt(int col, int row) {
        if (col < 0 || col >= cols) return '#';        // paredes laterales
        if (row < 0 || row >= rows) return '.';        // arriba y abajo: aire
        return tiles[row][col];
    }

    /** Suelo sólido ('#'): bloquea desde todos los lados. */
    public boolean isSolid(int col, int row) { return tileAt(col, row) == '#'; }

    /** Plataforma ('='): solo se pisa desde arriba y se puede bajar de ella. */
    public boolean isPlatform(int col, int row) { return tileAt(col, row) == '='; }

    // ===== dibujo =====

    public void render(GraphicsContext gc, Camera cam) {
        int t = Config.TILE;
        int firstCol = Math.max(0, (int) Math.floor(cam.getX() / t));
        int lastCol = Math.min(cols - 1, (int) Math.floor((cam.getX() + Config.WIDTH) / t));
        int firstRow = Math.max(0, (int) Math.floor(cam.getY() / t));
        int lastRow = Math.min(rows - 1, (int) Math.floor((cam.getY() + Config.HEIGHT) / t));

        for (int r = firstRow; r <= lastRow; r++) {
            for (int c = firstCol; c <= lastCol; c++) {
                char ch = tiles[r][c];
                if (ch == '.') continue;
                double sx = Math.round(cam.toScreenX(c * t)), sy = Math.round(cam.toScreenY(r * t));
                if (ch == '#') drawSolid(gc, sx, sy, t, isSolid(c, r - 1));
                else drawPlatform(gc, sx, sy, t);
            }
        }
    }

    /** Capa de depuración: zonas de jetpack. */
    public void renderDebug(GraphicsContext gc, Camera cam) {
        gc.setFill(Color.web("#00ff88", 0.25));
        for (Rectangle2D z : jetpackZones) {
            gc.fillRect(cam.toScreenX(z.getMinX()), cam.toScreenY(z.getMinY()), z.getWidth(), z.getHeight());
        }
    }

    private static void drawSolid(GraphicsContext gc, double sx, double sy, int t, boolean coveredAbove) {
        gc.setFill(Color.web("#5a5663"));          // concreto roto
        gc.fillRect(sx, sy, t, t);
        gc.setFill(Color.web("#3c3945"));          // sombra inferior / derecha
        gc.fillRect(sx, sy + t - 4, t, 4);
        gc.fillRect(sx + t - 4, sy, 4, t);
        if (!coveredAbove) {                        // borde superior claro: se lee como "suelo"
            gc.setFill(Color.web("#8c8796"));
            gc.fillRect(sx, sy, t, 5);
        }
    }

    private static void drawPlatform(GraphicsContext gc, double sx, double sy, int t) {
        gc.setFill(Color.web("#c98b3c"));
        gc.fillRect(sx, sy, t, 8);
        gc.setFill(Color.web("#7a4f1a"));
        gc.fillRect(sx, sy + 8, t, 3);
    }

    // ===== utilidades para el LevelLoader =====

    static List<Rectangle2D> mergeRuns(List<int[]> jTiles) {
        // jTiles: pares {col,row} ordenados por fila y luego columna
        List<Rectangle2D> out = new ArrayList<>();
        int i = 0;
        while (i < jTiles.size()) {
            int row = jTiles.get(i)[1], start = jTiles.get(i)[0], end = start;
            int j = i + 1;
            while (j < jTiles.size() && jTiles.get(j)[1] == row && jTiles.get(j)[0] == end + 1) {
                end++;
                j++;
            }
            out.add(new Rectangle2D(start * Config.TILE, row * Config.TILE, (end - start + 1) * Config.TILE, Config.TILE));
            i = j;
        }
        return out;
    }
}
