package model;

import javafx.scene.canvas.GraphicsContext;

/**
 * Capa que se dibuja ENCIMA del mundo (HUD). P5 implementa {@code Hud implements Overlay}
 * y lo registra con {@code gameController.addOverlay(new Hud())}.
 */
@FunctionalInterface
public interface Overlay {
    void render(GraphicsContext gc, GameWorld world);
}
