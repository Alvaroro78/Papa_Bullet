package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.function.Supplier;

/**
 * Arma tirada en el piso (letras 'F' y 'A' del mapa). Flota con un brillo; cuando el jugador la toca,
 * la recibe y desaparece. Si ya la tenía, solo la selecciona.
 */
public class WeaponPickUp extends Entity {
    private final Supplier<Weapon> factory;
    private final String icon;
    private double time;

    public WeaponPickUp(Supplier<Weapon> factory) {
        this.factory = factory;
        this.icon = factory.get().getIcon();
        width = 32;
        height = 32;
        team = Team.NEUTRAL;
        collidesWithTiles = true;
        gravityScale = 1;
    }

    @Override
    public void update(double dt, GameWorld world) {
        time += dt;
    }

    @Override
    public void onCollide(Entity other, GameWorld world) {
        if (other instanceof Player p) {
            p.addWeapon(factory.get());
            kill();
        }
    }

    @Override
    public void render(GraphicsContext gc, Camera cam) {
        double bob = Math.sin(time * 4) * 4;
        double cx = cam.toScreenX(getCenterX()), cy = cam.toScreenY(getCenterY()) - 6 + bob;
        double glow = 22 + Math.sin(time * 6) * 3;
        gc.setGlobalAlpha(0.45);
        gc.setFill(Color.web("#ffe14a"));
        gc.fillOval(cx - glow, cy - glow, glow * 2, glow * 2);
        gc.setGlobalAlpha(1);
        Image img = Sprites.get(icon, 36, 36);
        if (img != null) gc.drawImage(img, cx - img.getWidth() / 2, cy - img.getHeight() / 2);
        else {
            gc.setFill(Color.web("#ffe14a"));
            gc.fillRect(cx - 12, cy - 12, 24, 24);
        }
    }
}