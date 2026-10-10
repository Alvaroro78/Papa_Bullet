package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * 'B' — el jefe de cada nivel. Todos hacen lo mismo con distintos números:
 * van y vienen en su zona, atacan cada cierto tiempo y con menos de la mitad de vida se enojan
 * (más rápidos y atacan más seguido). Al morir avisan world.bossDefeated() y el nivel termina.
 *
 *   nivel 0  robot (sin sprite: se dibuja)        balas rectas en abanico
 *   nivel 1  ingeniero en máquina de demolición    bola de demolición en parábola (BolaJefe1)
 *   nivel 2  jefe fiestero                          abanico ancho de balas
 *   nivel 3  monstruo de piedra                     rocas en parábola
 */
public class Boss extends Enemy {

    /** Los números de cada jefe. */
    private record Spec(String name, String sprite, double w, double h, int hp, int shots, double spreadDeg,
                        double speed, double gravity, double size, int damage, String bulletSprite) {}

    private static final Spec[] SPECS = {
            new Spec("ROBOT", null, 90, 120, 600, 3, 30, 300, 0, 10, 25, null),
            new Spec("EL INGENIERO", "Jefe1", 150, 150, 800, 1, 0, 420, 1, 40, 45, "BolaJefe1_1.png"),
            new Spec("EL FIESTERO", "Jefe2", 90, 150, 900, 5, 70, 280, 0, 12, 25, null),
            new Spec("MONSTRUO DE PIEDRA", "Jefe3", 150, 150, 1200, 2, 20, 430, 1, 30, 40, null),
    };
    private static final double PATROL = 120, ATTACK_RATE = 1.8;

    private final Spec cfg;
    private final Image[] frames;
    private double homeX = Double.NaN, time;
    private boolean seen;

    public Boss(int levelIndex) {
        this(SPECS[Math.floorMod(levelIndex, SPECS.length)]);
    }

    private Boss(Spec cfg) {
        super(cfg.w(), cfg.h(), cfg.hp(), 50, false);
        this.cfg = cfg;
        this.frames = loadFrames(cfg.sprite(), cfg.w() * 1.2, cfg.h() * 1.1);
        cooldown = 1.2;
    }

    boolean isAngry() { return hp < maxHp / 2; }

    @Override
    protected void act(double dt, GameWorld world, Entity player) {
        if (Double.isNaN(homeX)) homeX = x;
        seen = true;
        time += dt;
        double speed = isAngry() ? 110 : 60;
        if (x < homeX - PATROL) vx = speed;           // va y viene entre homeX ± PATROL
        else if (x > homeX + PATROL) vx = -speed;
        else if (vx == 0) vx = -speed;
        if (cooldown == 0) {
            attack(world, player);
            cooldown = isAngry() ? ATTACK_RATE * 0.6 : ATTACK_RATE;
        }
    }

    /** Dispara cfg.shots balas en abanico hacia el jugador (con gravedad, en parábola por encima). */
    private void attack(GameWorld world, Entity player) {
        double sx = getCenterX() + facing * width * 0.4, sy = y + height * 0.35;
        double dx = player.getCenterX() - sx, dy = player.getCenterY() - sy;
        double base = cfg.gravity() > 0 ? Math.atan2(-1, Math.signum(dx)) : Math.atan2(dy, dx); // las parábolas salen a 45°
        double lift = cfg.gravity() > 0 ? Math.sqrt(Math.abs(dx) * Config.GRAVITY) / cfg.speed() : 1; // ajusta para caer cerca
        for (int i = 0; i < cfg.shots(); i++) {
            double a = cfg.shots() == 1 ? base : base + Math.toRadians(-cfg.spreadDeg() / 2 + cfg.spreadDeg() * i / (cfg.shots() - 1));
            double v = cfg.gravity() > 0 ? Math.min(cfg.speed(), cfg.speed() * lift) : cfg.speed();
            world.spawn(new EnemyBullet(sx, sy, Math.cos(a) * v, Math.sin(a) * v, cfg.gravity(), cfg.size(), cfg.damage(), cfg.bulletSprite()));
        }
    }

