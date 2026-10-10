package model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * CONTRATO 1 — todo lo que se mueve o se dibuja (jugador, enemigos, balas, aceite) hereda de aquí.
 *
 * Lo del documento (NO cambiar): x, y, width, height, vx, vy, hp, alive, update, render,
 * getBounds, takeDamage.
 *
 * Extras que añade P1 (todos opcionales, con valores por defecto que no estorban):
 *  - {@link #team}: PLAYER / ENEMY / NEUTRAL. getEnemies() devuelve los ENEMY.
 *  - Física automática: {@link #collidesWithTiles} y {@link #gravityScale}. Si los activas, el motor
 *    mueve la entidad (x += vx*dt ...), aplica gravedad y la frena contra baldosas. Tú solo
 *    pones vx / vy en update(). Tras moverse quedan {@link #onGround}, {@link #hitWall}, {@link #hitCeiling}.
 *  - {@link #onCollide}: el motor lo llama cuando dos entidades se solapan.
 *  - {@link #onDeath}: el motor lo llama UNA vez cuando alive pasa a false.
 */
public abstract class Entity {
    // ===== contrato original =====
    protected double x, y, width, height, vx, vy;
    protected int hp = 1;
    protected boolean alive = true;

    // ===== extras de P1 =====
    protected Team team = Team.NEUTRAL;
    /** Si es true el motor la frena contra '#' y '=' (las balas que "explotan al tocar algo" la necesitan). */
    protected boolean collidesWithTiles = false;
    /** 0 = no cae (helicóptero, bala recta). 1 = gravedad normal. 0.5 = flota más, etc. */
    protected double gravityScale = 0;
    /** Resultado del último paso de física. */
    protected boolean onGround, hitWall, hitCeiling;

    private double dropTimer;
    private boolean deathNotified;

    public abstract void update(double dt, GameWorld world);

    public abstract void render(GraphicsContext gc, Camera cam);

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) alive = false;
    }

    /** Daño sabiendo quién lo causó (bala, explosión, aceite). Por defecto es igual al de arriba;
     *  el policía con escudo lo sobrescribe para bloquear lo que le llega de frente. */
    public void takeDamage(int dmg, Entity source) {
        takeDamage(dmg);
    }

    // ===== hooks opcionales (sobrescribir si hace falta) =====

    /** Se llama cuando esta entidad se solapa con {@code other} (solo si ambas siguen vivas). */
    public void onCollide(Entity other, GameWorld world) {}

    /** Se llama una sola vez cuando alive pasa a false (aquí el jefe avisa: world.bossDefeated()). */
    public void onDeath(GameWorld world) {}

    // ===== utilidades =====

    /** Mata sin importar hp (caer al vacío, etc.). */
    public void kill() {
        hp = 0;
        alive = false;
    }

    /** Baja de una plataforma '=': durante 0.25 s la entidad ignora las plataformas. */
    public void dropThroughPlatform() {
        dropTimer = 0.25;
        if (vy < 120) vy = 120;
    }

    /** Dibuja la caja de la entidad con un color (para probar sin sprites). */
    protected void drawBox(GraphicsContext gc, Camera cam, Color color) {
        gc.setFill(color);
        gc.fillRect(Math.round(cam.toScreenX(x)), Math.round(cam.toScreenY(y)), width, height);
    }

    // ===== acceso para el motor y otros módulos =====

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
    public int getHp() { return hp; }
    public boolean isAlive() { return alive; }
    public Team getTeam() { return team; }
    public boolean collidesWithTiles() { return collidesWithTiles; }
    public double getGravityScale() { return gravityScale; }
    public boolean isOnGround() { return onGround; }
    public boolean isHitWall() { return hitWall; }
    public boolean isHitCeiling() { return hitCeiling; }
    public boolean isDroppingThrough() { return dropTimer > 0; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setVelocity(double vx, double vy) { this.vx = vx; this.vy = vy; }

    // --- solo para el motor ---

    public void setContacts(boolean ground, boolean wall, boolean ceiling) {
        this.onGround = ground;
        this.hitWall = wall;
        this.hitCeiling = ceiling;
    }

    public void tickTimers(double dt) {
        if (dropTimer > 0) dropTimer -= dt;
    }

    public void notifyDeath(GameWorld world) {
        if (!alive && !deathNotified) {
            deathNotified = true;
            onDeath(world);
        }
    }
}