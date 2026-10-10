package model;

import javafx.geometry.Rectangle2D;

/**
 * Cámara estilo Contra: sigue al jugador de izquierda a derecha y no retrocede.
 * Las entidades dibujan con {@code cam.toScreenX(x)} / {@code cam.toScreenY(y)}.
 */
public class Camera {
    private double x, y;
    private boolean oneWay = true;

    public double getX() { return x; }
    public double getY() { return y; }

    /** Si es true (por defecto) la cámara nunca retrocede y el jugador no puede salirse por la izquierda. */
    public boolean isOneWay() { return oneWay; }
    public void setOneWay(boolean oneWay) { this.oneWay = oneWay; }

    /** Coordenadas de pantalla (redondeadas: look retro, sin bordes borrosos). */
    public double toScreenX(double worldX) { return worldX - Math.round(x); }
    public double toScreenY(double worldY) { return worldY - Math.round(y); }

    /** ¿Se ve (o casi) esta caja del mundo? Útil para que un enemigo solo se active al entrar en pantalla. */
    public boolean isVisible(Rectangle2D box, double margin) {
        return box.getMaxX() > x - margin && box.getMinX() < x + Config.WIDTH + margin
                && box.getMaxY() > y - margin && box.getMinY() < y + Config.HEIGHT + margin;
    }

    public boolean isVisible(Rectangle2D box) { return isVisible(box, 0); }

    public void reset(Level level) {
        x = 0;
        y = Math.max(0, level.getHeightPx() - Config.HEIGHT); // niveles bajos quedan pegados al fondo
    }

    public void follow(Entity target, Level level) {
        double desiredX = target.getCenterX() - Config.WIDTH / 2.0;
        x = oneWay ? Math.max(x, desiredX) : desiredX;
        x = clamp(x, 0, Math.max(0, level.getWidthPx() - Config.WIDTH));
        double maxY = Math.max(0, level.getHeightPx() - Config.HEIGHT);
        y = clamp(target.getCenterY() - Config.HEIGHT / 2.0, 0, maxY);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