    @Override
    public void onDeath(GameWorld world) {
        world.spawn(new Explosion(getCenterX(), getCenterY(), Math.max(width, height), 0));
        world.bossDefeated();
    }

    // ---------------- dibujo ----------------

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        Image img = frames.length == 0 ? null : frames[(int) (time * 4) % frames.length];
        if (img == null) drawRobot(gc, cam);
        else {
            double dw = img.getWidth(), dh = img.getHeight();
            double dx = cam.toScreenX(getCenterX() - dw / 2), dy = cam.toScreenY(y + height - dh);
            if (hitFlash > 0) gc.setGlobalAlpha(0.55);
            if (facing > 0) gc.drawImage(img, dx, dy, dw, dh);
            else gc.drawImage(img, dx + dw, dy, -dw, dh);
            gc.setGlobalAlpha(1);
        }
        if (seen) drawHealthBar(gc);
    }

    /** Barra grande abajo de la pantalla, como en Contra. Se dibuja en coordenadas de pantalla. */
    private void drawHealthBar(GraphicsContext gc) {
        double w = 360, bx = (Config.WIDTH - w) / 2, by = Config.HEIGHT - 34;
        gc.setFill(Color.BLACK);
        gc.fillRect(bx - 3, by - 3, w + 6, 18);
        gc.setFill(Color.web("#3a0d0d"));
        gc.fillRect(bx, by, w, 12);
        gc.setFill(isAngry() ? Color.web("#ff5a2a") : Color.web("#e8433a"));
        gc.fillRect(bx, by, w * Math.max(0, hp) / maxHp, 12);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.BLACK);
        gc.fillText(cfg.name(), Config.WIDTH / 2.0 + 2, by - 8);
        gc.setFill(Color.web("#ffe14a"));
        gc.fillText(cfg.name(), Config.WIDTH / 2.0, by - 10);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    /** El robot del nivel 0 (no tiene sprite). */
    private void drawRobot(GraphicsContext gc, Camera cam) {
        boolean blink = (int) (time * 3) % 2 == 0;
        part(gc, cam, 20, 100, 18, 20, Color.web("#3a3f4a"));  // piernas
        part(gc, cam, 52, 100, 18, 20, Color.web("#3a3f4a"));
        part(gc, cam, 10, 40, 70, 62, Color.web("#7d8796"));   // cuerpo
        part(gc, cam, 10, 40, 70, 6, Color.web("#a9b3c2"));
        part(gc, cam, 30, 58, 30, 22, Color.web("#2a2f38"));   // pantalla del pecho
        part(gc, cam, 34, 62, 22, 14, isAngry() ? Color.web("#ff5a2a") : Color.web("#3ad06a"));
        part(gc, cam, 0, 48, 12, 40, Color.web("#5c6573"));    // brazos
        part(gc, cam, 78, 52, 14, 10, Color.web("#5c6573"));
        part(gc, cam, 86, 50, 6, 14, Color.web("#2a2f38"));    // cañón
        part(gc, cam, 22, 6, 46, 34, Color.web("#7d8796"));    // cabeza
        part(gc, cam, 30, 16, 30, 10, Color.web("#2a2f38"));   // visor
        part(gc, cam, blink ? 48 : 34, 18, 8, 6, Color.web("#ff3a3a"));
        part(gc, cam, 43, 0, 4, 8, Color.web("#5c6573"));      // antena
    }

    private static Image[] loadFrames(String prefix, double w, double h) {
        List<Image> list = new ArrayList<>();
        if (prefix == null) return new Image[0];
        for (int i = 1; Boss.class.getResource("/assets/sprites/" + prefix + "_" + i + ".png") != null; i++) {
            Image img = Sprites.get(prefix + "_" + i + ".png", w, h);
            if (img != null) list.add(img);
        }
        return list.toArray(new Image[0]);
    }
}