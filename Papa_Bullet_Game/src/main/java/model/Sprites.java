package model;

import javafx.scene.image.Image;

import java.io.InputStream;
import structures.HashTable;

/**
 * Carga y cachea sprites de {@code resources/assets/sprites/}.
 * Los PNG del repo son enormes (hasta 2048x2048), así que SIEMPRE pide el tamaño final:
 * {@code Sprites.get("PJ_1.png", 40, 60)} escala al cargar y ahorra memoria.
 * Devuelve null si el archivo no existe => el que dibuja vuelve al rectángulo de color.
 */
public final class Sprites {
    private static final HashTable<String, Image> CACHE = new HashTable<>();
    private static final String BASE = "/assets/sprites/";

    private Sprites() {}

    /** Escala para que quepa en w x h conservando proporción. */
    public static Image get(String file, double w, double h) {
        String key = file + "@" + w + "x" + h;
        if (CACHE.containsKey(key)) return CACHE.get(key);
        Image img = null;
        try (InputStream in = Sprites.class.getResourceAsStream(BASE + file)) {
            if (in != null) img = new Image(in, w, h, true, true);
            else System.err.println("[Sprites] No existe " + BASE + file);
        } catch (Exception ex) {
            System.err.println("[Sprites] Error cargando " + file + ": " + ex);
        }
        CACHE.put(key, img);
        return img;
    }
}