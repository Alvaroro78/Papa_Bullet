package model;

/** Tamaños y constantes físicas fijas del juego (contrato 5 del documento). */
public final class Config {
    private Config() {}

    /** Ventana 800x600. */
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    /** Baldosa de 40 px => 20 columnas x 15 filas visibles. */
    public static final int TILE = 40;
    public static final int VISIBLE_ROWS = HEIGHT / TILE;

    /** Física (px y segundos). Salto de ~136 px (3.4 baldosas) con vy = -700. */
    public static final double GRAVITY = 1800;
    public static final double MAX_FALL_SPEED = 900;

    /** dt máximo por frame: evita saltos enormes tras una pausa o un lag. */
    public static final double MAX_DT = 0.05;
}
