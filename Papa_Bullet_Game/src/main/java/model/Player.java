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
 * Q cambiar de arma · O (mantener 3 s mirando arriba) jetpack · Ñ recargar en un King Papa
 *
 * VIDA = TIEMPO: la vida es un contador en segundos que baja solo (1 s por segundo).
 * Los golpes le quitan segundos. Cada disparo CALIENTA la máquina (la mochila): con más calor
 * el contador baja más rápido y la mochila se pone roja y echa vapor. Si llega a 0, la máquina explota.
 *
 * P1 crea un Player nuevo cada vez que empieza o se reinicia un nivel: así los
 * King Papa usados se reinician solos "por iteración de nivel".
 */
public class Player extends Entity {


    // ---- Ajustes: cámbienlos probando en el juego (F1 muestra las cajas de colisión) ----
    /** Agachado mide 28: las balas rectas de un policía con pistola (que salen a 30-38 px del piso) le pasan por encima. */
    public static final double WIDTH = 36, HEIGHT = 60, CROUCH_HEIGHT = 28;
    public static final double SPEED = 200, JUMP_SPEED = 700;
    public static final double JETPACK_HOLD = 3.0, JETPACK_SPEED = 350;
    public static final int JETPACK_REACH = 8, JETPACK_BOOST = 4; // baldosas: hasta dónde busca piso arriba / cuánto sube si no hay
    public static final double INVULNERABLE_TIME = 1.0;
    /** Vida = segundos que le quedan. Los enemigos quitan 25-50 por golpe; el King Papa devuelve 150 y enfría la máquina. */
    public static final int MAX_HP = 300, RECHARGE = 150;
    /** Calor (0..1): cuánto enfría por segundo y cuánto más rápido baja el contador al máximo calor (1 + 3 = x4). */
    public static final double COOL_RATE = 0.12, MAX_EXTRA_DRAIN = 3;
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
    private double invulnerable, walkTime, fireCooldown;
    private double timeLeft = MAX_HP; // la vida "de verdad" (hp es este número redondeado hacia arriba)
    private double heat;              // 0 = fría, 1 = al rojo vivo
    private double clock;             // para animar el vapor


    private final List<Weapon> weapons = new ArrayList<>();
    private int weaponIndex;


    private Rectangle2D jetZone;   // dónde quedaría al volar (caja del jugador): ahí se dibuja la mira roja
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
        fireCooldown = Math.max(0, fireCooldown - dt);
        boolean jump = input.wasPressed(KEY_JUMP), shoot = input.isPressed(KEY_SHOOT), swap = input.wasPressed(KEY_SWITCH);
        boolean recharge = input.wasTyped(KEY_RECHARGE);


        // 0. El contador baja solo; más rápido mientras más caliente esté la máquina
        clock += dt;
        heat = Math.max(0, heat - COOL_RATE * dt);
        loseTime(dt * drainRate(), world);
        if (!alive) return;


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
        // si hay techo encima no se puede parar: sigue agachado (antes quedaba metido dentro del bloque)
        setCrouching(intent.crouch() || (crouching && !canStandUp(world.getLevel())));
        if (!intent.crouch() && side != 0) facing = side;
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
        if (shoot && fireCooldown == 0 && !weapons.isEmpty()) { // K sostenida dispara al ritmo del arma
            Weapon w = weapons.get(weaponIndex);
            w.fire(muzzleX(), muzzleY(), aim, world);
            fireCooldown = w.getCooldown();
            heat = Math.min(1, heat + w.getHeat());
        }


        // 4. Jetpack: en cualquier parte del piso, mirando arriba y manteniendo O
        jetZone = (onGround && aim == AimDirection.UP) ? landingAbove(world.getLevel()) : null;
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


