package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Explosión dibujada con círculos: hace daño UNA vez a los enemigos dentro del radio y se desvanece. */
public class Explosion extends Entity {
    private static final double DURATION = 0.35;
    private final double radius;
    private final int damage;
    private double age;
    private boolean done;

    public Explosion(double cx, double cy, double radius, int damage) {
        this.radius = radius;
        this.damage = damage;
        width = height = radius * 2;
        x = cx - radius;
        y = cy - radius;
        team = Team.NEUTRAL;
    }

    @Override
    public void update(double dt, GameWorld world) {
        if (!done) {
            done = true;
            for (Entity e : world.getEnemies()) {
                if (Math.hypot(e.getCenterX() - getCenterX(), e.getCenterY() - getCenterY()) <= radius + e.getWidth() / 2) {
                    e.takeDamage(damage, this);
                }
            }
        }
        age += dt;
        if (age >= DURATION) kill();
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        double t = age / DURATION, r = radius * (0.5 + 0.5 * t);
        double cx = cam.toScreenX(getCenterX()), cy = cam.toScreenY(getCenterY());
        gc.setGlobalAlpha(1 - t);
        gc.setFill(Color.web("#ff7a1a"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
        gc.setFill(Color.web("#ffd84a"));
        gc.fillOval(cx - r * 0.6, cy - r * 0.6, r * 1.2, r * 1.2);
        gc.setFill(Color.web("#fff6c8"));
        gc.fillOval(cx - r * 0.25, cy - r * 0.25, r * 0.5, r * 0.5);
        gc.setGlobalAlpha(1);
    }
}