package model;

/**
 * Convierte una AimDirection en números para moverse o disparar (lo que antes estaba dentro del enum).
 * En pantalla, y negativo es hacia arriba. P3 usa dx/dy para la velocidad de sus balas.
 */
public final class AimVector {

    private AimVector() { }

    /** -1 izquierda, 0 nada, 1 derecha. */
    public static int sx(AimDirection d) {
        return switch (d) {
            case RIGHT, UP_RIGHT, DOWN_RIGHT -> 1;
            case LEFT, UP_LEFT, DOWN_LEFT -> -1;
            default -> 0;
        };
    }

    /** -1 arriba, 0 nada, 1 abajo. */
    public static int sy(AimDirection d) {
        return switch (d) {
            case DOWN, DOWN_RIGHT, DOWN_LEFT -> 1;
            case UP, UP_RIGHT, UP_LEFT -> -1;
            default -> 0;
        };
    }

    /** Componente x normalizada: multiplíquenla por la velocidad de la bala. */
    public static double dx(AimDirection d) {
        return sx(d) / Math.hypot(sx(d), sy(d));
    }

    /** Componente y normalizada. */
    public static double dy(AimDirection d) {
        return sy(d) / Math.hypot(sx(d), sy(d));
    }

    /** La dirección que corresponde a (sx, sy), cada uno en {-1, 0, 1}. */
    public static AimDirection of(int sx, int sy) {
        for (AimDirection d : AimDirection.values()) {
            if (sx(d) == sx && sy(d) == sy) return d;
        }
        throw new IllegalArgumentException("Dirección inválida: " + sx + ", " + sy);
    }
}