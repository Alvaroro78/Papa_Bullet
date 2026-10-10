package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 'S' — policía con escudo: avanza despacio y bloquea lo que le llega DE FRENTE a la altura del escudo.
 * Se le gana por arriba (la papa bomba en parábola) o por abajo (el aceite en el piso).
 */
public class PoliceShield extends Enemy {
    private static final double SPEED = 50, SIGHT = 380;
    private double blockFlash;

    public PoliceShield() {
        super(36, 52, 80, 30, false);
    }

    @Override
    protected void act(double dt, GameWorld world, Entity player) {
        blockFlash = Math.max(0, blockFlash - dt);
        double d = distX(player);
        vx = (d < SIGHT && d > 20) ? facing * SPEED : 0;
    }

    @Override
    public void takeDamage(int dmg, Entity source) {
        if (blocks(source)) {
            blockFlash = 0.1;
            return;
        }
        takeDamage(dmg);
    }

    /** ¿El golpe viene del lado al que mira y a la altura del escudo (no desde arriba ni desde el piso)? */
    boolean blocks(Entity source) {
        boolean fromFront = Math.signum(source.getCenterX() - getCenterX()) == facing;
        boolean shieldHeight = source.getCenterY() > y && source.getCenterY() < y + height * 0.8;
        return fromFront && shieldHeight;
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawPolice(gc, cam);
        Color shield = blockFlash > 0 ? Color.web("#ffffff") : Color.web("#9fb3c8");
        part(gc, cam, 26, 8, 10, 40, Color.web("#3c4a5c")); // marco del escudo
        part(gc, cam, 27, 9, 8, 38, shield);
        part(gc, cam, 28, 16, 6, 3, Color.web("#3c4a5c"));  // ventanita
    }
}