package control;

import gui.App;
import javafx.fxml.FXML;

/**
 * [STUB DE P1 — P5 lo reemplaza/completa]
 * Solo lo mínimo para poder arrancar el juego desde el menú: START abre el juego.
 * P5: SKINS -> Skins.fxml y la variable compartida de skin.
 */
public class MainMenuController {

    @FXML private void onStartClicked() {
        App.showGame();
    }

    @FXML private void onSkinsClicked() {
        // TODO(P5): abrir Skins.fxml
    }
}
