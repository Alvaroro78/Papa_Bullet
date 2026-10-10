package model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El científico (P2).
 * A/D moverse · L saltar · S + L bajar de plataforma · W/S apuntar · K disparar
 * Q cambiar de arma · O (mantener 3 s bajo una zona J) jetpack · Ñ recargar en un King Papa
 *
 * P1 crea un Player nuevo cada vez que empieza o se reinicia un nivel: así los
 * King Papa usados se reinician solos "por iteración de nivel".
 */
public class Player extends Entity {

    // ---- Ajustes: cámbienlos probando en el juego (F1 muestra las cajas de colisión) ----
    public static final double WIDTH = 36, HEIGHT = 60, CROUCH_HEIGHT = 36;
    public static final double SPEED = 200, JUMP_SPEED = 700;
    public static final double JETPACK_HOLD = 3.0, JETPACK_SPEED = 350;
    public static final double INVULNERABLE_TIME = 1.0;
    /** Vida. Los enemigos de P4 quitan del orden de 25-50 por golpe; el King Papa devuelve 150. */
    public static final int MAX_HP = 300, RECHARGE = 150;
    /** La skin elegida en la pantalla de Skins: prefijo de los sprites + cambio de tono (-1..1, 0 = original). */
    public static String selectedSkin = "PJ";
    public static double selectedHue = 0;

    private static final KeyCode KEY_LEFT = KeyCode.A, KEY_RIGHT = KeyCode.D, KEY_UP = KeyCode.W,
            KEY_DOWN = KeyCode.S, KEY_JUMP = KeyCode.L, KEY_SHOOT = KeyCode.K,
            KEY_SWITCH = KeyCode.Q, KEY_JETPACK = KeyCode.O;
    private static final String KEY_RECHARGE = "ñ"; // JavaFX no tiene KeyCode para la ñ

    private final String skin;
    private final double hue;
    private final Image[] frames;

    private int facing = 1; // 1 derecha, -1 izquierda
    private AimDirection aim = AimDirection.RIGHT;
    private boolean crouching;
    private double invulnerable, walkTime;

    private final List<Weapon> weapons = new ArrayList<>();
    private int weaponIndex;

    private Rectangle2D jetZone;   // zona encima: se dibuja la mira roja
    private Rectangle2D flyTarget; // != null mientras el jetpack lo lleva
    private double jetCharge;

    /** skin = prefijo de los sprites, ej. "PJ" carga PJ_1.png, PJ_2.png... de /assets/sprites/ */
    public Player() {
        this(selectedSkin);
    }

    public Player(String skin) {
        this.width = WIDTH;
        this.height = HEIGHT;
        this.hp = MAX_HP;
        this.team = Team.PLAYER;
        this.collidesWithTiles = true;
        this.gravityScale = 1;
        this.skin = skin;
        this.hue = selectedHue;
        this.frames = loadFrames(skin);
        weapons.add(new ThrowerPapa()); // el arma por defecto; las otras se recogen en el nivel
    }

    // =====================================================================
    // Lógica de cada frame
    // =====================================================================

    @Override
    public void update(double dt, GameWorld world) {
        InputHandler input = world.getInput();
        invulnerable = Math.max(0, invulnerable - dt);
        boolean jump = input.wasPressed(KEY_JUMP), shoot = input.wasPressed(KEY_SHOOT), swap = input.wasPressed(KEY_SWITCH);
        boolean recharge = input.wasTyped(KEY_RECHARGE);

        if (flyTarget != null) {
            fly(dt);
            return;
        }

        // 1. Teclado -> apuntado, movimiento y agachado
        int side = (input.isPressed(KEY_RIGHT) ? 1 : 0) - (input.isPressed(KEY_LEFT) ? 1 : 0);
        boolean up = input.isPressed(KEY_UP), down = input.isPressed(KEY_DOWN);
        KeyCode sideKey = side > 0 ? KEY_RIGHT : KEY_LEFT;
        KeyCode vertKey = down ? KEY_DOWN : KEY_UP;
        boolean sideFirst = side != 0 && input.pressOrder(sideKey) < input.pressOrder(vertKey);

        Intent intent = decide(side, facing, up, down, sideFirst, onGround);
        aim = intent.aim();
        setCrouching(intent.crouch());
        if (!crouching && side != 0) facing = side;
        vx = intent.move() * SPEED;

        // 2. Saltar, o bajar de la plataforma si está agachado encima de una
        if (jump && onGround) {
            if (!crouching) {
                vy = -JUMP_SPEED;
            } else if (onPlatformOnly(world.getLevel())) {
                dropThroughPlatform();
                setCrouching(false);
            }
        }

        // 3. Armas
        if (swap && !weapons.isEmpty()) weaponIndex = (weaponIndex + 1) % weapons.size();
        if (shoot && !weapons.isEmpty()) weapons.get(weaponIndex).fire(muzzleX(), muzzleY(), aim, world);

        // 4. Jetpack: en el piso, bajo una zona J, mirando arriba y manteniendo O
        jetZone = (onGround && aim == AimDirection.UP) ? zoneAbove(world) : null;
        if (jetZone != null && input.isPressed(KEY_JETPACK)) {
            jetCharge += dt;
            if (jetCharge >= JETPACK_HOLD) {
                flyTarget = jetZone;
                jetZone = null;
                jetCharge = 0;
                vx = vy = 0;
                gravityScale = 0;
                collidesWithTiles = false;
                return;
            }
        } else {
            jetCharge = 0;
        }

        // 5. Recarga de vida: una vez por King Papa (si ya está lleno no lo gasta)
        if (recharge) {
            for (KingPapa k : world.getKingPapas()) {
                if (!k.isUsed() && hp < MAX_HP && k.getBounds().intersects(getBounds())) {
                    k.markUsed();
                    hp = Math.min(MAX_HP, hp + RECHARGE);
                    break;
                }
            }
        }

        // 6. La física (gravedad y baldosas) la aplica el motor con Collision después de update()
        walkTime = (onGround && vx != 0) ? walkTime + dt : 0;
    }

