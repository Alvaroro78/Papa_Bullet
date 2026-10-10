package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


/**
 * Bala de cualquier enemigo: recta o en curva (gravity), le quita vida al jugador.
 * Es projectile: atraviesa las plataformas '=' y solo se rompe contra '#' y '-'.
 */
public class EnemyBullet extends Entity {
    private static final double LIFETIME = 4;
    private final int damage;
    private final String sprite; // null = bolita roja
    private double age;


    public EnemyBullet(double cx, double cy, double vx, double vy, double gravity, double size, int damage, String sprite) {
        width = height = size;
        x = cx - size / 2;
        y = cy - size / 2;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.sprite = sprite;
        team = Team.ENEMY;
        collidesWithTiles = true;
        projectile = true;
        gravityScale = gravity;
    }


    @Override
    public void update(double dt, GameWorld world) {
        age += dt;
        if (hitWall || hitCeiling || onGround || age > LIFETIME) kill();
    }


    @Override
    public void onCollide(Entity other, GameWorld world) {
        if (other == world.getPlayer()) {
            other.takeDamage(damage, this);
            kill();
        }
    }


    @Override
    public void render(GraphicsContext gc, Camera cam) {
        double sx = cam.toScreenX(x), sy = cam.toScreenY(y);
        Image img = sprite == null ? null : Sprites.get(sprite, width * 1.3, width * 1.3);
        if (img != null) {
            gc.drawImage(img, sx + (width - img.getWidth()) / 2, sy + (height - img.getHeight()) / 2);
            return;
        }
        gc.setFill(Color.web("#3a0000"));
        gc.fillOval(sx - 1, sy - 1, width + 2, height + 2);
        gc.setFill(Color.web("#ff4a3a"));
        gc.fillOval(sx, sy, width, height);
        gc.setFill(Color.web("#ffd0c8"));
        gc.fillOval(sx + width * 0.25, sy + height * 0.2, width * 0.35, height * 0.35);
    }
}


