package model;


import static model.Config.TILE;


/**
 * Colisiones AABB entidad–baldosa. El motor llama {@link #step} para toda entidad con
 * física; los demás módulos normalmente no la usan directamente.
 *
 * Reglas:
 *  '#'  bloquea a TODOS desde todos los lados.
 *  '='  plataforma: a quien camina solo lo frena al CAER sobre ella desde arriba (se atraviesa
 *       saltando desde abajo y se baja con entity.dropThroughPlatform()).
 *       Las BALAS (entity.isProjectile()) la atraviesan siempre, en cualquier dirección.
 *  '-'  plataforma blindada: para quien camina es igual que '='; para las BALAS es como '#'
 *       (las frena desde todos los lados). Sirve para dar cobertura donde el nivel lo pida.
 * El movimiento se hace por ejes (primero X, luego Y) y en pasos de máx. 8 px para que
 * una bala rápida nunca atraviese una pared.
 */
public final class Collision {
    private static final double EPS = 1e-4;
    private static final double MAX_STEP = 8;


    private Collision() {}


    public static void step(Entity e, double dt, Level lv) {
        double vx = e.getVx(), vy = e.getVy();
        if (e.getGravityScale() != 0) {
            vy = Math.min(vy + Config.GRAVITY * e.getGravityScale() * dt, Config.MAX_FALL_SPEED);
        }
        double x = e.getX(), y = e.getY();
        double w = e.getWidth(), h = e.getHeight();


        if (!e.collidesWithTiles()) {            // vuela libre (helicóptero, efectos)
            e.setPosition(x + vx * dt, y + vy * dt);
            e.setVelocity(vx, vy);
            e.setContacts(false, false, false);
            return;
        }


        boolean wall = false, ground = false, ceiling = false;
        boolean bullet = e.isProjectile();


        // ---------- eje X ----------
        double rem = vx * dt;
        while (Math.abs(rem) > EPS) {
            double s = Math.abs(rem) > MAX_STEP ? Math.copySign(MAX_STEP, rem) : rem;
            rem -= s;
            double nx = x + s;
            if (blockedX(lv, nx, y, w, h, s > 0, bullet)) {
                nx = s > 0 ? Math.floor((nx + w - EPS) / TILE) * TILE - w
                        : (Math.floor(nx / TILE) + 1) * TILE;
                x = nx;
                wall = true;
                vx = 0;
                break;
            }
            x = nx;
        }


        // ---------- eje Y ----------
        rem = vy * dt;
        boolean dropping = e.isDroppingThrough();
        while (Math.abs(rem) > EPS) {
            double s = Math.abs(rem) > MAX_STEP ? Math.copySign(MAX_STEP, rem) : rem;
            rem -= s;
            double ny = y + s;
            if (s > 0) {                                           // bajando
                int row = (int) Math.floor((ny + h - EPS) / TILE);
                if (landsOn(lv, x, w, row, y + h, dropping, bullet)) {
                    y = row * TILE - h;
                    ground = true;
                    vy = 0;
                    break;
                }
            } else {                                               // subiendo
                int row = (int) Math.floor(ny / TILE);
                if (hitsCeiling(lv, x, w, row, bullet)) {
                    y = (row + 1) * TILE;
                    ceiling = true;
                    vy = 0;
                    break;
                }
            }
            y = ny;
        }


        e.setPosition(x, y);
        e.setVelocity(vx, vy);
        e.setContacts(ground, wall, ceiling);
    }


    /** ¿Esta baldosa frena a esta entidad desde cualquier lado? */
    private static boolean solidFor(Level lv, int col, int row, boolean bullet) {
        return lv.isSolid(col, row) || (bullet && lv.isArmored(col, row));
    }


    private static boolean blockedX(Level lv, double nx, double y, double w, double h, boolean movingRight, boolean bullet) {
        int col = (int) Math.floor((movingRight ? nx + w - EPS : nx) / TILE);
        int r0 = (int) Math.floor(y / TILE);
        int r1 = (int) Math.floor((y + h - EPS) / TILE);
        for (int r = r0; r <= r1; r++) {
            if (solidFor(lv, col, r, bullet)) return true;
        }
        return false;
    }


    private static boolean landsOn(Level lv, double x, double w, int row, double prevBottom, boolean dropping, boolean bullet) {
        int c0 = (int) Math.floor(x / TILE);
        int c1 = (int) Math.floor((x + w - EPS) / TILE);
        for (int c = c0; c <= c1; c++) {
            if (solidFor(lv, c, row, bullet)) return true;
            // plataforma: solo si veníamos desde arriba de ella y no nos estamos dejando caer.
            // Las balas nunca se paran sobre una plataforma: la atraviesan.
            if (!bullet && !dropping && lv.isPlatform(c, row) && prevBottom <= row * TILE + 0.5) return true;
        }
        return false;
    }


    private static boolean hitsCeiling(Level lv, double x, double w, int row, boolean bullet) {
        int c0 = (int) Math.floor(x / TILE);
        int c1 = (int) Math.floor((x + w - EPS) / TILE);
        for (int c = c0; c <= c1; c++) {
            if (solidFor(lv, c, row, bullet)) return true;
        }
        return false;
    }
}


