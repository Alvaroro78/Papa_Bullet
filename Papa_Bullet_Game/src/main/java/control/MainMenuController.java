package control;

import gui.App;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.Background;
import model.Camera;
import model.Config;
import model.Level;
import model.LevelLoader;
import model.Player;
import model.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú principal. El fondo es el mismo paisaje del juego (Background + baldosas de Level),
 * pintado una sola vez en el Canvas que está detrás de los botones.
 * P5: SKINS -> Skins.fxml y Player.selectedSkin.
 */
public class MainMenuController {

    @FXML private Canvas background;

    @FXML
    private void initialize() {
        GraphicsContext gc = background.getGraphicsContext2D();
        Camera cam = new Camera();
        Background.render(gc, cam);
        Level ground = LevelLoader.parse("MENU", groundRows());
        ground.render(gc, cam);

        // el científico en el acantilado de la izquierda y la freidora en el de la derecha
        double cliffTop = 12 * Config.TILE;
        Image pj = Sprites.get(Player.selectedSkin + "_1.png", 160, 80);
        if (pj != null) gc.drawImage(pj, 34, cliffTop - pj.getHeight() * 0.92);
        Image fryer = Sprites.get("Freidora.png", 56, 64);
        if (fryer != null) gc.drawImage(fryer, Config.WIDTH - 90, cliffTop - fryer.getHeight());
    }

    /** Piso con un acantilado a cada lado (3 baldosas de ancho, una más alto). */
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
        // TODO(P5): abrir Skins.fxml
    }
}