    /** Lo que el jugador quiere hacer este frame según las teclas. */
    record Intent(AimDirection aim, int move, boolean crouch) {}

    /**
     * Reglas de apuntado del documento del juego. Es estática y sin JavaFX para poder probarla sola.
     * @param side      -1 izquierda, 0 ninguno, 1 derecha
     * @param sideFirst true si el lado se oprimió antes que W/S
     */
    static Intent decide(int side, int facing, boolean up, boolean down, boolean sideFirst, boolean onGround) {
        if (!onGround) { // en el aire apunta a cualquier lado (abajo solo aquí) y se mueve libre
            int sy = up ? -1 : down ? 1 : 0;
            int sx = sy == 0 ? (side != 0 ? side : facing) : side;
            return new Intent(AimVector.of(sx, sy), side, false);
        }
        if (down && !sideFirst) return new Intent(AimVector.of(facing, 0), 0, true);   // S primero: se agacha y no gira
        if (down) return new Intent(AimVector.of(side, 1), side, false);               // lado primero + S: diagonal abajo caminando
        if (up && side == 0) return new Intent(AimDirection.UP, 0, false);                // solo W: arriba, quieto
        if (up) return new Intent(AimVector.of(side, -1), sideFirst ? side : 0, false); // W primero: diagonal quieto
        return new Intent(AimVector.of(side != 0 ? side : facing, 0), side, false);    // al frente
    }

    // =====================================================================
    // Agacharse y bajar de plataformas (el resto de la colisión está en Collision)
    // =====================================================================

    /** true si está parado solo sobre plataformas '=' (de un piso sólido no se puede bajar). */
    private boolean onPlatformOnly(Level level) {
        int row = tile(y + height + 1);
        boolean platform = false;
        for (int col = tile(x); col <= tile(x + width - 0.001); col++) {
            if (level.isSolid(col, row)) return false;
            platform |= level.isPlatform(col, row);
        }
        return platform;
    }

    /** Agacharse encoge la caja hacia abajo: los pies no se mueven. */
    private void setCrouching(boolean value) {
        if (value == crouching) return;
        crouching = value;
        double diff = HEIGHT - CROUCH_HEIGHT;
        y += value ? diff : -diff; // ponytail: al pararse no revisa techo; basta con no poner techos de 1 baldosa
        height = value ? CROUCH_HEIGHT : HEIGHT;
    }

    private static int tile(double px) {
        return (int) Math.floor(px / Config.TILE);
    }

    // =====================================================================
    // Jetpack
    // =====================================================================

    /** La zona J más cercana justo encima de la cabeza, o null. */
    private Rectangle2D zoneAbove(GameWorld world) {
        double cx = x + width / 2;
        Rectangle2D best = null;
        for (Rectangle2D z : world.getLevel().getJetpackZones()) {
            boolean above = cx >= z.getMinX() && cx <= z.getMaxX() && z.getMaxY() <= y;
            if (above && (best == null || z.getMaxY() > best.getMaxY())) best = z;
        }
        return best;
    }

    /** Vuela en línea recta hasta dejar los pies en el borde de abajo de la zona. Ignora baldosas. */
    private void fly(double dt) {
        double tx = flyTarget.getMinX() + flyTarget.getWidth() / 2 - width / 2;
        double ty = flyTarget.getMaxY() - height;
        double dx = tx - x, dy = ty - y, dist = Math.hypot(dx, dy), step = JETPACK_SPEED * dt;
        if (dist <= step) {
            x = tx;
            y = ty;
            vx = vy = 0;
            flyTarget = null;
            gravityScale = 1;
            collidesWithTiles = true;
        } else {
            x += dx / dist * step;
            y += dy / dist * step;
        }
    }

