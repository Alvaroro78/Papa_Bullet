package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** 'R' — policía con porra: camina hacia el jugador y lo golpea al tocarlo. */
public class PoliceBaton extends Enemy {
    private static final double SPEED = 90, SIGHT = 420;

    public PoliceBaton() {
        super(30, 52, 60, 40, false);
    }

    @Override
    protected void act(double dt, GameWorld world, Entity player) {
        double d = distX(player);
        vx = (d < SIGHT && d > 16) ? facing * SPEED : 0;
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawPolice(gc, cam);
        part(gc, cam, 24, 10, 4, 20, Color.web("#2a2a2a")); // porra levantada
        part(gc, cam, 22, 26, 6, 4, Color.web("#d8a07a"));  // mano
    }
}