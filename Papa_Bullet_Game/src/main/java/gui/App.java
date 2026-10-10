package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Config;

import java.io.IOException;

/**
 * Arranque de la aplicación y cambio de pantallas.
 * Ejecutar normal:           mvn javafx:run
 * Saltarse el menú (dev):    mvn javafx:run -Djavafx.args="--game"
 */
public class App extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Papa Bullet");
        stage.setResizable(false);
        if (getParameters().getRaw().contains("--game")) showGame();
        else showMainMenu();
        stage.show();
    }

    public static void showMainMenu() { show("/assets/views/MainMenu.fxml"); }

    public static void showGame() { show("/assets/views/Game.fxml"); }

    private static void show(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            stage.setScene(new Scene(root, Config.WIDTH, Config.HEIGHT));
            stage.sizeToScene();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar " + fxml, e);
        }
    }
}