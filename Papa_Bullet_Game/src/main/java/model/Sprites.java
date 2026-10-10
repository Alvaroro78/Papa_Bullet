package model;


import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;


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
    private static final HashTable<Image, Image> HEAT_MASKS = new HashTable<>();
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


    /**
     * Máscara de sobrecalentamiento: una copia del sprite donde SOLO la máquina (los píxeles morados de
     * la mochila) queda pintada de rojo-naranja y el resto transparente. Player la dibuja encima del
     * sprite con globalAlpha = calor. Se calcula una vez por imagen. null si la imagen no tiene morado.
     */
    public static Image heatMask(Image src) {
        if (src == null) return null;
        if (HEAT_MASKS.containsKey(src)) return HEAT_MASKS.get(src);
        Image out = null;
        PixelReader pr = src.getPixelReader();
        if (pr != null) {
            int w = (int) src.getWidth(), h = (int) src.getHeight();
            WritableImage mask = new WritableImage(w, h);
            PixelWriter pw = mask.getPixelWriter();
            boolean any = false;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    Color c = pr.getColor(i, j);
                    if (c.getOpacity() > 0.5 && c.getHue() >= 237 && c.getHue() <= 310
                            && c.getSaturation() > 0.2 && c.getBrightness() > 0.3) {
                        double b = c.getBrightness();             // conserva el sombreado del pixel art
                        pw.setColor(i, j, Color.color(1, 0.15 + 0.35 * b, 0.05));
                        any = true;
                    }
                }
            }
            if (any) out = mask;
        }
        HEAT_MASKS.put(src, out);
        return out;
    }
}


