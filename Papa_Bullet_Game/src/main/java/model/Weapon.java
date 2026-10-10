package model;

/**
 * CONTRATO ENTRE P2 Y P3. El jugador solo sabe llamar fire(); cada arma de P3
 * (Thrower Papa, French Spikes, Oil Slick) decide qué balas crea con world.spawn(...).
 */
public interface Weapon {
    /** (x, y) = boca del arma en coordenadas del mundo. */
    void fire(double x, double y, AimDirection dir, GameWorld world);

    /** Nombre para el HUD de P5. */
    String getName();
}