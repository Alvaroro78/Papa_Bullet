package model;

import javafx.geometry.Rectangle2D;

import structures.HashTable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTRATO 4 — lee un nivel de resources/assets/levels/{nombre}.json y crea las entidades.
 *
 * <pre>
 * {
 *   "nombre": "UNIVALLE",            // lo que muestra el juego (opcional)
 *   "mapa": [                        // una letra por baldosa de 40x40, una fila por texto
 *     "#......#",
 *     "########"
 *   ]
 * }
 * </pre>
 * Letras: . aire | # suelo | = plataforma | P jugador | R/G/S policía porra/pistola/escudo |
 *         H helicóptero | B jefe | K King Papa | J zona de jetpack
 *
 *  - Filas de distinto largo se rellenan con aire a la derecha.
 *  - Si el mapa tiene menos de 15 filas se rellena con aire ARRIBA (queda pegado al fondo).
 *  - Cualquier otro carácter se trata como aire (y se avisa por consola).
 */
public final class LevelLoader {
    /** Orden de los niveles del juego. */
    public static final String[] LEVEL_FILES = {"nivel0", "nivel1", "nivel2", "nivel3"};
    /** Mapa de reserva si aún no existe el .json de un nivel. */
    public static final String FALLBACK = "prueba";

    private static final String[] DEFAULT_NAMES = {"UNIVALLE", "AYTONOMA", "JABERUMBA", "ACESI"};
    private static final String ENTITY_LETTERS = "PRGSHBKJ";

    private LevelLoader() {}

    public static int levelCount() { return LEVEL_FILES.length; }

    /** Carga el nivel número {@code index}; si su archivo no existe usa {@link #FALLBACK}. */
    public static Level loadIndex(int index) {
        String file = LEVEL_FILES[index];
        Level lv = load(file);
        if (lv == null) {
            System.err.println("[LevelLoader] Falta " + file + ".json, uso " + FALLBACK + ".json");
            lv = load(FALLBACK);
        }
        if (lv == null) throw new IllegalStateException("No hay ni " + file + ".json ni " + FALLBACK + ".json");
        return lv;
    }

    /** Carga assets/levels/{name}.json; devuelve null si no existe. */
    public static Level load(String name) {
        try (InputStream in = LevelLoader.class.getResourceAsStream("/assets/levels/" + name + ".json")) {
            if (in == null) return null;
            return fromJson(name, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el nivel " + name, e);
        }
    }

    /** Convierte el texto JSON de un nivel en un Level (público para poder probarlo sin archivo). */
    @SuppressWarnings("unchecked")
    public static Level fromJson(String fileName, String json) {
        if (!(JsonReader.parse(json) instanceof HashTable<?, ?> raw)) {
            throw new IllegalArgumentException(fileName + ".json debe ser un objeto { \"nombre\": ..., \"mapa\": [...] }");
        }
        HashTable<String, Object> obj = (HashTable<String, Object>) raw;
        if (!(obj.get("mapa") instanceof List<?> mapa)) {
            throw new IllegalArgumentException(fileName + ".json no tiene \"mapa\" (lista de filas de texto)");
        }
        List<String> rows = new ArrayList<>();
        for (Object row : mapa) rows.add(String.valueOf(row));
        Object name = obj.get("nombre");
        return parse(name instanceof String n ? n : defaultName(fileName), rows);
    }

    /** Arma el Level a partir de las filas del mapa. */
    public static Level parse(String displayName, List<String> rowsText) {
        if (rowsText.isEmpty()) throw new IllegalArgumentException("Nivel vacío: " + displayName);

        int cols = rowsText.stream().mapToInt(String::length).max().getAsInt();
        int pad = Math.max(0, Config.VISIBLE_ROWS - rowsText.size());
        int rows = rowsText.size() + pad;

        char[][] tiles = new char[rows][cols];
        for (char[] r : tiles) java.util.Arrays.fill(r, '.');
        List<Level.Spawn> spawns = new ArrayList<>();
        List<int[]> jTiles = new ArrayList<>();
        boolean warned = false;

        for (int i = 0; i < rowsText.size(); i++) {
            String line = rowsText.get(i);
            int r = i + pad;
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                if (ch == '#' || ch == '=') tiles[r][c] = ch;
                else if (ch == 'J') jTiles.add(new int[]{c, r});
                else if (ENTITY_LETTERS.indexOf(ch) >= 0) spawns.add(new Level.Spawn(ch, c, r));
                else if (ch != '.' && ch != ' ' && !warned) {
                    System.err.println("[LevelLoader] " + displayName + ": carácter desconocido '" + ch + "' (fila " + (i + 1) + ", col " + (c + 1) + ") => aire");
                    warned = true;
                }
            }
        }
        List<Rectangle2D> zones = Level.mergeRuns(jTiles);
        return new Level(displayName, tiles, spawns, zones);
    }

    /**
     * Crea una entidad por cada letra del mapa (usa {@link EntityRegistry}) y las coloca
     * centradas en su baldosa y apoyadas en su base. 'J' no crea entidad (es una zona).
     */
    public static List<Entity> createEntities(Level level) {
        List<Entity> out = new ArrayList<>();
        int t = Config.TILE;
        for (Level.Spawn s : level.getSpawns()) {
            Entity e = EntityRegistry.create(s.letter());
            if (e == null) continue;
            double x = s.col() * t + (t - e.getWidth()) / 2.0;
            double y = (s.row() + 1) * t - e.getHeight();
            e.setPosition(x, y);
            out.add(e);
        }
        return out;
    }

    private static String defaultName(String fileName) {
        for (int i = 0; i < LEVEL_FILES.length; i++) {
            if (LEVEL_FILES[i].equals(fileName)) return DEFAULT_NAMES[i];
        }
        return fileName.toUpperCase();
    }
}