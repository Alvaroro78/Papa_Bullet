package control;

import gui.App;
import model.Config;
import model.GameLoop;
import model.GameWorld;
import model.InputHandler;
import model.Overlay;
import model.Entity;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de Game.fxml (P1): conecta el Canvas 800x600 con el GameLoop y el GameWorld,
 * y gestiona la pausa como una capa (Pause.fxml) encima del juego.
 *
 * Teclas de desarrollo:  Esc pausa | F1 hitboxes + datos | F2 saltar al siguiente nivel | F3 reiniciar nivel
 */
public class GameController implements PauseHandler {

    @FXML private StackPane root;
    @FXML private Canvas canvas;
    @FXML private Text levelLabel;
    @FXML private Button pauseButton;

    private InputHandler input;
    private GameWorld world;
    private GameLoop loop;
    private GraphicsContext gc;

    private final List<Overlay> overlays = new ArrayList<>();
    private Scene scene;
    private Parent pauseLayer;
    private boolean paused;
    private boolean debug;
    private boolean leaving;
    private String shownLevelName = "";

    private final EventHandler<KeyEvent> devKeys = this::onDevKey;

    @FXML
    private void initialize() {
        input = new InputHandler();
        world = new GameWorld(input);
        world.loadLevel(0);
        gc = canvas.getGraphicsContext2D();
        loop = new GameLoop(this::update, this::render);

        pauseButton.setFocusTraversable(false);   // que Espacio/Enter no activen el botón por accidente
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) attachTo(newScene);
            else detach();
        });

        // P5: aquí se registra el HUD ->  addOverlay(new Hud());
    }

    /** P5: registra capas que se dibujan encima del mundo (HUD). */
    public void addOverlay(Overlay overlay) {
        overlays.add(overlay);
    }

    public GameWorld getWorld() { return world; }

    // ===================== ciclo de vida =====================

    private void attachTo(Scene s) {
        scene = s;
        input.attach(s);
        s.addEventFilter(KeyEvent.KEY_PRESSED, devKeys);
        s.windowProperty().addListener((o, oldW, newW) -> {
            if (newW != null) newW.focusedProperty().addListener((fo, was, is) -> { if (!is) input.clear(); });
        });
        loop.start();
    }

    private void detach() {
        loop.stop();
        if (scene != null) {
            input.detach(scene);
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, devKeys);
            scene = null;
        }
    }

    // ===================== frame =====================

    private void update(double dt) {
        world.update(dt);
        input.endFrame();

        String name = world.getLevel().getName();
        if (!name.equals(shownLevelName)) {
            shownLevelName = name;
            levelLabel.setText("NIVEL " + world.getLevelIndex() + " · " + name);
        }
        if (world.getState() == GameWorld.State.GAME_COMPLETE && world.getStateTime() > 4 && !leaving) {
            exitToMenu();
        }
    }

    private void render() {
        world.render(gc);
        for (Overlay o : overlays) o.render(gc, world);
        drawStateBanner();
        if (debug) drawDebug();
    }

    private void drawStateBanner() {
        String msg = switch (world.getState()) {
            case PLAYER_DEAD -> "HAS MUERTO";
            case LEVEL_COMPLETE -> "NIVEL COMPLETADO";
            case GAME_COMPLETE -> "¡JUEGO COMPLETADO!";
            default -> null;
        };
        if (msg == null) return;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.BLACK);
        gc.fillText(msg, Config.WIDTH / 2.0 + 3, Config.HEIGHT / 2.0 + 3);
        gc.setFill(Color.web("#ffd24a"));
        gc.fillText(msg, Config.WIDTH / 2.0, Config.HEIGHT / 2.0);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawDebug() {
        var cam = world.getCamera();
        world.getLevel().renderDebug(gc, cam);
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        for (Entity e : world.getEntities()) {
            Rectangle2D b = e.getBounds();
            gc.strokeRect(Math.round(cam.toScreenX(b.getMinX())) + 0.5, Math.round(cam.toScreenY(b.getMinY())) + 0.5,
                    b.getWidth(), b.getHeight());
        }
        Entity p = world.getPlayer();
        gc.setFont(Font.font("Monospaced", 12));
        gc.setFill(Color.WHITE);
        gc.fillText(String.format("cam=%.0f  jugador=(%.0f, %.0f) suelo=%b  entidades=%d  estado=%s",
                cam.getX(), p.getX(), p.getY(), p.isOnGround(), world.getEntities().size(), world.getState()), 10, Config.HEIGHT - 10);
    }

    // ===================== teclas =====================

    private void onDevKey(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> { if (paused) resume(); else pause(); e.consume(); }
            case F1 -> debug = !debug;
            case F2 -> { if (!paused) world.loadLevel((world.getLevelIndex() + 1) % model.LevelLoader.levelCount()); }
            case F3 -> { if (!paused) world.restartLevel(); }
            default -> { }
        }
    }

    // ===================== pausa =====================

    /** Botón "II" de Game.fxml. */
    @FXML
    private void onPauseClicked() {
        pause();
    }

    private void pause() {
        if (paused || world.getState() == GameWorld.State.GAME_COMPLETE) return;
        try {
            FXMLLoader fx = new FXMLLoader(App.class.getResource("/assets/views/Pause.fxml"));
            pauseLayer = fx.load();
            PauseController pc = fx.getController();
            pc.setHandler(this);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar Pause.fxml", ex);
        }
        paused = true;
        loop.stop();
        input.clear();
        root.getChildren().add(pauseLayer);
    }

    @Override
    public void resume() {
        if (!paused) return;
        root.getChildren().remove(pauseLayer);
        pauseLayer = null;
        paused = false;
        input.clear();
        loop.start();
    }

    @Override
    public void restart() {
        resume();
        world.restartLevel();
    }

    @Override
    public void exitToMenu() {
        if (leaving) return;
        leaving = true;
        detach();
        App.showMainMenu();
    }
}
