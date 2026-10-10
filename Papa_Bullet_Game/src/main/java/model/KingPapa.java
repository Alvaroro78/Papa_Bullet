package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


/**
 * Estación de recarga ('K'). Se dibuja como un CARRITO DE CHOLADO (hielo raspado con fruta, típico de Cali):
 * al usarlo con Ñ devuelve +150 s al contador y ENFRÍA la máquina (calor a 0). La recarga la hace Player.
 * Cada vez que se (re)inicia el nivel se crean carritos nuevos => "una vez por carrito por intento".
 * (La clase conserva el nombre KingPapa para no romper el código de los demás módulos.)
 *
 *   for (KingPapa k : world.getKingPapas())
 *       if (!k.isUsed() && k.getBounds().intersects(getBounds())) { k.markUsed(); ...recargar... }
 */
public class KingPapa extends Entity {
    private boolean used, playerNear;
    private double time;


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
    public void update(double dt, GameWorld world) {
        time += dt;
        Entity p = world.getPlayer();
        playerNear = p != null && p.isAlive() && p.getBounds().intersects(getBounds());
    }


    @Override
    public void render(GraphicsContext gc, Camera cam) {
        double sx = Math.round(cam.toScreenX(getCenterX())), bottom = Math.round(cam.toScreenY(y + height));
        drawCart(gc, sx, bottom, used, time);
        if (playerNear && !used) {                         // cartelito "Ñ" que rebota encima
            double by = bottom - 88 + Math.sin(time * 6) * 3;
            gc.setFill(Color.BLACK);
            gc.fillRect(sx - 12, by - 2, 24, 22);
            gc.setFill(Color.web("#ffe14a"));
            gc.fillRect(sx - 10, by, 20, 18);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
            gc.fillText("Ñ", sx - 5, by + 14);
        }
    }


    /**
     * Carrito de cholado en pixel art, con las ruedas apoyadas en {@code bottom} y centrado en {@code cx}
     * (coordenadas de pantalla). Gastado = sin hielo, sombrilla cerrada y colores apagados.
     * Público y estático para que los menús lo puedan pintar también.
     */
    public static void drawCart(GraphicsContext gc, double cx, double bottom, boolean used, double time) {
        double l = cx - 26;                                // borde izquierdo (el carrito mide 52 px)
        Color wood = Color.web(used ? "#5a5048" : "#f4f1e6"), trim = Color.web(used ? "#3a3a46" : "#2f6fd6");
        // ruedas
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(l + 6, bottom - 10, 10, 10);
        gc.fillRect(l + 36, bottom - 10, 10, 10);
        gc.setFill(Color.web("#8a8a8a"));
        gc.fillRect(l + 9, bottom - 7, 4, 4);
        gc.fillRect(l + 39, bottom - 7, 4, 4);
        // cajón
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(l, bottom - 34, 52, 26);
        gc.setFill(wood);
        gc.fillRect(l + 2, bottom - 32, 48, 22);
        gc.setFill(trim);
        gc.fillRect(l + 2, bottom - 32, 48, 4);
        gc.fillRect(l + 2, bottom - 14, 48, 4);
        // letrero
        gc.setFill(Color.web(used ? "#6a6a6a" : "#d6332f"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
        gc.fillText("CHOLADO", l + 6, bottom - 18);
        // vitrina con hielo y fruta
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(l + 6, bottom - 48, 40, 15);
        gc.setFill(Color.web(used ? "#4a5560" : "#bfe9ff"));
        gc.fillRect(l + 8, bottom - 46, 36, 12);
        if (!used) {
            String[] fruit = {"#ff4a6a", "#ffd24a", "#7ad04a", "#ff8a1a", "#b84aff"};
            for (int i = 0; i < 5; i++) {                  // vasitos de colores
                gc.setFill(Color.web(fruit[i]));
                gc.fillRect(l + 10 + i * 7, bottom - 42, 5, 7);
                gc.setFill(Color.WHITE);
                gc.fillRect(l + 10 + i * 7, bottom - 44, 5, 2);
            }
            gc.setFill(Color.WHITE);                       // brillito del hielo
            gc.fillRect(l + 9, bottom - 45, 3, 2);
        }
        // palo y sombrilla
        gc.setFill(Color.web("#3a3a3a"));
        gc.fillRect(cx - 1, bottom - 72, 3, 26);
        if (used) {                                        // cerrada
            gc.setFill(Color.web("#5a5a5a"));
            gc.fillRect(cx - 3, bottom - 80, 7, 20);
        } else {                                           // abierta, a rayas, se mece un poquito
            double sway = Math.round(Math.sin(time * 2) * 1.5);
            for (int i = 0; i < 6; i++) {
                gc.setFill(Color.web(i % 2 == 0 ? "#d6332f" : "#ffe14a"));
                gc.fillRect(cx - 27 + i * 9 + sway, bottom - 80 + Math.abs(i - 2.5) * 2, 9, 8 - Math.abs(i - 2.5));
            }
            gc.setFill(Color.web("#1a1a1a"));
            gc.fillRect(cx - 28 + sway, bottom - 72, 56, 2);
        }
    }
}


