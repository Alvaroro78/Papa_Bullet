package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


/**
 * Las papas a la francesa. Dos modos:
 *  - caja (spike = false): va recta y a los SPLIT_TIME segundos se abre en 5 espinas en abanico.
 *  - espina (spike = true): papita suelta que pincha al primer enemigo que toca.
 */
public class FrenchFry extends Entity {
    private static final double SPLIT_TIME = 0.3, SPIKE_LIFE = 0.45, SPIKE_SPEED = 460;
    private static final int SPIKE_DAMAGE = 20, SPREAD = 5;
    private final boolean spike;
    private double age;


    public FrenchFry(double cx, double cy, double vx, double vy, boolean spike) {
        this.spike = spike;
        width = height = spike ? 8 : 20;
        x = cx - width / 2;
        y = cy - height / 2;
        this.vx = vx;
        this.vy = vy;
        team = Team.PLAYER;
        collidesWithTiles = true;
        projectile = true;
    }


    @Override
    public void update(double dt, GameWorld world) {
        age += dt;
        if (hitWall || hitCeiling || onGround) {
            kill();
        } else if (!spike && age >= SPLIT_TIME) {
            split(world);
        } else if (spike && age >= SPIKE_LIFE) {
            kill();
        }
    }


    /** Se abre en abanico de ±40° alrededor de la dirección en que iba. */
    private void split(GameWorld world) {
        double base = Math.atan2(vy, vx);
        for (int i = 0; i < SPREAD; i++) {
            double a = base + Math.toRadians(-40 + 80.0 * i / (SPREAD - 1));
            world.spawn(new FrenchFry(getCenterX(), getCenterY(), Math.cos(a) * SPIKE_SPEED, Math.sin(a) * SPIKE_SPEED, true));
        }
        kill();
    }


    @Override
    public void onCollide(Entity other, GameWorld world) {
        if (other.getTeam() != Team.ENEMY || other.isProjectile()) return; // ignora las balas enemigas
        if (spike) {
            other.takeDamage(SPIKE_DAMAGE, this);
            kill();
        } else {
            split(world); // la caja choca antes de abrirse: se abre ahí mismo
        }
    }


    @Override
    public void render(GraphicsContext gc, Camera cam) {
        double sx = cam.toScreenX(getCenterX()), sy = cam.toScreenY(getCenterY());
        if (!spike) {
            Image img = Sprites.get("FrenchSpikes.png", 26, 26);
            if (img != null) {
                gc.drawImage(img, sx - img.getWidth() / 2, sy - img.getHeight() / 2);
                return;
            }
        }
        // espina: una papita amarilla con borde, apuntando hacia donde va
        gc.save();
        gc.translate(sx, sy);
        gc.rotate(Math.toDegrees(Math.atan2(vy, vx)));
        gc.setFill(Color.web("#7a4a10"));
        gc.fillRect(-9, -3, 18, 6);
        gc.setFill(Color.web("#ffd84a"));
        gc.fillRect(-8, -2, 16, 4);
        gc.restore();
    }
}


