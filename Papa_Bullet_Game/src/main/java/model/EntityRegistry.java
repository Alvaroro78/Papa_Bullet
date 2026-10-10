package model;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Qué clase nace con cada letra del mapa. Mientras no existan las clases reales hay
 * placeholders (rectángulos). CUANDO TU CLASE ESTÉ LISTA cambia SOLO TU LÍNEA en registerDefaults():
 *
 *   P2:  register('P', Player::new);
 *   P4:  register('R', PoliceBaton::new);  register('G', PoliceGun::new);  register('S', PoliceShield::new);
 *        register('H', Helicopter::new);   register('B', () -> bossForLevel(...)) (ver nota abajo)
 *
 * Nota jefes: hay un jefe distinto por nivel. Si P4 necesita saber el nivel, puede leerlo en su
 * constructor con {@code EntityRegistry.currentLevelIndex()}.
 */
public final class EntityRegistry {
    private static final Map<Character, Supplier<Entity>> FACTORIES = new HashMap<>();
    private static int currentLevelIndex;

    static {
        registerDefaults();
    }

    private EntityRegistry() {}

    private static void registerDefaults() {
        // --- P2 ---
        register('P', DebugPlayer::new);
        // --- P4 ---
        register('R', () -> new PlaceholderEntity("R", Color.web("#2f4f9f"), 30, 52, 3, false, false));
        register('G', () -> new PlaceholderEntity("G", Color.web("#3a6fbf"), 30, 52, 3, false, false));
        register('S', () -> new PlaceholderEntity("S", Color.web("#1d2f66"), 36, 52, 6, false, false));
        register('H', () -> new PlaceholderEntity("H", Color.web("#7a7f99"), 72, 36, 4, true, false));
        register('B', () -> new PlaceholderEntity("BOSS", Color.web("#b02a2a"), 100, 140, 30, false, true));
        // --- P1 ---
        register('K', KingPapa::new);
    }

    public static void register(char letter, Supplier<Entity> factory) {
        FACTORIES.put(letter, factory);
    }

    /** Crea la entidad de esa letra (null si la letra no crea entidades). */
    public static Entity create(char letter) {
        Supplier<Entity> f = FACTORIES.get(letter);
        return f == null ? null : f.get();
    }

    /** Índice (0..3) del nivel que se está cargando; lo fija GameWorld (no lo llames tú). */
    public static int currentLevelIndex() { return currentLevelIndex; }

    public static void setCurrentLevelIndex(int i) { currentLevelIndex = i; }
}
