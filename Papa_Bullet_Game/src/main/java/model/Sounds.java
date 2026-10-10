package model;

import javafx.scene.media.AudioClip;

import java.net.URL;
import structures.HashTable;

/**
 * Carga y cachea efectos de sonido cortos de resources/assets/sounds/.
 * Uso: Sounds.play("frenchspikessound.wav"). Cada archivo se carga una sola vez.
 */
public final class Sounds {
    private static final HashTable<String, AudioClip> CACHE = new HashTable<>();
    private static final String BASE = "/assets/sounds/";
    private static double volume = 0.8;

    private Sounds() {}

    public static void play(String file) {
        AudioClip clip = get(file);
        if (clip != null) clip.play(volume);
    }

    /** Volumen general de efectos, 0..1. */
    public static void setVolume(double v) { volume = Math.max(0, Math.min(1, v)); }

    private static AudioClip get(String file) {
        if (CACHE.containsKey(file)) return CACHE.get(file);
        AudioClip clip = null;
        try {
            URL url = Sounds.class.getResource(BASE + file);
            if (url != null) clip = new AudioClip(url.toExternalForm());
            else System.err.println("[Sounds] No existe " + BASE + file);
        } catch (Exception ex) {
            System.err.println("[Sounds] Error cargando " + file + ": " + ex);
        }
        CACHE.put(file, clip);
        return clip;
    }
}