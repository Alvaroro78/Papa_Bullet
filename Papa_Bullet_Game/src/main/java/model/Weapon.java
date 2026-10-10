package model;


/**
 * CONTRATO ENTRE P2 Y P3. El jugador solo sabe llamar fire(); cada arma
 * (ThrowerPapa, FrenchSpikes, OilSlick) decide qué crea con world.spawn(...).
 */
public interface Weapon {
    /** (x, y) = boca del arma en coordenadas del mundo. */
    void fire(double x, double y, AimDirection dir, GameWorld world);


    /** Segundos mínimos entre un disparo y el siguiente (más alto = más difícil). */
    double getCooldown();


    /**
     * Cuánto calienta la máquina cada disparo (0..1; 1 = la deja al rojo de una).
     * Más calor => el contador de vida/tiempo baja más rápido. Por defecto 0.15.
     */
    default double getHeat() { return 0.15; }


    /** Nombre para el HUD. */
    String getName();


    /** Imagen de /assets/sprites/ para el HUD y para el objeto que se recoge. */
    String getIcon();
}


