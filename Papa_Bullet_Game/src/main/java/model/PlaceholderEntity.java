package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Rectángulo de color con vida: ocupa el lugar de un enemigo/jefe hasta que P4 entregue el real.
 * Sirve para que P3 pruebe armas ("¿la papa explota y le quita vida?") y para cerrar el nivel
 * (el jefe falso avisa world.bossDefeated() al morir).
 */
public class PlaceholderEntity extends Entity {
    private final String label;
    private final Color color;
    private final int maxHp;
    private final boolean boss;

    public PlaceholderEntity(String label, Color color, double w, double h, int hp, boolean flying, boolean boss) {
        this.label = label;
        this.color = color;
        this.width = w;
        this.height = h;
        this.hp = hp;
        this.maxHp = hp;
        this.boss = boss;
        this.team = Team.ENEMY;
        this.collidesWithTiles = !flying;
        this.gravityScale = flying ? 0 : 1;
    }

    @Override
    public void update(double dt, GameWorld world) {
        // quieto a propósito: es un blanco
    }

    @Override
    public void onDeath(GameWorld world) {
        if (boss) world.bossDefeated();
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawBox(gc, cam, color);
        double sx = Math.round(cam.toScreenX(x)), sy = Math.round(cam.toScreenY(y));
        // barra de vida
        gc.setFill(Color.BLACK);
        gc.fillRect(sx, sy - 8, width, 5);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(sx, sy - 8, width * Math.max(0, hp) / maxHp, 5);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText(label, sx + 4, sy + 16);
    }
}
