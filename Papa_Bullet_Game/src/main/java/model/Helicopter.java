package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** 'H' — helicóptero de la policía: va y viene en el aire alrededor de donde nace y dispara hacia abajo. */
public class Helicopter extends Enemy {
    private static final double SWING = 140, FIRE_RATE = 2.0, BULLET_SPEED = 280;
    private double homeX = Double.NaN, homeY, time;

    public Helicopter() {
        super(72, 36, 120, 30, true);
        cooldown = 1;
    }

    @Override
    protected void act(double dt, GameWorld world, Entity player) {
        if (Double.isNaN(homeX)) { homeX = x; homeY = y; } // la posición la pone el LevelLoader después de crearlo
        time += dt;
        x = homeX + Math.sin(time * 0.8) * SWING;
        y = homeY + Math.sin(time * 2.3) * 8;
        if (cooldown == 0) {
            shootAt(world, player, getCenterX(), y + height, BULLET_SPEED);
            cooldown = FIRE_RATE;
        }
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        part(gc, cam, 50, 14, 22, 6, Color.web("#2f4a3a"));   // cola
        part(gc, cam, 66, 8, 4, 14, Color.web("#2f4a3a"));
        part(gc, cam, 8, 10, 46, 22, Color.web("#3d6b4f"));   // cuerpo
        part(gc, cam, 8, 26, 46, 6, Color.web("#2f4a3a"));
        part(gc, cam, 10, 13, 16, 10, Color.web("#9fd3f0"));  // ventana (adelante)
        part(gc, cam, 14, 34, 34, 3, Color.web("#222222"));   // patines
        part(gc, cam, 28, 4, 6, 6, Color.web("#222222"));     // eje
        double blade = 34 * Math.abs(Math.sin(time * 30));    // hélice girando
        part(gc, cam, 31 - blade, 2, blade * 2, 3, Color.web("#cfcfcf"));
        part(gc, cam, 30, 18, 12, 4, Color.web("#ffd24a"));  // franja "POLICÍA"
    }
}