package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CONTRATO 2 — la ÚNICA forma en que los módulos se hablan entre sí.
 *
 *   world.getPlayer()      // Entity (haz cast a tu Player si necesitas métodos propios)
 *   world.getEnemies()     // entidades vivas con team == ENEMY (incluye jefes y sus balas enemigas)
 *   world.getLevel()       // baldosas, zonas de jetpack
 *   world.spawn(entity)    // crear balas, aceite, explosiones, enemigos...
 *   world.bossDefeated()   // el jefe lo llama en onDeath() => el nivel termina
 *
 * Orden de cada frame (update):
 *   1. entity.update(dt, world) de cada entidad viva
 *   2. física: gravedad + movimiento + baldosas (solo entidades con collidesWithTiles / gravityScale)
 *   3. entity.onCollide(other) por cada par solapado
 *   4. onDeath() de las que murieron, se borran las alive=false y entran las nuevas (spawn)
 *   5. la cámara sigue al jugador; se revisa muerte del jugador / jefe derrotado
 * Lo que haces con spawn() en el frame N aparece (y empieza a actualizarse) en el frame N+1.
 * El jugador muerto NO se borra de la lista (para que P2 pueda dibujar su sprite de muerte).
 */
public class GameWorld {

    public enum State { PLAYING, PLAYER_DEAD, LEVEL_COMPLETE, GAME_COMPLETE }

    /** Segundos que se ve "HAS MUERTO" antes de reiniciar / "NIVEL COMPLETADO" antes de avanzar. */
    public static final double DEATH_DELAY = 1.5;
    public static final double COMPLETE_DELAY = 2.5;

    private final InputHandler input;
    private final Camera camera = new Camera();
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> pending = new ArrayList<>();

    private Level level;
    private int levelIndex;
    private Entity player;
    private State state = State.PLAYING;
    private double stateTime;
    private boolean bossDefeated;

    public GameWorld(InputHandler input) {
        this.input = input;
    }

    // ================= contrato =================

    public Entity getPlayer() { return player; }

    public List<Entity> getEnemies() {
        List<Entity> out = new ArrayList<>();
        for (Entity e : entities) {
            if (e.isAlive() && e.getTeam() == Team.ENEMY) out.add(e);
        }
        return out;
    }

    public Level getLevel() { return level; }

    public void spawn(Entity e) {
        pending.add(e);
    }

    public void bossDefeated() {
        bossDefeated = true;
    }

    // ================= extras útiles =================

    public InputHandler getInput() { return input; }
    public Camera getCamera() { return camera; }
    public int getLevelIndex() { return levelIndex; }
    public State getState() { return state; }
    public double getStateTime() { return stateTime; }

    /** Todas las entidades (solo lectura): para explosiones con radio, aceite, etc. */
    public List<Entity> getEntities() { return Collections.unmodifiableList(entities); }

    public List<KingPapa> getKingPapas() {
        List<KingPapa> out = new ArrayList<>();
        for (Entity e : entities) {
            if (e instanceof KingPapa k) out.add(k);
        }
        return out;
    }

    // ================= niveles =================

    /** Carga el nivel {@code index} (0..3) desde cero: entidades nuevas, cámara al inicio. */
    public void loadLevel(int index) {
        levelIndex = index;
        EntityRegistry.setCurrentLevelIndex(index);
        level = LevelLoader.loadIndex(index);
        entities.clear();
        pending.clear();
        player = null;
        bossDefeated = false;
        state = State.PLAYING;
        stateTime = 0;

        for (Entity e : LevelLoader.createEntities(level)) {
            entities.add(e);
            if (player == null && e.getTeam() == Team.PLAYER) player = e;
        }
        if (player == null) {                    // el mapa no tiene 'P': nace al inicio
            player = new DebugPlayer();
            player.setPosition(2 * Config.TILE, level.getHeightPx() - 4 * Config.TILE);
            entities.add(player);
        }
        camera.reset(level);
        camera.follow(player, level);
    }

    /** Reinicia el nivel actual (muerte del jugador o botón REINICIAR de la pausa). */
    public void restartLevel() {
        loadLevel(levelIndex);
    }

    // ================= frame =================

    public void update(double dt) {
        stateTime += dt;
        switch (state) {
            case PLAYING -> updatePlaying(dt);
            case PLAYER_DEAD -> {
                if (stateTime >= DEATH_DELAY) restartLevel();
            }
            case LEVEL_COMPLETE -> {
                if (stateTime >= COMPLETE_DELAY) {
                    if (levelIndex + 1 < LevelLoader.levelCount()) loadLevel(levelIndex + 1);
                    else setState(State.GAME_COMPLETE);
                }
            }
            case GAME_COMPLETE -> { /* el GameController decide qué hacer */ }
        }
    }

    private void updatePlaying(double dt) {
        // 1. update
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            if (!e.isAlive()) continue;
            e.tickTimers(dt);
            e.update(dt, this);
        }

        // 2. física
        for (Entity e : entities) {
            if (!e.isAlive()) continue;
            if (e.collidesWithTiles() || e.getGravityScale() != 0 || e.getVx() != 0 || e.getVy() != 0) {
                Collision.step(e, dt, level);
            }
            if (e.getY() > level.getHeightPx() + 300) e.kill();   // cayó a un hoyo
        }
        lockPlayerInsideCamera();

        // 3. choques entre entidades
        for (int i = 0; i < entities.size(); i++) {
            Entity a = entities.get(i);
            if (!a.isAlive()) continue;
            for (int j = i + 1; j < entities.size(); j++) {
                Entity b = entities.get(j);
                if (!a.isAlive()) break;
                if (!b.isAlive()) continue;
                if (a.getBounds().intersects(b.getBounds())) {
                    a.onCollide(b, this);
                    if (a.isAlive() && b.isAlive()) b.onCollide(a, this);
                }
            }
        }

        // 4. muertes, borrar, nuevas
        for (int i = 0; i < entities.size(); i++) entities.get(i).notifyDeath(this);
        entities.removeIf(e -> !e.isAlive() && e != player);
        flushPending();

        // 5. cámara y estado
        camera.follow(player, level);
        if (bossDefeated) setState(State.LEVEL_COMPLETE);
        else if (!player.isAlive()) setState(State.PLAYER_DEAD);
    }

    private void flushPending() {
        // onDeath() puede generar más spawns: vaciar en bucle
        while (!pending.isEmpty()) {
            List<Entity> batch = new ArrayList<>(pending);
            pending.clear();
            entities.addAll(batch);
        }
    }

    private void lockPlayerInsideCamera() {
        if (camera.isOneWay() && player.isAlive() && player.getX() < camera.getX()) {
            player.setPosition(camera.getX(), player.getY());
            if (player.getVx() < 0) player.setVelocity(0, player.getVy());
        }
    }

    private void setState(State s) {
        state = s;
        stateTime = 0;
    }

    public void render(GraphicsContext gc) {
        gc.clearRect(0, 0, Config.WIDTH, Config.HEIGHT);
        level.render(gc, camera);
        for (Entity e : entities) e.render(gc, camera);
    }
}