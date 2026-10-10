package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Fondo con parallax estilo Contra 4, dibujado con figuras (no necesita imágenes):
 * cielo en franjas, montañas a lo lejos, ciudad en ruinas y una capa cercana.
 * Los colores y la capa cercana (selva, desierto, ciudad, tierra quemada) salen del {@link Theme} del nivel.
 * Cada capa se mueve más lento que la cámara (factor < 1): eso da la sensación de profundidad.
 * Todo es determinista (rnd con semilla fija), así el fondo no "tiembla" entre frames.
 */
public final class Background {
    private Background() {}

    private static final int BAND = 40;

    /** Fondo del nivel 0 (lo usan los menús). */
    public static void render(GraphicsContext gc, Camera cam) {
        render(gc, cam, Theme.ALL[0]);
    }

    public static void render(GraphicsContext gc, Camera cam, Theme t) {
        double cx = cam.getX(), cy = cam.getY();
        sky(gc, cx, t);
        mountains(gc, cx * 0.12, 330 - cy * 0.05, t);
        ruins(gc, cx * 0.35, 470 - cy * 0.15, t);
        double off = cx * 0.6, base = 540 - cy * 0.3;
        switch (t.near()) {
            case Theme.DESERT -> desert(gc, off, base, t);
            case Theme.NIGHT_CITY -> nightCity(gc, off, base, t);
            case Theme.BURNT -> burnt(gc, off, base, t);
            default -> jungle(gc, off, base, t);
        }
        gc.setFill(t.nearDark());                   // relleno debajo de la capa cercana
        gc.fillRect(0, base, Config.WIDTH, Config.HEIGHT - base);
    }

    // ---------------- capas ----------------

    private static void sky(GraphicsContext gc, double cx, Theme t) {
        Color[] bands = t.sky();
        int w = Config.WIDTH;
        for (int i = 0; i < bands.length; i++) {
            gc.setFill(bands[i]);
            gc.fillRect(0, i * BAND, w, BAND);
            if (i + 1 < bands.length) {                 // tramado de 4 px entre franjas: look de 16 bits
                gc.setFill(bands[i + 1]);
                for (int x = 0; x < w; x += 8) {
                    gc.fillRect(x, (i + 1) * BAND - 4, 4, 2);
                    gc.fillRect(x + 4, (i + 1) * BAND - 2, 4, 2);
                }
            }
        }
        gc.setFill(bands[bands.length - 1]);
        gc.fillRect(0, bands.length * BAND, w, Config.HEIGHT - bands.length * BAND);

        if (t.orb() != null) {                         // sol o luna: casi quieto, detrás de las montañas
            double ox = 560 - cx * 0.02, r = t.orbR();
            gc.setFill(t.orb().deriveColor(0, 1, 1, 0.25));
            gc.fillOval(ox - r * 1.5, t.orbY() - r * 1.5, r * 3, r * 3);
            gc.setFill(t.orb());
            gc.fillOval(ox - r, t.orbY() - r, r * 2, r * 2);
        }

        if (t.stars() == null) return;
        gc.setFill(t.stars());                         // estrellas (o brasas) casi quietas
        double off = cx * 0.03;
        for (int i = 0; i < 40; i++) {
            double x = mod(rnd(i, 1) * 1600 - off, 1600);
            if (x < w) gc.fillRect(Math.round(x), Math.round(rnd(i, 2) * 110), 2, 2);
        }
    }

