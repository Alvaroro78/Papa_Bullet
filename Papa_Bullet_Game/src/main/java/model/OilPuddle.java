package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


/**
 * Aceite caliente: primero es una gota que cae; al tocar el piso se vuelve un charco que dura
 * unos segundos y quema a cada enemigo encima (TICK_DAMAGE cada TICK segundos).
 */
public class OilPuddle extends Entity {
    private static final double PUDDLE_W = 90, PUDDLE_H = 12, DURATION = 5, TICK = 0.3, DROP_LIFE = 3;
    private static final int TICK_DAMAGE = 10;
    private boolean spread;
    private double age, tickTimer;


    public OilPuddle(double cx, double cy, double vx, double vy) {
        width = height = 12;
        x = cx - 6;
        y = cy - 6;
        this.vx = vx;
        this.vy = vy;
        team = Team.NEUTRAL;      // no es bala: no choca con nada, solo quema lo que tiene encima
        collidesWithTiles = true;
        gravityScale = 1;
    }


    @Override
    public void update(double dt, GameWorld world) {
        age += dt;
        if (!spread) {
            if (onGround) spreadOnFloor();
            else if (hitWall) vx = 0;
            else if (age > DROP_LIFE) kill();
            return;
        }
        tickTimer -= dt;
        if (tickTimer <= 0) {
            tickTimer = TICK;
            for (Entity e : world.getEnemies()) {
                if (!e.isProjectile() && e.getBounds().intersects(getBounds())) e.takeDamage(TICK_DAMAGE, this);
            }
        }
        if (age >= DURATION) kill();
    }


    /** Si la gota le pega a un enemigo en el aire, cae derecho a sus pies (el charco queda debajo de él). */
    @Override
    public void onCollide(Entity other, GameWorld world) {
        if (!spread && other.getTeam() == Team.ENEMY && !other.isProjectile()) vx = 0;
    }


    /** Se aplana sobre el piso, centrado donde cayó la gota. */
    private void spreadOnFloor() {
        double cx = getCenterX(), bottom = y + height;
        spread = true;
        age = 0;
        width = PUDDLE_W;
        height = PUDDLE_H;
        x = cx - PUDDLE_W / 2;
        y = bottom - PUDDLE_H;
        vx = 0;
    }


    @Override
    public void render(GraphicsContext gc, Camera cam) {
        double sx = cam.toScreenX(x), sy = cam.toScreenY(y);
        if (!spread) {
            gc.setFill(Color.web("#f2b632"));
            gc.fillOval(sx, sy, width, height);
            return;
        }
        gc.setGlobalAlpha(age > DURATION - 1 ? DURATION - age : 1); // se desvanece el último segundo
        Image img = Sprites.get("CharcoAceite.png", PUDDLE_W, PUDDLE_H * 3);
        if (img != null) gc.drawImage(img, sx + (width - img.getWidth()) / 2, sy + height - img.getHeight() + 4);
        else {
            gc.setFill(Color.web("#f2b632"));
            gc.fillOval(sx, sy, width, height);
        }
        gc.setGlobalAlpha(1);
    }
}


