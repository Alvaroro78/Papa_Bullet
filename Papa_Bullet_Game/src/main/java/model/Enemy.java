package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


/**
 * Base de todos los enemigos (P4). Hace lo común y cada subclase solo escribe act() y render():
 *  - solo se mueve cuando la cámara lo ve (no disparan desde fuera de la pantalla),
 *  - su cooldown solo corre en pantalla (no disparan apenas aparecen),
 *  - mira hacia el jugador,
 *  - los que caminan NO se bajan de la plataforma o el borde en el que están,
 *  - le hace daño al jugador si lo toca,
 *  - parpadea en blanco cuando recibe un golpe.
 */
public abstract class Enemy extends Entity {
    protected final int maxHp;
    protected final int contactDamage;
    protected int facing = -1;
    protected double hitFlash, cooldown;


    protected Enemy(double w, double h, int hp, int contactDamage, boolean flying) {
        width = w;
        height = h;
        this.hp = hp;
        this.maxHp = hp;
        this.contactDamage = contactDamage;
        team = Team.ENEMY;
        collidesWithTiles = !flying;
        gravityScale = flying ? 0 : 1;
    }


    /** Lo que hace el enemigo este frame (solo se llama si está en pantalla y el jugador vive). */
    protected abstract void act(double dt, GameWorld world, Entity player);


    @Override
    public final void update(double dt, GameWorld world) {
        hitFlash = Math.max(0, hitFlash - dt);
        Entity player = world.getPlayer();
        if (player == null || !player.isAlive() || !world.getCamera().isVisible(getBounds(), 40)) {
            vx = 0;
            return;
        }
        cooldown = Math.max(0, cooldown - dt); // antes corría fuera de pantalla y disparaban al instante de aparecer
        facing = player.getCenterX() < getCenterX() ? -1 : 1;
        act(dt, world, player);
        // Los de a pie no se bajan de su plataforma: si adelante no hay piso, se quedan en el borde.
        if (gravityScale > 0 && onGround && vx != 0 && ledgeAhead(world.getLevel(), (int) Math.signum(vx))) vx = 0;
    }


    /** ¿Justo delante de sus pies (hacia dir) se acaba el piso? */
    protected boolean ledgeAhead(Level lv, int dir) {
        double footX = dir > 0 ? x + width + 2 : x - 2;
        int col = (int) Math.floor(footX / Config.TILE);
        int row = (int) Math.floor((y + height + 1) / Config.TILE);
        return !lv.isSolid(col, row) && !lv.isPlatform(col, row);
    }


    @Override
    public void takeDamage(int dmg) {
        super.takeDamage(dmg);
        hitFlash = 0.08;
    }


    @Override
    public void onCollide(Entity other, GameWorld world) {
        if (other.getTeam() == Team.PLAYER && other == world.getPlayer()) other.takeDamage(contactDamage, this);
    }


    /**
     * Apuntado con las MISMAS 8 direcciones del jugador (AimDirection): devuelve la más cercana a (dx, dy).
     * Si la más cercana es DOWN y no se permite (un policía de a pie no dispara hacia sus pies), devuelve null.
     */
    protected static AimDirection snapAim(double dx, double dy, boolean allowStraightDown) {
        int sector = (int) Math.round(Math.atan2(dy, dx) / (Math.PI / 4)); // -4..4, cada uno de 45°
        double a = sector * Math.PI / 4;
        AimDirection d = AimVector.of((int) Math.round(Math.cos(a)), (int) Math.round(Math.sin(a)));
        return (d == AimDirection.DOWN && !allowStraightDown) ? null : d;
    }


    /** Dispara una bala enemiga desde (x, y) en una de las 8 direcciones. */
    protected void shootDir(GameWorld world, AimDirection dir, double x, double y, double speed) {
        world.spawn(new EnemyBullet(x, y, AimVector.dx(dir) * speed, AimVector.dy(dir) * speed, 0, 8, 25, null));
    }


    /** Dispara una bala enemiga desde (x, y) hacia el centro del jugador (ángulo libre: helicóptero). */
    protected void shootAt(GameWorld world, Entity player, double x, double y, double speed) {
        double dx = player.getCenterX() - x, dy = player.getCenterY() - y, d = Math.max(1, Math.hypot(dx, dy));
        world.spawn(new EnemyBullet(x, y, dx / d * speed, dy / d * speed, 0, 8, 25, null));
    }


    /** Distancia horizontal al jugador. */
    protected double distX(Entity player) {
        return Math.abs(player.getCenterX() - getCenterX());
    }


    // ---------------- dibujo de los policías (sin sprites: rectángulos estilo pixel) ----------------


    /** Rectángulo en coordenadas locales (0..width), volteado si mira a la izquierda. */
    protected void part(GraphicsContext gc, Camera cam, double lx, double ly, double w, double h, Color c) {
        double wx = facing > 0 ? x + lx : x + width - lx - w;
        gc.setFill(hitFlash > 0 ? Color.WHITE : c);
        gc.fillRect(Math.round(cam.toScreenX(wx)), Math.round(cam.toScreenY(y + ly)), w, h);
    }


    /** Policía base: botas, pantalón, camisa azul, cara, gorra con placa dorada. */
    protected void drawPolice(GraphicsContext gc, Camera cam) {
        part(gc, cam, 6, 44, 8, 8, Color.web("#111111"));     // botas
        part(gc, cam, 17, 44, 8, 8, Color.web("#111111"));
        part(gc, cam, 7, 30, 7, 15, Color.web("#1d2a4a"));    // pantalón
        part(gc, cam, 17, 30, 7, 15, Color.web("#1d2a4a"));
        part(gc, cam, 5, 14, 21, 17, Color.web("#2f5fb3"));   // camisa
        part(gc, cam, 5, 27, 21, 3, Color.web("#111111"));    // cinturón
        part(gc, cam, 9, 4, 13, 11, Color.web("#d8a07a"));    // cara
        part(gc, cam, 18, 8, 3, 3, Color.web("#111111"));     // ojo (mira al frente)
        part(gc, cam, 7, 0, 17, 5, Color.web("#14213d"));     // gorra
        part(gc, cam, 20, 4, 7, 2, Color.web("#14213d"));     // visera
        part(gc, cam, 13, 1, 4, 3, Color.web("#ffd24a"));     // placa
    }
}


