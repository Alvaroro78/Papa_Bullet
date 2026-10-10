package model;

/**
 * Las 8 direcciones en las que el jugador puede apuntar.
 * dx/dy ya vienen normalizados (las diagonales miden 1).
 * En JavaFX la Y crece hacia ABAJO, por eso UP tiene dy = -1.
 */
public enum AimDirection {
    RIGHT(1, 0),
    LEFT(-1, 0),
    UP(0, -1),
    DOWN(0, 1),
    UP_RIGHT(1, -1),
    UP_LEFT(-1, -1),
    DOWN_RIGHT(1, 1),
    DOWN_LEFT(-1, 1);

    public final double dx;
    public final double dy;

    AimDirection(double dx, double dy) {
        double len = Math.hypot(dx, dy);
        this.dx = dx / len;
        this.dy = dy / len;
    }

    public boolean facingRight() {
        return dx > 0;
    }
}