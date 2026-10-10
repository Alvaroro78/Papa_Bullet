package model;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Música de fondo en loop. Solo suena una canción a la vez:
 * al pedir otra, para la anterior. Si se pide la misma que ya suena, no la reinicia.
 */
public final class Music {
    private static final String BASE = "/assets/sounds/";
    private static MediaPlayer player;
    private static String current = "";
    private static double volume = 0.4;

    private Music() {}

    public static void play(String file) {
        if (file.equals(current)) return;      // ya está sonando: no la reinicia
        stop();
        URL url = Music.class.getResource(BASE + file);
        if (url == null) {
            System.err.println("[Music] No existe " + BASE + file);
            return;
        }
        try {
            player = new MediaPlayer(new Media(url.toExternalForm()));
            player.setCycleCount(MediaPlayer.INDEFINITE);   // loop infinito
            player.setVolume(volume);
            player.play();
            current = file;
        } catch (Exception ex) {
            System.err.println("[Music] Error cargando " + file + ": " + ex);
            player = null;
        }
    }

    /** Nivel 0 = tutorial, 1..3 = road level N. */
    public static void playLevel(int index) {
        play(index == 0 ? "road tutorial.mp3" : "road level " + index + ".mp3");
    }

    public static void pause()  { if (player != null) player.pause(); }

    public static void resume() { if (player != null) player.play(); }

    public static void stop() {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
        }
        current = "";
    }

    /** Volumen de la música, 0..1. */
    public static void setVolume(double v) {
        volume = Math.max(0, Math.min(1, v));
        if (player != null) player.setVolume(volume);
    }
}