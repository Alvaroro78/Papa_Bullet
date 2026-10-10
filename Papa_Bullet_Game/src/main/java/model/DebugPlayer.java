package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 * Jugador de PRUEBA (un rectángulo): A/D camina, L salta, S+L baja de una plataforma '='.
 * Existe para el "esqueleto jugable" del día 2. P2 lo reemplaza con su Player real
 * (ver EntityRegistry: una línea).
 */
public class DebugPlayer extends Entity {
    private static final double SPEED = 220;
    private static final double JUMP = 700;
    private boolean facingRight = true;

    public DebugPlayer() {
        width = 28;
        height = 48;
        hp = 5;
        team = Team.PLAYER;
        collidesWithTiles = true;
        gravityScale = 1;
    }

    @Override
    public void update(double dt, GameWorld world) {
        var in = world.getInput();
        double dir = 0;
        if (in.isPressed(KeyCode.A)) dir -= 1;
        if (in.isPressed(KeyCode.D)) dir += 1;
        vx = dir * SPEED;
        if (dir != 0) facingRight = dir > 0;

        if (in.wasPressed(KeyCode.L)) {
            if (in.isPressed(KeyCode.S)) {
                if (onGround) dropThroughPlatform();
            } else if (onGround) {
                vy = -JUMP;
            }
        }
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawBox(gc, cam, alive ? Color.web("#3d7be0") : Color.web("#555555"));
        // "ojo" que marca hacia dónde mira
        gc.setFill(Color.WHITE);
        double sx = Math.round(cam.toScreenX(x)), sy = Math.round(cam.toScreenY(y));
        gc.fillRect(sx + (facingRight ? width - 10 : 4), sy + 8, 6, 6);
    }
}
