package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** 'G' — policía con pistola: se queda quieto y le dispara al jugador cada FIRE_RATE segundos. */
public class PolicePistol extends Enemy {
    private static final double FIRE_RATE = 1.6, RANGE = 520, BULLET_SPEED = 320;

    public PolicePistol() {
        super(30, 52, 60, 30, false);
        cooldown = 0.8; // no dispara apenas aparece
    }

    @Override
    protected void act(double dt, GameWorld world, Entity player) {
        vx = 0;
        if (cooldown == 0 && distX(player) < RANGE) {
            shootAt(world, player, getCenterX() + facing * 20, y + 20, BULLET_SPEED);
            cooldown = FIRE_RATE;
        }
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawPolice(gc, cam);
        part(gc, cam, 20, 18, 12, 4, Color.web("#2f5fb3")); // brazo estirado
        part(gc, cam, 28, 15, 8, 5, Color.web("#222222"));  // pistola
    }
}