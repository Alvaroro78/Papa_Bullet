package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Fondo con parallax estilo Contra 4, dibujado con figuras (no necesita imágenes):
 * cielo en franjas, los Farallones a lo lejos, Cali en ruinas y la selva con palmas.
 * Cada capa se mueve más lento que la cámara (factor < 1): eso da la sensación de profundidad.
 * Todo es determinista (rnd con semilla fija), así el fondo no "tiembla" entre frames.
 */
public final class Background {
    private Background() {}

    private static final Color[] SKY = {
            Color.web("#0b1640"), Color.web("#13235a"), Color.web("#1b3374"), Color.web("#24478f"),
            Color.web("#2f5ea9"), Color.web("#3e77c2"), Color.web("#5291d6"), Color.web("#6daae3"), Color.web("#8cc2ec")};
    private static final int BAND = 40;

    public static void render(GraphicsContext gc, Camera cam) {
        double cx = cam.getX(), cy = cam.getY();
        sky(gc, cx);
        mountains(gc, cx * 0.12, 330 - cy * 0.05);
        ruins(gc, cx * 0.35, 470 - cy * 0.15);
        jungle(gc, cx * 0.6, 540 - cy * 0.3);
    }

    // ---------------- capas ----------------

    private static void sky(GraphicsContext gc, double cx) {
        int w = Config.WIDTH;
        for (int i = 0; i < SKY.length; i++) {
            gc.setFill(SKY[i]);
            gc.fillRect(0, i * BAND, w, BAND);
            if (i + 1 < SKY.length) {                 // tramado de 4 px entre franjas: look de 16 bits
                gc.setFill(SKY[i + 1]);
                for (int x = 0; x < w; x += 8) {
                    gc.fillRect(x, (i + 1) * BAND - 4, 4, 2);
                    gc.fillRect(x + 4, (i + 1) * BAND - 2, 4, 2);
                }
            }
        }
        gc.setFill(SKY[SKY.length - 1]);
        gc.fillRect(0, SKY.length * BAND, w, Config.HEIGHT - SKY.length * BAND);

        gc.setFill(Color.web("#dfe9ff"));             // estrellas casi quietas
        double off = cx * 0.03;
        for (int i = 0; i < 40; i++) {
            double x = mod(rnd(i, 1) * 1600 - off, 1600);
            if (x < w) gc.fillRect(Math.round(x), Math.round(rnd(i, 2) * 110), 2, 2);
        }
    }

