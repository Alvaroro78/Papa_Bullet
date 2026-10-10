package model;

/** Bando de una entidad: permite que armas y enemigos se reconozcan sin conocer el código del otro. */
public enum Team {
    PLAYER,   // jugador y sus balas
    ENEMY,    // policías, helicópteros, jefes (y sus balas)
    NEUTRAL   // King Papa, aceite, explosiones, etc.
}
