package model;

import javafx.geometry.Rectangle2D;

import java.util.List;

/**
 * CONTRATO DEL DÍA 1 (dueño: P1). Lo único que las entidades saben del mundo.
 * Aquí está solo lo que usa el jugador; P1 agrega getPlayer(), getEnemies(), etc.
 */
public interface GameWorld {
    int TILE = 40;

    /** Baldosa '#': nada la atraviesa. */
    boolean isSolid(int col, int row);

    /** Baldosa '=': se pisa desde arriba, se atraviesa desde abajo y se puede bajar de ella. */
    boolean isPlatform(int col, int row);

    /** Zonas 'J' (en coordenadas del mundo, en píxeles). */
    List<Rectangle2D> getJetpackZones();

    /** King Papas 'K' (en coordenadas del mundo, en píxeles). */
    List<Rectangle2D> getKingPapas();

    /** Agrega una entidad nueva (balas, explosiones...) al siguiente frame. */
    void spawn(Entity e);
}