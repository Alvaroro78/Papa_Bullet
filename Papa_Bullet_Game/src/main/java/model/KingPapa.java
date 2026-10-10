package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Estación de recarga ('K'). Solo guarda si ya se usó: la recarga en sí (tecla Ñ, +150) la hace P2.
 * Cada vez que se (re)inicia el nivel se crean King Papa nuevos => "una vez por King Papa por intento".
 *
 *   for (KingPapa k : world.getKingPapas())
 *       if (!k.isUsed() && k.getBounds().intersects(getBounds())) { k.markUsed(); ...recargar... }
 */
public class KingPapa extends Entity {
    private boolean used;

    public KingPapa() {
        width = 40;
        height = 56;
        team = Team.NEUTRAL;
        collidesWithTiles = true;
        gravityScale = 1;
    }

    public boolean isUsed() { return used; }
    public void markUsed() { used = true; }

    @Override
    public void update(double dt, GameWorld world) {}

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawBox(gc, cam, used ? Color.web("#8a7a3a") : Color.web("#f2c230"));
        gc.setFill(Color.web("#3a2208"));
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText("KING", Math.round(cam.toScreenX(x)) + 4, Math.round(cam.toScreenY(y)) + 20);
        gc.fillText(used ? "usado" : "PAPA", Math.round(cam.toScreenX(x)) + 2, Math.round(cam.toScreenY(y)) + 36);
    }
}
