package control;


import gui.App;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import model.Background;
import model.Camera;
import model.Config;
import model.KingPapa;
import model.Level;
import model.LevelLoader;
import model.Player;
import model.Sprites;


import java.util.ArrayList;
import java.util.List;


/**
 * Menú principal. El fondo es el mismo paisaje del juego (Background + baldosas de Level),
 * pintado una sola vez en el Canvas que está detrás de los botones.
 */
public class MainMenuController {


    @FXML private Canvas background;


    @FXML
    private void initialize() {
        paintScenery(background.getGraphicsContext2D(), true);
    }


    /** Paisaje de los menús: fondo, piso con un acantilado a cada lado y (opcional) el científico y el carrito de cholado. */
    static void paintScenery(GraphicsContext gc, boolean withCharacters) {
        Camera cam = new Camera();
        Background.render(gc, cam);
        LevelLoader.parse("MENU", groundRows()).render(gc, cam);
        if (!withCharacters) return;


        double cliffTop = 12 * Config.TILE;
        Image pj = Sprites.get(Player.selectedSkin + "_1.png", 160, 80);
        if (pj != null) {
            if (Player.selectedHue != 0) gc.setEffect(new ColorAdjust(Player.selectedHue, 0, 0, 0));
            gc.drawImage(pj, 34, cliffTop - pj.getHeight() * 0.92);
            gc.setEffect(null);
        }
        KingPapa.drawCart(gc, Config.WIDTH - 62, cliffTop, false, 0);
    }


    private static List<String> groundRows() {
        List<String> rows = new ArrayList<>();
        for (int r = 0; r < 12; r++) rows.add("....................");
        rows.add("###..............###");
        rows.add("####################");
        rows.add("####################");
        return rows;
    }


    @FXML private void onStartClicked() {
        App.showGame();
    }


    @FXML private void onSkinsClicked() {
        App.showSkins();
    }
}


