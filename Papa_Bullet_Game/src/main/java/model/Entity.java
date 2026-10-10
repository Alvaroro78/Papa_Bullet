package model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * CONTRATO DEL DÍA 1 (dueño: P1). Todo lo que se mueve o se dibuja hereda de aquí.
 * Cambio frente al documento: render recibe solo el GraphicsContext, porque P1 aplica
 * la cámara una sola vez con gc.translate(-camX, -camY) antes de dibujar todo.
 */
public abstract class Entity {
    protected double x, y, width, height, vx, vy;
    protected int hp = 1;
    protected boolean alive = true;

    public abstract void update(double dt, GameWorld world);

    public abstract void render(GraphicsContext gc);

    public Rectangle2D getBounds() { return new Rectangle2D(x, y, width, height); }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) alive = false;
    }

    public boolean isAlive() { return alive; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getHp() { return hp; }
}
