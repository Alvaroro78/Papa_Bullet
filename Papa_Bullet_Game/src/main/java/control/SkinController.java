package control;

import gui.App;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import model.Player;
import model.Sprites;

/**
 * Pantalla de Skins (P5): flechas para pasar entre skins, vista previa grande y VOLVER.
 * Cada skin = un juego de sprites + un tono. Para una skin con dibujos propios (ej. "PJ2_1.png"...)
 * basta agregar una línea en SKINS con ese prefijo.
 */
public class SkinController {

    private record Skin(String name, String sprites, double hue) {}

    private static final Skin[] SKINS = {
            new Skin("CLÁSICO", "PJ", 0),
            new Skin("NOCTURNO", "PJ", -0.45),
            new Skin("TÓXICO", "PJ", 0.3),
            new Skin("FUEGO", "PJ", 0.9),
    };
    private static int index; // static: al volver a esta pantalla sigue marcada la última elegida

    @FXML private Canvas background;
    @FXML private Canvas preview;
    @FXML private Text skinName;

    @FXML
    private void initialize() {
        MainMenuController.paintScenery(background.getGraphicsContext2D(), false);
        show();
    }

    @FXML private void onPrevClicked() { change(-1); }

    @FXML private void onNextClicked() { change(1); }

    @FXML private void onBackClicked() { App.showMainMenu(); }

    /** Pasa a la skin anterior/siguiente y queda elegida de una vez. */
    private void change(int step) {
        index = Math.floorMod(index + step, SKINS.length);
        show();
    }

    private void show() {
        Skin s = SKINS[index];
        Player.selectedSkin = s.sprites();
        Player.selectedHue = s.hue();
        skinName.setText(s.name() + "  " + (index + 1) + "/" + SKINS.length);

        GraphicsContext gc = preview.getGraphicsContext2D();
        gc.clearRect(0, 0, preview.getWidth(), preview.getHeight());
        Image img = Sprites.get(s.sprites() + "_1.png", preview.getWidth(), preview.getHeight());
        if (img == null) return;
        gc.setEffect(s.hue() != 0 ? new ColorAdjust(s.hue(), 0, 0, 0) : null);
        gc.drawImage(img, (preview.getWidth() - img.getWidth()) / 2, preview.getHeight() - img.getHeight());
        gc.setEffect(null);
    }
}