        // 5. Recarga: +150 s y la máquina queda fría. Una vez por King Papa (si no hace falta, no lo gasta)
        if (recharge && (timeLeft < MAX_HP || heat > 0)) {
            for (KingPapa k : world.getKingPapas()) {
                if (!k.isUsed() && k.getBounds().intersects(getBounds())) {
                    k.markUsed();
                    timeLeft = Math.min(MAX_HP, timeLeft + RECHARGE);
                    hp = (int) Math.ceil(timeLeft);
                    heat = 0;
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


    /** ¿Hay espacio libre arriba para volver a medir HEIGHT? */
    private boolean canStandUp(Level level) {
        double top = y + height - HEIGHT;
        for (int row = tile(top); row <= tile(y - 0.001); row++) {
            for (int col = tile(x); col <= tile(x + width - 0.001); col++) {
                if (level.isSolid(col, row)) return false;
            }
        }
        return true;
    }


    /** Agacharse encoge la caja hacia abajo: los pies no se mueven. */
    private void setCrouching(boolean value) {
        if (value == crouching) return;
        crouching = value;
        double diff = HEIGHT - CROUCH_HEIGHT;
        y += value ? diff : -diff; // al pararse, update() ya revisó con canStandUp() que no haya techo
        height = value ? CROUCH_HEIGHT : HEIGHT;
    }


    private static int tile(double px) {
        return (int) Math.floor(px / Config.TILE);
    }


    // =====================================================================
    // Jetpack
    // =====================================================================


    /**
     * A dónde lo lleva el jetpack: el piso más cercano encima de su cabeza (plataforma '=' o bloque '#'
     * con espacio libre arriba para pararse), hasta JETPACK_REACH baldosas. Si no hay ninguno,
     * sube JETPACK_BOOST baldosas y después cae. Devuelve la caja que ocuparía el jugador al llegar.
     */
    Rectangle2D landingAbove(Level level) {
        int T = Config.TILE;
        int c0 = tile(x), c1 = tile(x + width - 0.001), head = tile(y);
        for (int r = head - 1; r >= head - JETPACK_REACH; r--) {
            if (standable(level, c0, c1, r)) return new Rectangle2D(x, r * T - height, width, height);
        }
        return new Rectangle2D(x, y - JETPACK_BOOST * T, width, height);
    }


    /** ¿En la fila r hay algo que pisar y encima 2 filas libres (el jugador mide 1.5 baldosas)? */
    private static boolean standable(Level level, int c0, int c1, int r) {
        boolean floor = false;
        for (int c = c0; c <= c1; c++) {
            if (level.isSolid(c, r - 1) || level.isSolid(c, r - 2)) return false;
            floor |= level.isSolid(c, r) || level.isPlatform(c, r);
        }
        return floor;
    }


    /** Vuela en línea recta hasta dejar los pies en el borde de abajo del destino. Ignora baldosas. */
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
        if (invulnerable > 0 || flyTarget != null || !alive) return;
        loseTime(dmg, null);
        invulnerable = INVULNERABLE_TIME;
    }


    /** Segundos que pierde por cada segundo real: 1 fría, hasta 1 + MAX_EXTRA_DRAIN al rojo. */
    public double drainRate() {
        return 1 + MAX_EXTRA_DRAIN * heat * heat;
    }


    /** Quita segundos al contador; si llega a 0 la máquina explota (world == null: explota en el próximo update). */
    private void loseTime(double seconds, GameWorld world) {
        if (!alive) return;
        timeLeft = Math.max(0, timeLeft - seconds);
        hp = (int) Math.ceil(timeLeft);
        if (timeLeft <= 0 && world != null) explode(world);
    }


    private void explode(GameWorld world) {
        hp = 0;
        alive = false;
        vx = 0;
        world.spawn(new Explosion(getCenterX(), y + height * 0.35, 70, 0)); // la mochila revienta
    }


    /** Segundos que le quedan (con decimales). */
    public double getTimeLeft() { return timeLeft; }


    /** Calor de la máquina: 0 fría .. 1 al rojo. */
    public double getHeat() { return heat; }


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
        if (alive) drawHeat(gc, img, dx, dy, dw, dh);
    }


    /**
     * Sobrecalentamiento: la mochila (los píxeles morados del sprite) se tiñe de rojo según el calor,
     * titila cuando está casi al máximo y echa vapor desde la mitad del calor.
     */
    private void drawHeat(GraphicsContext gc, Image img, double dx, double dy, double dw, double dh) {
        if (heat < 0.05) return;
        Image hot = Sprites.heatMask(img);
        if (hot != null) {
            double alpha = Math.min(1, heat * 1.1);
            if (heat > 0.85) alpha *= 0.65 + 0.35 * Math.abs(Math.sin(clock * 14)); // titila
            gc.setGlobalAlpha(alpha);
            if (facing > 0) gc.drawImage(hot, dx, dy, dw, dh);
            else gc.drawImage(hot, dx + dw, dy, -dw, dh);
            gc.setGlobalAlpha(1);
        }
        if (heat < 0.5) return;
        double packX = facing > 0 ? x + 2 : x + width - 8, packY = y + height * 0.1; // arriba de la mochila
        for (int k = 0; k < 3; k++) {
            double phase = (clock * 1.6 + k / 3.0) % 1.0;
            gc.setGlobalAlpha((1 - phase) * (heat - 0.4));
            gc.setFill(Color.web("#e8e8e8"));
            double size = 4 + phase * 6;
            gc.fillRect(Math.round(packX + Math.sin(clock * 3 + k) * 3 - size / 2), Math.round(packY - phase * 26), size, size);
        }
        gc.setGlobalAlpha(1);
    }


    /** El dibujo de la pose actual; si esa skin no la tiene, los cuadros de caminar. */
    private Image currentImage() {
        String pose = pose();
        if (pose != null) {
            double h = pose.equals("muerto") ? HEIGHT * 0.8 : pose.equals("agachado") ? HEIGHT * 0.68 : HEIGHT * 1.2;
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


