package model;

/**
 * CONTRATO ENTRE P2 Y P3. El jugador solo sabe llamar fire(); cada arma
 * (ThrowerPapa, FrenchSpikes, OilSlick) decide qué crea con world.spawn(...).
 */
public interface Weapon {
    /** (x, y) = boca del arma en coordenadas del mundo. */
    void fire(double x, double y, AimDirection dir, GameWorld world);

    /** Nombre para el HUD. */
    String getName();

    /** Imagen de /assets/sprites/ para el HUD y para el objeto que se recoge. */
    String getIcon();
}