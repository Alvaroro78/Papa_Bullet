package control;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CONTRATO DEL DÍA 1 (dueño: P1). Nadie más escucha el teclado.
 * Guarda además el ORDEN en que se presionaron las teclas, porque el apuntado
 * del jugador cambia según si se oprimió primero el lado o arriba/abajo.
 */
public class InputHandler {
    private final Map<KeyCode, Long> pressed = new HashMap<>(); // tecla -> número de orden
    private final Set<String> typed = new HashSet<>();
    private long counter;

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            pressed.putIfAbsent(e.getCode(), counter++); // putIfAbsent: la autorrepetición no cambia el orden
            if (!e.getText().isEmpty()) typed.add(e.getText().toLowerCase());
        });
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));
    }

    public boolean isPressed(KeyCode key) {
        return pressed.containsKey(key);
    }

    /** Menor = se presionó antes. Long.MAX_VALUE si no está presionada. */
    public long pressOrder(KeyCode key) {
        return pressed.getOrDefault(key, Long.MAX_VALUE);
    }

    /**
     * Para teclas sin KeyCode fijo, como la ñ: devuelve true una sola vez por pulsación.
     * Quien la use debe llamarlo en TODOS los frames, para no guardar pulsaciones viejas.
     */
    public boolean consumeTyped(String text) {
        return typed.remove(text);
    }
}