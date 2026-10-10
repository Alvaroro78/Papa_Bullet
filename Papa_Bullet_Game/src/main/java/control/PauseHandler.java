package control;

/**
 * Lo que el menú de pausa (P5) le pide al juego (P1). GameController lo implementa y se lo
 * entrega a PauseController con {@code pauseController.setHandler(this)}.
 */
public interface PauseHandler {
    /** Continuar: quita la pausa. */
    void resume();

    /** Reiniciar: vuelve a empezar el nivel actual. */
    void restart();

    /** Salir: vuelve al Main Menu. */
    void exitToMenu();
}