    /** Los Farallones: picos con cara iluminada a la izquierda, sombra a la derecha y nieve arriba. */
    private static void mountains(GraphicsContext gc, double off, double base, Theme t) {
        final int period = 1200;
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH; k++) {
            double ox = k * period - off;
            for (int i = 0; i < 9; i++) {
                double px = ox + i * period / 9.0 + rnd(i, 3) * 60;
                double h = 110 + rnd(i, 4) * 130, half = h * (0.9 + rnd(i, 5) * 0.5);
                double top = base - h;
                gc.setFill(t.farLit());
                gc.fillPolygon(new double[]{px - half, px, px}, new double[]{base, top, base}, 3);
                gc.setFill(t.farShade());
                gc.fillPolygon(new double[]{px, px, px + half}, new double[]{top, base, base}, 3);
                double s = 0.22;                       // la punta del pico (nieve, lava...)
                gc.setFill(t.peakLit());
                gc.fillPolygon(new double[]{px - half * s, px, px}, new double[]{top + h * s, top, top + h * s * 1.25}, 3);
                gc.setFill(t.peakShade());
                gc.fillPolygon(new double[]{px, px, px + half * s}, new double[]{top, top + h * s * 1.25, top + h * s}, 3);
            }
        }
        gc.setFill(t.farShade());
        gc.fillRect(0, base, Config.WIDTH, Config.HEIGHT - base);
    }

    /** Cali después del terremoto: edificios con el techo roto, algunas ventanas encendidas. */
    private static void ruins(GraphicsContext gc, double off, double base, Theme t) {
        final int period = 960;
        Color wall = t.wall(), dark = t.wallDark(), lit = t.window();
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
                        gc.setFill(r > 1 - t.litWindows() ? lit : dark);
                        gc.fillRect(Math.round(wx), Math.round(wy), 6, 8);
                    }
                }
                x += w + 8 + rnd(i, 10) * 30;
            }
        }
        gc.setFill(wall);
        gc.fillRect(0, base, Config.WIDTH, Config.HEIGHT - base);
    }

    // ---------------- capa cercana (una por tipo de nivel) ----------------

    /** Selva y palmas. */
    private static void jungle(GraphicsContext gc, double off, double base, Theme t) {
        final int period = 800;
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH + 100; k++) {
            double ox = k * period - off;
            for (int i = 0; i < 4; i++) palm(gc, ox + 60 + i * 200 + rnd(i, 11) * 80, base - 10, 120 + rnd(i, 12) * 60);
            for (int i = 0; i < 26; i++) {
                double x = ox + i * 32 + rnd(i, 13) * 10, r = 26 + rnd(i, 14) * 22;
                gc.setFill(t.nearDark());
                gc.fillOval(x - r, base - r * 0.9, r * 2, r * 2);
                gc.setFill(t.nearLit());
                gc.fillOval(x - r * 0.6, base - r * 0.85, r * 1.1, r * 0.7);
            }
        }
    }

    /** Dunas bajas y cactus. */
    private static void desert(GraphicsContext gc, double off, double base, Theme t) {
        final int period = 800;
        Color cactus = Color.web("#4f7a3a"), cactusLit = Color.web("#78a050");
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH + 100; k++) {
            double ox = k * period - off;
            for (int i = 0; i < 7; i++) {                  // dunas
                double x = ox + i * 120 + rnd(i, 20) * 40, r = 70 + rnd(i, 21) * 50;
                gc.setFill(t.nearDark());
                gc.fillOval(x - r, base - r * 0.35, r * 2, r * 0.9);
                gc.setFill(t.nearLit());
                gc.fillOval(x - r * 0.7, base - r * 0.33, r * 1.1, r * 0.25);
            }
            for (int i = 0; i < 3; i++) {                  // cactus: tronco y dos brazos
                double x = ox + 80 + i * 260 + rnd(i, 22) * 90, h = 60 + rnd(i, 23) * 50, top = base - 10 - h;
                gc.setFill(cactus);
                gc.fillRoundRect(x, top, 14, h + 10, 12, 12);
                gc.fillRoundRect(x - 14, top + h * 0.35, 14, 8, 6, 6);
                gc.fillRoundRect(x - 14, top + h * 0.12, 8, h * 0.3, 6, 6);
                gc.fillRoundRect(x + 14, top + h * 0.5, 12, 8, 6, 6);
                gc.fillRoundRect(x + 18, top + h * 0.28, 8, h * 0.3, 6, 6);
                gc.setFill(cactusLit);
                gc.fillRect(x + 3, top + 4, 3, h);
            }
        }
    }

    /** Edificios cercanos de noche y postes de luz. */
    private static void nightCity(GraphicsContext gc, double off, double base, Theme t) {
        final int period = 800;
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH + 100; k++) {
            double x = k * period - off;
            for (int i = 0; i < 8; i++) {                  // siluetas con algunas ventanas amarillas
                double w = 70 + rnd(i, 30) * 40, h = 60 + rnd(i, 31) * 90, top = base - h;
                gc.setFill(t.nearDark());
                gc.fillRect(Math.round(x), Math.round(top), w, h + 1);
                gc.setFill(t.nearLit());
                for (double wy = top + 10; wy < base - 14; wy += 18) {
                    for (double wx = x + 8; wx < x + w - 12; wx += 16) {
                        if (rnd((int) (wx * 3 + wy), 32) > 0.8) gc.fillRect(Math.round(wx), Math.round(wy), 6, 8);
                    }
                }
                x += w + 4;
            }
            for (int i = 0; i < 4; i++) {                  // postes: brillo y bombillo
                double px = k * period - off + 100 + i * 200, top = base - 110;
                gc.setFill(t.nearLit().deriveColor(0, 1, 1, 0.3));
                gc.fillOval(px - 6, top - 8, 36, 24);
                gc.setFill(Color.web("#2a2840"));
                gc.fillRect(px - 2, top, 4, 110);
                gc.fillRect(px - 2, top, 14, 4);
                gc.setFill(t.nearLit());
                gc.fillRect(px + 8, top + 4, 8, 5);
            }
        }
    }

    /** Lomas de roca y árboles quemados. */
    private static void burnt(GraphicsContext gc, double off, double base, Theme t) {
        final int period = 800;
        for (int k = (int) Math.floor(off / period); k * period - off < Config.WIDTH + 100; k++) {
            double ox = k * period - off;
            gc.setStroke(t.nearLit());
            for (int i = 0; i < 4; i++) {                  // tronco y ramas secas
                double x = ox + 60 + i * 200 + rnd(i, 40) * 80, h = 90 + rnd(i, 41) * 60, top = base - h;
                gc.setLineWidth(7);
                gc.strokeLine(x, base, x + 4, top);
                gc.setLineWidth(3);
                for (int b = 0; b < 4; b++) {
                    double by = top + h * (0.1 + b * 0.18), dir = b % 2 == 0 ? -1 : 1, len = 22 + rnd(i * 4 + b, 42) * 22;
                    gc.strokeLine(x + 3, by, x + 3 + dir * len, by - len * 0.6);
                }
            }
            for (int i = 0; i < 14; i++) {                 // lomas
                double x = ox + i * 60 + rnd(i, 43) * 20, r = 30 + rnd(i, 44) * 30;
                gc.setFill(t.nearDark());
                gc.fillOval(x - r, base - r * 0.6, r * 2, r * 1.4);
            }
        }
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