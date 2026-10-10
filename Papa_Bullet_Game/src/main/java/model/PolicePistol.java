package model;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


/**
 * 'G' — policía con pistola. NUNCA camina ni se baja de su plataforma: se planta y le dispara al jugador.
 *
 * Apunta con las mismas 8 direcciones que el jugador (AimDirection), escogiendo la más cercana a donde
 * está el jugador. La diferencia con el jugador: el policía SÍ puede disparar en diagonal hacia abajo
 * estando quieto (el jugador tiene que caminar para eso). Igual que el jugador en el piso, NO puede
 * disparar recto hacia abajo: si el jugador está justo debajo de él, baja el arma y espera.
 *
 * Cuando cambia de dirección tarda AIM_TIME en volver a disparar (se le ve girar el brazo: es la
 * "advertencia" para que el jugador alcance a esquivar). Sus balas atraviesan las plataformas '='.
 */
public class PolicePistol extends Enemy {
    private static final double FIRE_RATE = 1.6, RANGE = 520, BULLET_SPEED = 320;
    private static final double AIM_TIME = 0.3, ARM = 22, SHOULDER_Y = 18, FLASH_TIME = 0.08;


    private AimDirection aim = AimDirection.LEFT;
    private boolean canAim = true;   // false = el jugador está justo debajo (no puede apuntarle)
    private double aimTimer, flash;


    public PolicePistol() {
        super(30, 52, 60, 30, false);
        cooldown = 0.8; // no dispara apenas aparece (el cooldown solo corre en pantalla)
    }


    @Override
    protected void act(double dt, GameWorld world, Entity player) {
        vx = 0;                                   // se queda quieto en su sitio, siempre
        flash = Math.max(0, flash - dt);
        aimTimer = Math.max(0, aimTimer - dt);


        double sx = getCenterX(), sy = y + SHOULDER_Y;
        AimDirection want = snapAim(player.getCenterX() - sx, player.getCenterY() - sy, false);
        canAim = want != null;
        if (!canAim) return;                      // justo debajo: no hay dirección válida
        if (want != aim) {                        // giró el brazo: pequeña espera antes de disparar
            aim = want;
            aimTimer = AIM_TIME;
        }
        if (AimVector.sx(aim) != 0) facing = AimVector.sx(aim);


        if (cooldown == 0 && aimTimer == 0 && distX(player) < RANGE) {
            double mx = sx + AimVector.dx(aim) * ARM, my = sy + AimVector.dy(aim) * ARM;
            shootDir(world, aim, mx, my, BULLET_SPEED);
            cooldown = FIRE_RATE;
            flash = FLASH_TIME;
        }
    }


    AimDirection getAim() { return aim; }


    // ---------------- dibujo: el brazo y la pistola siguen la dirección de apuntado ----------------


    @Override
    public void render(GraphicsContext gc, Camera cam) {
        drawPolice(gc, cam);
        Color sleeve = Color.web("#2f5fb3"), gun = Color.web("#222222");
        int sy = canAim ? AimVector.sy(aim) : 1;
        boolean straightUp = canAim && aim == AimDirection.UP;
        double gx, gy;                                         // punta del arma (coordenadas locales)
        if (straightUp) {                                      // recto arriba
            part(gc, cam, 18, 4, 4, 14, sleeve);
            part(gc, cam, 17, -4, 6, 8, gun);
            gx = 20; gy = -6;
        } else if (sy < 0) {                                   // diagonal arriba
            part(gc, cam, 20, 14, 6, 4, sleeve);
            part(gc, cam, 24, 10, 6, 4, sleeve);
            part(gc, cam, 28, 4, 7, 7, gun);
            gx = 34; gy = 4;
        } else if (sy > 0) {                                   // diagonal abajo (o arma baja si no puede apuntar)
            part(gc, cam, 20, 20, 6, 4, sleeve);
            part(gc, cam, 24, 24, 6, 4, sleeve);
            part(gc, cam, 28, 27, 7, 7, gun);
            gx = 34; gy = 33;
        } else {                                               // al frente
            part(gc, cam, 20, 18, 12, 4, sleeve);
            part(gc, cam, 28, 15, 8, 5, gun);
            gx = 36; gy = 16;
        }
        if (flash > 0) part(gc, cam, gx - 2, gy - 2, 6, 6, Color.web("#ffe14a")); // fogonazo
    }
}


