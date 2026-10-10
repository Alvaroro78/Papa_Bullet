package model;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * CONTRATO 3 — teclas presionadas. Nadie más escucha el teclado.
 *
 * <pre>
 *   input.isPressed(KeyCode.A)     // mantenida
 *   input.wasPressed(KeyCode.L)    // solo el frame en que se pulsó (ideal para saltar / disparar 1 vez)
 *   input.wasTyped("ñ")            // texto escrito este frame (para la Ñ, que no tiene KeyCode fijo)
 * </pre>
 */
public class InputHandler {
    private final Set<KeyCode> down = new HashSet<>();
    private final Set<KeyCode> pressedThisFrame = new HashSet<>();
    private final Set<String> typedThisFrame = new HashSet<>();

    private final EventHandler<KeyEvent> onPressed = e -> press(e.getCode());
    private final EventHandler<KeyEvent> onReleased = e -> release(e.getCode());
    private final EventHandler<KeyEvent> onTyped = e -> type(e.getCharacter());

    /** Conecta el handler a una escena (filtros: ningún botón se come las teclas). */
    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, onPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, onReleased);
        scene.addEventFilter(KeyEvent.KEY_TYPED, onTyped);
    }

    public void detach(Scene scene) {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, onPressed);
        scene.removeEventFilter(KeyEvent.KEY_RELEASED, onReleased);
        scene.removeEventFilter(KeyEvent.KEY_TYPED, onTyped);
        clear();
    }

    // --- consulta (la usan los demás módulos) ---

    public boolean isPressed(KeyCode code) {
        return down.contains(code);
    }

    /** true solo en el frame en que la tecla pasó de suelta a presionada. */
    public boolean wasPressed(KeyCode code) {
        return pressedThisFrame.contains(code);
    }

    /** true si ese carácter se escribió este frame (no distingue mayúsculas: "ñ" == "Ñ"). */
    public boolean wasTyped(String character) {
        return typedThisFrame.contains(character.toLowerCase());
    }

    // --- alimentación (la usa el motor; público para poder probar sin ventana) ---

    public void press(KeyCode code) {
        if (code != null && down.add(code)) pressedThisFrame.add(code);
    }

    public void release(KeyCode code) {
        if (code != null) down.remove(code);
    }

    public void type(String character) {
        if (character != null && !character.isEmpty()) typedThisFrame.add(character.toLowerCase());
    }

    /** Lo llama el motor al final de cada frame: borra los eventos "de este frame". */
    public void endFrame() {
        pressedThisFrame.clear();
        typedThisFrame.clear();
    }

    /** Suelta todo (al pausar o al perder el foco, para que no queden teclas "pegadas"). */
    public void clear() {
        down.clear();
        pressedThisFrame.clear();
        typedThisFrame.clear();
    }
}
