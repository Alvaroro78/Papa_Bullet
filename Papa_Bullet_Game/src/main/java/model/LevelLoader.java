package model;

import javafx.geometry.Rectangle2D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTRATO 4 — lee un .txt de resources/assets/levels/ (una letra por baldosa de 40x40) y crea las entidades.
 *
 * Letras: . aire | # suelo | = plataforma | P jugador | R/G/S policía porra/pistola/escudo |
 *         H helicóptero | B jefe | K King Papa | J zona de jetpack
 *
 * Extras (opcionales):
 *  - Una línea que empieza con ';' es comentario. ";nombre=Univalle" fija el nombre que ve el HUD.
 *  - Filas de distinto largo se rellenan con aire a la derecha.
 *  - Si el mapa tiene menos de 15 filas se rellena con aire ARRIBA (queda pegado al fondo).
 *  - Cualquier otro carácter se trata como aire (y se avisa por consola).
 */
public final class LevelLoader {
    /** Orden de los niveles del juego. */
    public static final String[] LEVEL_FILES = {"nivel0", "nivel1", "nivel2", "nivel3"};
    /** Mapa de reserva si aún no existe el .txt de un nivel. */
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
            System.err.println("[LevelLoader] Falta " + file + ".txt, uso " + FALLBACK + ".txt");
            lv = load(FALLBACK);
        }
        if (lv == null) throw new IllegalStateException("No hay ni " + file + ".txt ni " + FALLBACK + ".txt");
        return lv;
    }

    /** Carga assets/levels/{name}.txt; devuelve null si no existe. */
    public static Level load(String name) {
        try (InputStream in = LevelLoader.class.getResourceAsStream("/assets/levels/" + name + ".txt")) {
            if (in == null) return null;
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
            }
            return parse(name, lines);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el nivel " + name, e);
        }
    }

    /** Convierte las líneas de un .txt en un Level (público para poder probarlo con texto). */
    public static Level parse(String fileName, List<String> lines) {
        String displayName = defaultName(fileName);
        List<String> rowsText = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.replace("\r", "");
            if (line.startsWith(";")) {
                String meta = line.substring(1).trim();
                if (meta.toLowerCase().startsWith("nombre=")) displayName = meta.substring(7).trim();
            } else if (!line.isBlank()) {
                rowsText.add(line);
            }
        }
        if (rowsText.isEmpty()) throw new IllegalArgumentException("Nivel vacío: " + fileName);

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
                    System.err.println("[LevelLoader] " + fileName + ": carácter desconocido '" + ch + "' (fila " + (i + 1) + ", col " + (c + 1) + ") => aire");
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
