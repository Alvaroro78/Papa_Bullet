package model;

import structures.HashTable;

import java.util.function.Supplier;

/**
 * Qué clase nace con cada letra del mapa (ver LevelLoader para la lista de letras).
 * Para agregar un tipo nuevo: una línea register(letra, Clase::new) y la letra en LevelLoader.
 * El jefe sabe de qué nivel es con currentLevelIndex (lo fija GameWorld al cargar).
 */
public final class EntityRegistry {
    private static final HashTable<Character, Supplier<Entity>> FACTORIES = new HashTable<>();
    private static int currentLevelIndex;

    static {
        registerDefaults();
    }

    private EntityRegistry() {}

    private static void registerDefaults() {
        // --- P2 ---
        register('P', Player::new);
        // --- P4 ---
        register('R', PoliceBaton::new);
        register('G', PolicePistol::new);
        register('S', PoliceShield::new);
        register('H', Helicopter::new);
        register('B', () -> new Boss(currentLevelIndex)); // un jefe distinto por nivel
        // --- P1 ---
        register('K', KingPapa::new);
        // --- P3: armas para recoger ---
        register('F', () -> new WeaponPickUp(FrenchSpikes::new));
        register('A', () -> new WeaponPickUp(OilSlick::new));
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