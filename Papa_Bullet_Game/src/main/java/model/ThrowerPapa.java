package model;

/** Arma por defecto: tira papas bomba en parábola. */
public class ThrowerPapa implements Weapon {
    private static final double SPEED = 460, LIFT = 260; // curva baja: pasa a la altura del pecho de un policía y llega a ~5 baldosas

    @Override
    public void fire(double x, double y, AimDirection dir, GameWorld world) {
        world.spawn(new PapaBomba(x, y, AimVector.dx(dir) * SPEED, AimVector.dy(dir) * SPEED - LIFT));
    }

    @Override public double getCooldown() { return 0.6; }
    @Override public String getName() { return "Thrower Papa"; }
    @Override public String getIcon() { return "PapaBomba.png"; }
}