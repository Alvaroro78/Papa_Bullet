package model;

import javafx.animation.AnimationTimer;

import java.util.function.DoubleConsumer;

/**
 * Bucle del juego con AnimationTimer: calcula dt (segundos), llama update(dt) y luego render().
 * stop() / start() sirven para la pausa; al reanudar el primer frame no acumula tiempo.
 */
public class GameLoop extends AnimationTimer {
    private final DoubleConsumer update;
    private final Runnable render;
    private long last;
    private boolean running;

    public GameLoop(DoubleConsumer update, Runnable render) {
        this.update = update;
        this.render = render;
    }

    @Override
    public void handle(long now) {
        if (last == 0) {          // primer frame tras start(): solo fija la referencia
            last = now;
            return;
        }
        double dt = Math.min((now - last) / 1_000_000_000.0, Config.MAX_DT);
        last = now;
        update.accept(dt);
        render.run();
    }

    @Override
    public void start() {
        last = 0;
        running = true;
        super.start();
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
    }

    public boolean isRunning() { return running; }
}
