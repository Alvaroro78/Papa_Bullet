package gui;

import javafx.application.Application;

/**
 * Punto de entrada para ejecutar desde el IDE (IntelliJ): una clase main que NO extiende
 * Application evita el error "JavaFX runtime components are missing".
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
