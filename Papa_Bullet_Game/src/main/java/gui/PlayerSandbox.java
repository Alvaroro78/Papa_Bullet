package gui;

import control.InputHandler;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.AimDirection;
import model.AimVector;
import model.Entity;
import model.GameWorld;
import model.Player;
import model.Weapon;

import java.util.ArrayList;
import java.util.List;

/**
 * Banco de pruebas de P2: el jugador en un nivel pequeño, sin esperar el motor de P1.
 * Ejecutar la clase PlayerSandbox.Launch (no PlayerSandbox directamente).
 * Cuando el motor de P1 esté listo, este archivo se puede borrar.
 */
public class PlayerSandbox extends Application {

    private static final int T = GameWorld.TILE;
    private static final String[] MAP = {
            "..............................",
            "..............................",
            "..............................",
            "..............................",
            "..................JJJ.........",
            "..................JJJ.........",
            ".................=====........",
            "..............................",
            "..............................",
            "..............................",
            ".........=====................",
            "........................#.....",
            ".P...K..................#.....",
            "##############################",
            "##############################",
    };

    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> spawned = new ArrayList<>();
    private final List<Rectangle2D> jetZones = new ArrayList<>();
    private final List<Rectangle2D> kingPapas = new ArrayList<>();
    private Player player;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        Scene scene = new Scene(new StackPane(canvas));
        InputHandler input = new InputHandler();
        input.attach(scene);

        GameWorld world = new GameWorld() {
            public boolean isSolid(int col, int row) { return tileAt(col, row) == '#'; }
            public boolean isPlatform(int col, int row) { return tileAt(col, row) == '='; }
            public List<Rectangle2D> getJetpackZones() { return jetZones; }
            public List<Rectangle2D> getKingPapas() { return kingPapas; }
            public void spawn(Entity e) { spawned.add(e); }
        };

        for (int r = 0; r < MAP.length; r++) {
            for (int c = 0; c < MAP[r].length(); c++) {
                switch (MAP[r].charAt(c)) {
                    case 'P' -> player = new Player(c * T, (r + 1) * T - Player.HEIGHT, input, "PJ");
                    case 'J' -> jetZones.add(new Rectangle2D(c * T, r * T, T, T));
                    case 'K' -> kingPapas.add(new Rectangle2D(c * T, r * T, T, T));
                    default -> { }
                }
            }
        }
        player.addWeapon(new TestWeapon("Prueba", Color.GOLD));
        player.addWeapon(new TestWeapon("Prueba 2", Color.TOMATO));
        entities.add(player);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        new AnimationTimer() {
            private long last;

            @Override
            public void handle(long now) {
                double dt = last == 0 ? 0 : Math.min((now - last) / 1e9, 0.05);
                last = now;
                for (Entity e : entities) e.update(dt, world);
                entities.addAll(spawned);
                spawned.clear();
                entities.removeIf(e -> !e.isAlive());
                draw(gc);
            }
        }.start();

        stage.setTitle("Papa Bullet - prueba del jugador");
        stage.setScene(scene);
        stage.show();
    }

    /** Fuera del mapa a los lados hay pared, para que no se salga. */
    private static char tileAt(int col, int row) {
        if (col < 0 || col >= MAP[0].length()) return '#';
        if (row < 0 || row >= MAP.length) return '.';
        return MAP[row].charAt(col);
    }

    private void draw(GraphicsContext gc) {
        double camX = Math.max(0, Math.min(player.getX() - 400, MAP[0].length() * T - 800));
        gc.setFill(Color.web("#4b2f5c"));
        gc.fillRect(0, 0, 800, 600);

        gc.save();
        gc.translate(-camX, 0); // la cámara: todo lo de adentro se dibuja en coordenadas del mundo
        for (int r = 0; r < MAP.length; r++) {
            for (int c = 0; c < MAP[r].length(); c++) {
                switch (MAP[r].charAt(c)) {
                    case '#' -> { gc.setFill(Color.web("#6a4022")); gc.fillRect(c * T, r * T, T, T); }
                    case '=' -> { gc.setFill(Color.web("#d39b5c")); gc.fillRect(c * T, r * T, T, 10); }
                    case 'J' -> { gc.setStroke(Color.LIGHTBLUE); gc.setLineWidth(1); gc.strokeRect(c * T, r * T, T, T); }
                    case 'K' -> {
                        gc.setFill(Color.GOLD);
                        gc.fillRect(c * T, r * T, T, T);
                        gc.setFill(Color.BLACK);
                        gc.fillText("K", c * T + 16, r * T + 25);
                    }
                    default -> { }
                }
            }
        }
        for (Entity e : entities) e.render(gc);
        gc.restore();

        Weapon w = player.getCurrentWeapon();
        gc.setFill(Color.WHITE);
        gc.fillText(String.format("Vida %d · Reserva %d · Apunta %s · Arma %s",
                player.getHp(), player.getReserve(), player.getAim(), w == null ? "-" : w.getName()), 12, 20);
        gc.fillText("A/D mover · L saltar · S+L bajar · W/S apuntar · K disparar · Q arma · O jetpack (bajo J) · Ñ recargar (en K)",
                12, 590);
    }

    /** Arma falsa: dispara una bolita recta para ver hacia dónde apunta el jugador. */
    private record TestWeapon(String name, Color color) implements Weapon {
        public void fire(double x, double y, AimDirection dir, GameWorld world) { world.spawn(new TestBullet(x, y, dir, color)); }
        public String getName() { return name; }
    }

    private static class TestBullet extends Entity {
        private final Color color;
        private double life = 1;

        TestBullet(double x, double y, AimDirection dir, Color color) {
            this.x = x - 4;
            this.y = y - 4;
            this.width = this.height = 8;
            this.vx = AimVector.dx(dir) * 600;
            this.vy = AimVector.dy(dir) * 600;
            this.color = color;
        }

        public void update(double dt, GameWorld world) {
            x += vx * dt;
            y += vy * dt;
            life -= dt;
            if (life <= 0) alive = false;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x, y, width, height);
        }
    }

    /** Lanzador: sin module-info, JavaFX no arranca si el main está en la clase que hereda de Application. */
    public static class Launch {
        public static void main(String[] args) {
            Application.launch(PlayerSandbox.class, args);
        }
    }
}