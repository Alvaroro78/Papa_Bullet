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

    /** Mensaje del tutorial: se muestra mientras el jugador está desde esa columna hasta el siguiente mensaje. */
    public record Hint(int col, String text) {}

    private final String name;
    private final char[][] tiles; // [fila][col]: '#', '=' o '.'
    private final int cols, rows;
    private final List<Spawn> spawns;
    private final List<Rectangle2D> jetpackZones;
    private final List<Hint> hints;

    Level(String name, char[][] tiles, List<Spawn> spawns, List<Rectangle2D> jetpackZones, List<Hint> hints) {
        this.name = name;
        this.tiles = tiles;
        this.rows = tiles.length;
        this.cols = rows == 0 ? 0 : tiles[0].length;
        this.spawns = spawns;
        this.jetpackZones = jetpackZones;
        this.hints = hints;
    }

    public String getName() { return name; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public int getWidthPx() { return cols * Config.TILE; }
    public int getHeightPx() { return rows * Config.TILE; }
    public List<Spawn> getSpawns() { return Collections.unmodifiableList(spawns); }

    /** Zonas 'J' (una caja por tramo horizontal de letras J), en píxeles del mundo. */
    public List<Rectangle2D> getJetpackZones() { return Collections.unmodifiableList(jetpackZones); }

    /** El mensaje del tutorial que toca en la posición x (píxeles), o null si no hay. */
    public String hintAt(double x) {
        String text = null;
        for (Hint h : hints) {                   // vienen ordenados por columna
            if (h.col() * Config.TILE <= x) text = h.text();
        }
        return text;
    }

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
                if (ch == '#') drawSolid(gc, sx, sy, t, c, r);
                else drawPlatform(gc, sx, sy, t, c);
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

    // Roca café con pasto encima, como los acantilados de Contra 4. Las piedras salen de
    // Background.rnd(col, fila): siempre las mismas en la misma baldosa (no titilan).
    private static final Color[] ROCK = {Color.web("#8a5a2c"), Color.web("#7a4e26"), Color.web("#6a4320")};
    private static final Color STONE = Color.web("#5c3a1b"), STONE_LIT = Color.web("#a8723c"), EDGE = Color.web("#3e2510");
    private static final Color GRASS = Color.web("#3c9c32"), GRASS_LIT = Color.web("#7bd052"), GRASS_DARK = Color.web("#2a7524");

    private void drawSolid(GraphicsContext gc, double sx, double sy, int t, int c, int r) {
        int depth = 0;                                       // cuántas baldosas sólidas hay encima (más hondo = más oscuro)
        while (depth < ROCK.length - 1 && isSolid(c, r - depth - 1)) depth++;
        gc.setFill(ROCK[depth]);
        gc.fillRect(sx, sy, t, t);

        for (int k = 0; k < 3; k++) {                        // piedras
            double rx = sx + 3 + Background.rnd(c * 31 + k, r) * (t - 14);
            double ry = sy + 12 + Background.rnd(r * 17 + k, c) * (t - 22);
            double size = 5 + Background.rnd(c + r * 7, k) * 5;
            gc.setFill(STONE);
            gc.fillRect(Math.round(rx), Math.round(ry), size, size * 0.7);
            gc.setFill(STONE_LIT);
            gc.fillRect(Math.round(rx), Math.round(ry), size, 2);
        }
        gc.setFill(EDGE);                                    // borde oscuro donde la roca termina
        if (!isSolid(c - 1, r)) gc.fillRect(sx, sy, 3, t);
        if (!isSolid(c + 1, r)) gc.fillRect(sx + t - 3, sy, 3, t);
        if (!isSolid(c, r + 1) && r + 1 < rows) gc.fillRect(sx, sy + t - 3, t, 3);

        if (!isSolid(c, r - 1)) drawGrass(gc, sx, sy, t, c, 10);
    }

    /** Plataforma '=': un borde de pasto con tierra colgando. */
    private void drawPlatform(GraphicsContext gc, double sx, double sy, int t, int c) {
        gc.setFill(ROCK[1]);
        gc.fillRect(sx, sy + 8, t, 6);
        for (int bx = 0; bx < t; bx += 5) {                  // tierra que cuelga
            gc.setFill(ROCK[2]);
            gc.fillRect(sx + bx, sy + 14, 5, 1 + Background.rnd(c * 9 + bx, 5) * 5);
        }
        drawGrass(gc, sx, sy, t, c, 8);
    }

    private static void drawGrass(GraphicsContext gc, double sx, double sy, int t, int c, int thick) {
        gc.setFill(GRASS);
        gc.fillRect(sx, sy, t, thick);
        gc.setFill(GRASS_LIT);
        gc.fillRect(sx, sy, t, 3);
        for (int bx = 0; bx < t; bx += 4) {
            gc.setFill(GRASS_DARK);                          // hojas que caen sobre la roca
            gc.fillRect(sx + bx, sy + thick, 4, 1 + Background.rnd(c * 13 + bx, 1) * 6);
            if (Background.rnd(c * 11 + bx, 2) > 0.6) {      // matitas que sobresalen arriba (solo se ven, no chocan)
                gc.setFill(GRASS_LIT);
                gc.fillRect(sx + bx, sy - 3, 2, 3);
            }
        }
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