    // =====================================================================
    // Vida y armas
    // =====================================================================

    @Override
    public void takeDamage(int dmg) {
        if (invulnerable > 0 || flyTarget != null) return;
        super.takeDamage(dmg);
        invulnerable = INVULNERABLE_TIME;
    }

    /** Al recoger un arma: si es nueva se agrega, y en ambos casos queda seleccionada. */
    public void addWeapon(Weapon weapon) {
        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getClass() == weapon.getClass()) {
                weaponIndex = i;
                return;
            }
        }
        weapons.add(weapon);
        weaponIndex = weapons.size() - 1;
    }

    /** Todas las armas recogidas, en orden (para el HUD). */
    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public Weapon getCurrentWeapon() {
        return weapons.isEmpty() ? null : weapons.get(weaponIndex);
    }

    private double muzzleX() { return x + width / 2 + AimVector.dx(aim) * width * 0.7; }
    private double muzzleY() { return y + height * 0.4 + AimVector.dy(aim) * height * 0.5; }

    public AimDirection getAim() { return aim; }
    public boolean isFlying() { return flyTarget != null; }

    // =====================================================================
    // Dibujo
    // =====================================================================

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        gc.save();
        gc.translate(cam.toScreenX(0), cam.toScreenY(0));
        boolean blinkOff = invulnerable > 0 && (int) (invulnerable * 10) % 2 == 0;
        if(!blinkOff) drawBody(gc);
        if(jetZone != null) drawReticle(gc, jetZone);
        gc.restore();
    }

    private void drawBody(GraphicsContext gc) {
        Image img = currentImage();
        if (img == null) { // skin sin sprites: un rectángulo para poder seguir probando
            gc.setFill(Color.ORANGE);
            gc.fillRect(x, y, width, height);
            return;
        }
        // Sprites ya entrega la imagen al tamaño de dibujo; los PNG traen borde transparente,
        // por eso se ven más grandes que la caja de colisión. Los pies quedan en el borde de abajo.
        double dw = img.getWidth(), dh = img.getHeight();
        double dx = x + width / 2 - dw / 2, dy = y + height - dh * 0.92;
        if (hue != 0) gc.setEffect(new ColorAdjust(hue, 0, 0, 0)); // el tono de la skin
        if (facing > 0) gc.drawImage(img, dx, dy, dw, dh);
        else gc.drawImage(img, dx + dw, dy, -dw, dh); // ancho negativo = espejo
        gc.setEffect(null);
    }

    /** El dibujo de la pose actual; si esa skin no la tiene, los cuadros de caminar. */
    private Image currentImage() {
        String pose = pose();
        if (pose != null) {
            double h = pose.equals("muerto") ? HEIGHT * 0.8 : pose.equals("agachado") ? HEIGHT * 0.9 : HEIGHT * 1.2;
            Image img = Sprites.get(skin + "_" + pose + ".png", HEIGHT * 2.4, h);
            if (img != null) return img;
        }
        return frames.length == 0 ? null : frames[onGround ? (int) (walkTime * 8) % frames.length : 0];
    }

    /** Qué sprite toca (skin_pose.png). null = caminar / quieto con los cuadros skin_1..4. */
    String pose() {
        if (!alive) return "muerto";
        if (crouching) return "agachado";
        if (aim == AimDirection.UP) return "arriba";
        if (aim == AimDirection.UP_LEFT || aim == AimDirection.UP_RIGHT) return "diagonal";
        if (!onGround || flyTarget != null) return "salto";
        return null;
    }

    private void drawReticle(GraphicsContext gc, Rectangle2D z) {
        double cx = z.getMinX() + z.getWidth() / 2, cy = z.getMinY() + z.getHeight() / 2, r = 14;
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeOval(cx - r, cy - r, 2 * r, 2 * r);
        gc.strokeLine(cx - r - 6, cy, cx + r + 6, cy);
        gc.strokeLine(cx, cy - r - 6, cx, cy + r + 6);
        if (jetCharge > 0) { // anillo que se llena en 3 s
            double R = r + 8;
            gc.strokeArc(cx - R, cy - R, 2 * R, 2 * R, 90, -360 * jetCharge / JETPACK_HOLD, ArcType.OPEN);
        }
    }

    /** Carga skin_1.png, skin_2.png... hasta que falte uno. Sprites los escala y los guarda en caché. */
    private static Image[] loadFrames(String skin) {
        List<Image> list = new ArrayList<>();
        for (int i = 1; Player.class.getResource("/assets/sprites/" + skin + "_" + i + ".png") != null; i++) {
            Image img = Sprites.get(skin + "_" + i + ".png", HEIGHT * 2.4, HEIGHT * 1.2); // alto de dibujo: height * 1.2
            if (img != null) list.add(img);
        }
        return list.toArray(new Image[0]);
    }
}