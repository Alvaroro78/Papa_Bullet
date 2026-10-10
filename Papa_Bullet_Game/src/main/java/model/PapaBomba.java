package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


/** La papa explosiva: cae en parábola, atraviesa las plataformas '=' y explota al tocar '#', '-' o un enemigo. */
public class PapaBomba extends Entity {
    private static final double SIZE = 18, LIFETIME = 3;
    private double age, spin;


    public PapaBomba(double cx, double cy, double vx, double vy) {
        width = height = SIZE;
        x = cx - SIZE / 2;
        y = cy - SIZE / 2;
        this.vx = vx;
        this.vy = vy;
        team = Team.PLAYER;
        collidesWithTiles = true;
        projectile = true;
        gravityScale = 1;
    }


    @Override
    public void update(double dt, GameWorld world) {
        age += dt;
        spin += dt * 720 * Math.signum(vx == 0 ? 1 : vx);
        if (onGround || hitWall || hitCeiling) explode(world);
        else if (age > LIFETIME) kill();
    }


    @Override
    public void onCollide(Entity other, GameWorld world) {
        if (other.getTeam() == Team.ENEMY && !other.isProjectile()) explode(world); // las balas enemigas no la detonan
    }


    private void explode(GameWorld world) {
        world.spawn(new Explosion(getCenterX(), getCenterY(), 48, 40));
        kill();
    }


    @Override
    public void render(GraphicsContext gc, Camera cam) {
        Image img = Sprites.get("PapaBomba.png", SIZE * 1.6, SIZE * 1.6);
        double sx = cam.toScreenX(getCenterX()), sy = cam.toScreenY(getCenterY());
        gc.save();
        gc.translate(sx, sy);
        gc.rotate(spin);
        if (img != null) gc.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2);
        else {
            gc.setFill(Color.web("#c8803a"));
            gc.fillOval(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
        }
        gc.restore();
    }
}


