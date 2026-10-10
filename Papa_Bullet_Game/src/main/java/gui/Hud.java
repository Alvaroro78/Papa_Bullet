package gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Config;
import model.GameWorld;
import model.Overlay;
import model.Player;
import model.Sprites;
import model.Weapon;

import java.util.List;

/**
 * HUD estilo Contra (P5 lo puede retocar): "1P", barra de vida por segmentos y los espacios
 * de las 3 armas. El arma que se está usando tiene el marco dorado.
 */
public class Hud implements Overlay {
    private static final int SLOTS = 3, SEGMENTS = 15;
    private static final Font BIG = Font.font("Monospaced", FontWeight.BOLD, 20);
    private static final Font SMALL = Font.font("Monospaced", FontWeight.BOLD, 11);
    private static final Color GOLD = Color.web("#ffd24a");
    private static final double HINT_SIZE = 15;

    @Override
    public void render(GraphicsContext gc, GameWorld world) {
        if (!(world.getPlayer() instanceof Player p)) return;
        double x = 16, y = 16;

        gc.setFont(BIG);
        shadowText(gc, "1P", x, y + 18, Color.WHITE);

        // fila 1: vida en 15 segmentos (verde -> amarillo -> rojo) y el número
        double bx = x + 40, bw = 150, bh = 14;
        double ratio = Math.max(0, p.getHp()) / (double) Player.MAX_HP;
        gc.setFill(Color.BLACK);
        gc.fillRect(bx - 2, y + 4, bw + 4, bh + 4);
        gc.setFill(Color.web("#3a0d0d"));
        gc.fillRect(bx, y + 6, bw, bh);
        gc.setFill(ratio > 0.5 ? Color.web("#5ee05a") : ratio > 0.25 ? Color.web("#f2d23a") : Color.web("#e8433a"));
        int filled = (int) Math.ceil(ratio * SEGMENTS);
        double seg = bw / SEGMENTS;
        for (int i = 0; i < filled; i++) gc.fillRect(bx + i * seg + 1, y + 6, seg - 2, bh);
        gc.setFont(SMALL);
        shadowText(gc, String.valueOf(Math.max(0, p.getHp())), bx + bw + 8, y + 18, Color.WHITE);

        // fila 2: las 3 armas; vacío = gris, recogida = inicial, actual = marco dorado
        List<Weapon> weapons = p.getWeapons();
        Weapon current = p.getCurrentWeapon();
        double sy = y + 30;
        for (int i = 0; i < SLOTS; i++) {
            double cx = x + i * 34;
            Weapon w = i < weapons.size() ? weapons.get(i) : null;
            gc.setFill(Color.BLACK);
            gc.fillRect(cx, sy, 28, 28);
            gc.setFill(w == null ? Color.web("#2b2b38") : w == current ? GOLD : Color.web("#8a7a3a"));
            gc.fillRect(cx + 2, sy + 2, 24, 24);
            gc.setFill(w == null ? Color.web("#1a1a22") : Color.web("#3a1a0c"));
            gc.fillRect(cx + 4, sy + 4, 20, 20);
            Image icon = w == null ? null : Sprites.get(w.getIcon(), 20, 20);
            if (icon != null) {
                gc.drawImage(icon, cx + 14 - icon.getWidth() / 2, sy + 14 - icon.getHeight() / 2);
            } else if (w != null) {
                gc.setFont(BIG);
                gc.setFill(Color.WHITE);
                gc.fillText(w.getName().substring(0, 1).toUpperCase(), cx + 8, sy + 21);
            }
        }
        if (current != null) {
            gc.setFont(SMALL);
            shadowText(gc, current.getName().toUpperCase(), x + SLOTS * 34 + 4, sy + 18, GOLD);
        }

        String hint = world.getLevel().hintAt(p.getCenterX());
        if (hint != null) drawHint(gc, hint);
    }

    /** Cartel del tutorial arriba al centro: panel azul noche con borde dorado (igual que los menús). */
    private static void drawHint(GraphicsContext gc, String text) {
        double w = text.length() * HINT_SIZE * 0.6 + 32, h = 30;   // Monospaced: cada letra mide ~0.6 del tamaño
        double bx = (Config.WIDTH - w) / 2, by = 92;   // debajo de las dos filas del HUD
        gc.setFill(Color.web("#080e28", 0.88));
        gc.fillRect(bx, by, w, h);
        gc.setStroke(GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(bx, by, w, h);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, HINT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#ffe14a"));
        gc.fillText(text, Config.WIDTH / 2.0, by + 20);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private static void shadowText(GraphicsContext gc, String text, double x, double y, Color color) {
        gc.setFill(Color.BLACK);
        gc.fillText(text, x + 2, y + 2);
        gc.setFill(color);
        gc.fillText(text, x, y);
    }
}