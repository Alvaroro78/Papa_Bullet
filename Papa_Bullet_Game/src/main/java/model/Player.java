package model;

import control.InputHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * El científico (P2).
 * A/D moverse · L saltar · S + L bajar de plataforma · W/S apuntar · K disparar
 * Q cambiar de arma · O (mantener 3 s bajo una zona J) jetpack · Ñ recargar en un King Papa
 *
 * P1 crea un Player nuevo cada vez que empieza o se reinicia un nivel: así los
 * King Papa usados se reinician solos "por iteración de nivel".
 */
public class Player extends Entity {

    // ---- Ajustes: cámbienlos probando en PlayerSandbox ----
    public static final double WIDTH = 36, HEIGHT = 60, CROUCH_HEIGHT = 36;
    public static final double SPEED = 200, JUMP_SPEED = 700, GRAVITY = 1800, MAX_FALL = 900;
    public static final double JETPACK_HOLD = 3.0, JETPACK_SPEED = 350;
    public static final double DROP_TIME = 0.25, INVULNERABLE_TIME = 1.0;
    public static final int MAX_HP = 3, RECHARGE = 150;

    private static final KeyCode KEY_LEFT = KeyCode.A, KEY_RIGHT = KeyCode.D, KEY_UP = KeyCode.W,
            KEY_DOWN = KeyCode.S, KEY_JUMP = KeyCode.L, KEY_SHOOT = KeyCode.K,
            KEY_SWITCH = KeyCode.Q, KEY_JETPACK = KeyCode.O;
    private static final String KEY_RECHARGE = "ñ"; // JavaFX no tiene KeyCode para la ñ

    private final InputHandler input;
    private final Image[] frames;
    private final Set<KeyCode> held = new HashSet<>();

    private int facing = 1; // 1 derecha, -1 izquierda
    private AimDirection aim = AimDirection.RIGHT;
    private boolean onGround, crouching;
    private double dropTimer, invulnerable, walkTime;

    private final List<Weapon> weapons = new ArrayList<>();
    private int weaponIndex;

    private Rectangle2D jetZone;   // zona encima: se dibuja la mira roja
    private Rectangle2D flyTarget; // != null mientras el jetpack lo lleva
    private double jetCharge;

    private int reserve; // lo que da el King Papa (+150): qué es exactamente sigue pendiente en el doc
    private final Set<Rectangle2D> usedKingPapas = new HashSet<>();

    /** skin = prefijo de los sprites, ej. "PJ" carga PJ_1.png, PJ_2.png... de /assets/sprites/ */
    public Player(double x, double y, InputHandler input, String skin) {
        this.x = x;
        this.y = y;
        this.width = WIDTH;
        this.height = HEIGHT;
        this.hp = MAX_HP;
        this.input = input;
        this.frames = loadFrames(skin);
    }

    // =====================================================================
    // Lógica de cada frame
    // =====================================================================