    /** Los Farallones: picos con cara iluminada a la izquierda, sombra a la derecha y nieve arriba. */
    private static void mountains(GraphicsContext gc, double off, double base) {
        final int period = 1200;
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH; k++) {
            double ox = k * period - off;
            for (int i = 0; i < 9; i++) {
                double px = ox + i * period / 9.0 + rnd(i, 3) * 60;
                double h = 110 + rnd(i, 4) * 130, half = h * (0.9 + rnd(i, 5) * 0.5);
                double top = base - h;
                gc.setFill(Color.web("#7f9fc6"));
                gc.fillPolygon(new double[]{px - half, px, px}, new double[]{base, top, base}, 3);
                gc.setFill(Color.web("#557aa6"));
                gc.fillPolygon(new double[]{px, px, px + half}, new double[]{top, base, base}, 3);
                double s = 0.22;                       // nieve: la punta del pico
                gc.setFill(Color.web("#e6eef8"));
                gc.fillPolygon(new double[]{px - half * s, px, px}, new double[]{top + h * s, top, top + h * s * 1.25}, 3);
                gc.setFill(Color.web("#b9c9dd"));
                gc.fillPolygon(new double[]{px, px, px + half * s}, new double[]{top, top + h * s * 1.25, top + h * s}, 3);
            }
        }
        gc.setFill(Color.web("#557aa6"));
        gc.fillRect(0, base, Config.WIDTH, Config.HEIGHT - base);
    }

    /** Cali después del terremoto: edificios con el techo roto, algunas ventanas encendidas. */
    private static void ruins(GraphicsContext gc, double off, double base) {
        final int period = 960;
        Color wall = Color.web("#2c3e63"), dark = Color.web("#223152"), lit = Color.web("#d9b25a");
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH; k++) {
            double x = k * period - off;
            for (int i = 0; x < (k + 1) * period - off - 40; i++) {
                double w = 50 + rnd(i, 6) * 60, h = 70 + rnd(i, 7) * 120, top = base - h;
                double cut = 15 + rnd(i, 8) * 30;    // trozo que se cayó
                gc.setFill(wall);
                gc.fillPolygon(new double[]{x, x, x + w * 0.4, x + w * 0.55, x + w, x + w},
                        new double[]{base, top + cut, top, top + cut * 0.6, top + cut * 1.4, base}, 6);
                gc.setFill(dark);
                gc.fillRect(Math.round(x + w - 6), Math.round(top + cut * 1.4), 6, base - top - cut * 1.4);
                for (double wy = top + cut + 12; wy < base - 12; wy += 16) {
                    for (double wx = x + 8; wx < x + w - 12; wx += 14) {
                        double r = rnd((int) (wx * 7 + wy), 9);
                        if (r < 0.55) continue;           // ventanas sin vidrio / apagadas
                        gc.setFill(r > 0.93 ? lit : dark);
                        gc.fillRect(Math.round(wx), Math.round(wy), 6, 8);
                    }
                }
                x += w + 8 + rnd(i, 10) * 30;
            }
        }
        gc.setFill(wall);
        gc.fillRect(0, base, Config.WIDTH, Config.HEIGHT - base);
    }

    /** Selva y palmas, la capa más cercana. */
    private static void jungle(GraphicsContext gc, double off, double base) {
        final int period = 800;
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH + 100; k++) {
            double ox = k * period - off;
            for (int i = 0; i < 4; i++) palm(gc, ox + 60 + i * 200 + rnd(i, 11) * 80, base - 10, 120 + rnd(i, 12) * 60);
            for (int i = 0; i < 26; i++) {
                double x = ox + i * 32 + rnd(i, 13) * 10, r = 26 + rnd(i, 14) * 22;
                gc.setFill(Color.web("#1d5b33"));
                gc.fillOval(x - r, base - r * 0.9, r * 2, r * 2);
                gc.setFill(Color.web("#2b7a41"));
                gc.fillOval(x - r * 0.6, base - r * 0.85, r * 1.1, r * 0.7);
            }
        }
        gc.setFill(Color.web("#1d5b33"));
        gc.fillRect(0, base, Config.WIDTH, Config.HEIGHT - base);
    }

    private static void palm(GraphicsContext gc, double x, double base, double h) {
        gc.setStroke(Color.web("#4a3622"));
        gc.setLineWidth(6);
        double tx = x + h * 0.15, ty = base - h;
        gc.strokeLine(x, base, x + h * 0.05, base - h * 0.5);
        gc.strokeLine(x + h * 0.05, base - h * 0.5, tx, ty);
        gc.setStroke(Color.web("#17482a"));
        gc.setLineWidth(5);
        for (int f = 0; f < 7; f++) {
            double a = Math.PI * (0.05 + f / 6.0 * 0.9), len = h * 0.42;
            double mx = tx + Math.cos(a) * len * 0.6, my = ty - Math.sin(a) * len * 0.35;
            gc.strokeLine(tx, ty, mx, my);
            gc.strokeLine(mx, my, tx + Math.cos(a) * len, ty - Math.sin(a) * len * 0.1 + len * 0.25);
        }
    }

    // ---------------- utilidades ----------------

    /** Número "aleatorio" fijo en [0, 1) para (i, semilla). */
    static double rnd(int i, int seed) {
        int h = i * 374761393 + seed * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0x7fffffff) / (double) Integer.MAX_VALUE;
    }

    private static double mod(double a, double m) {
        return ((a % m) + m) % m;
    }
}