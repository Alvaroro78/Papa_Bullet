package control;

import javafx.fxml.FXML;

/**
 * [STUB DE P1 — P5 lo reemplaza/completa]
 * Controlador de Pause.fxml. Solo reenvía los clics al PauseHandler (GameController).
 * Pause.fxml ya tiene CONTINUAR, REINICIAR y SALIR conectados.
 */
public class PauseController {
    private PauseHandler handler;

    public void setHandler(PauseHandler handler) {
        this.handler = handler;
    }

    @FXML private void onResumeClicked() { handler.resume(); }

    @FXML private void onRestartClicked() { handler.restart(); }

    @FXML private void onExitClicked() { handler.exitToMenu(); }
}