    @Override
    public void update(double dt, GameWorld world) {
        dropTimer = Math.max(0, dropTimer - dt);
        invulnerable = Math.max(0, invulnerable - dt);
        boolean jump = pressedNow(KEY_JUMP), shoot = pressedNow(KEY_SHOOT), swap = pressedNow(KEY_SWITCH);
        boolean recharge = input.consumeTyped(KEY_RECHARGE);

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
            } else if (onPlatformOnly(world)) {
                dropTimer = DROP_TIME;
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
                return;
            }
        } else {
            jetCharge = 0;
        }

        // 5. Recarga: una vez por King Papa
        if (recharge) {
            for (Rectangle2D k : world.getKingPapas()) {
                if (k.intersects(getBounds()) && usedKingPapas.add(k)) {
                    reserve += RECHARGE;
                    break;
                }
            }
        }

        // 6. Física
        vy = Math.min(vy + GRAVITY * dt, MAX_FALL);
        moveX(vx * dt, world);
        moveY(vy * dt, world);
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
    // Movimiento y colisiones con las baldosas
    // ponytail: la colisión vive aquí; si P4 la necesita para los enemigos, P1 la sube a Entity.
    // =====================================================================

    private void moveX(double dx, GameWorld world) {
        if (dx == 0) return;
        x += dx;
        int T = GameWorld.TILE;
        int col = tile(dx > 0 ? x + width - 0.001 : x);
        for (int row = tile(y); row <= tile(y + height - 0.001); row++) {
            if (world.isSolid(col, row)) {
                x = dx > 0 ? col * T - width : (col + 1) * T;
                return;
            }
        }
    }

    private void moveY(double dy, GameWorld world) {
        double oldBottom = y + height;
        y += dy;
        onGround = false;
        int T = GameWorld.TILE;
        if (dy > 0) { // cayendo: piso sólido o plataforma (solo si venía desde arriba)
            int row = tile(y + height - 0.001);
            for (int col = tile(x); col <= tile(x + width - 0.001); col++) {
                boolean platform = world.isPlatform(col, row) && dropTimer == 0 && oldBottom <= row * T;
                if (world.isSolid(col, row) || platform) {
                    y = row * T - height;
                    vy = 0;
                    onGround = true;
                    return;
                }
            }
        } else if (dy < 0) { // subiendo: las plataformas se atraviesan, el techo sólido no
            int row = tile(y);
            for (int col = tile(x); col <= tile(x + width - 0.001); col++) {
                if (world.isSolid(col, row)) {
                    y = (row + 1) * T;
                    vy = 0;
                    return;
                }
            }
        }
    }

    /** true si está parado solo sobre plataformas '=' (de un piso sólido no se puede bajar). */
    private boolean onPlatformOnly(GameWorld world) {
        int row = tile(y + height + 1);
        boolean platform = false;
        for (int col = tile(x); col <= tile(x + width - 0.001); col++) {
            if (world.isSolid(col, row)) return false;
            platform |= world.isPlatform(col, row);
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
        return (int) Math.floor(px / GameWorld.TILE);
    }

    // =====================================================================
    // Jetpack
    // =====================================================================

    /** La zona J más cercana justo encima de la cabeza, o null. */
    private Rectangle2D zoneAbove(GameWorld world) {
        double cx = x + width / 2;
        Rectangle2D best = null;
        for (Rectangle2D z : world.getJetpackZones()) {
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

    /** P3 lo llama al desbloquear un arma. La primera que se agrega es la de por defecto. */
    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public Weapon getCurrentWeapon() {
        return weapons.isEmpty() ? null : weapons.get(weaponIndex);
    }

    private double muzzleX() { return x + width / 2 + AimVector.dx(aim) * width * 0.7; }
    private double muzzleY() { return y + height * 0.4 + AimVector.dy(aim) * height * 0.5; }

    public AimDirection getAim() { return aim; }
    public int getReserve() { return reserve; }
    public boolean isFlying() { return flyTarget != null; }

    /** true solo en el frame en que la tecla se oprime (para no disparar 60 veces por segundo). */
    private boolean pressedNow(KeyCode key) {
        boolean down = input.isPressed(key);
        boolean edge = down && !held.contains(key);
        if (down) held.add(key);
        else held.remove(key);
        return edge;
    }

    // =====================================================================
    // Dibujo
    // =====================================================================

    @Override
    public void render(GraphicsContext gc) {
        boolean blinkOff = invulnerable > 0 && (int) (invulnerable * 10) % 2 == 0;
        if (!blinkOff) drawBody(gc);
        if (jetZone != null) drawReticle(gc, jetZone);
    }

    private void drawBody(GraphicsContext gc) {
        if (frames.length == 0) { // skin sin sprites: un rectángulo para poder seguir probando
            gc.setFill(Color.ORANGE);
            gc.fillRect(x, y, width, height);
            return;
        }
        Image img = frames[onGround ? (int) (walkTime * 8) % frames.length : 0];
        // Los PNG traen borde transparente, por eso se dibujan más grandes que la caja de colisión.
        // ponytail: agachado se dibuja el mismo sprite aplastado; cambiar cuando haya sprite de agachado.
        double dh = height * 1.2, dw = dh * img.getWidth() / img.getHeight();
        double dx = x + width / 2 - dw / 2, dy = y + height - dh * 0.92;
        if (facing > 0) gc.drawImage(img, dx, dy, dw, dh);
        else gc.drawImage(img, dx + dw, dy, -dw, dh); // ancho negativo = espejo
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

    private static Image[] loadFrames(String skin) {
        List<Image> list = new ArrayList<>();
        for (int i = 1; ; i++) {
            try (InputStream in = Player.class.getResourceAsStream("/assets/sprites/" + skin + "_" + i + ".png")) {
                if (in == null) break;
                list.add(new Image(in));
            } catch (IOException e) {
                break;
            }
        }
        return list.toArray(new Image[0]);
    }
}