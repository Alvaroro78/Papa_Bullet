package model;

/** Segundo desbloqueo: lanza aceite caliente que cae y queda en el piso quemando a los enemigos. */
public class OilSlick implements Weapon {
    private static final double SPEED = 260, LIFT = 120;

    @Override
    public void fire(double x, double y, AimDirection dir, GameWorld world) {
        world.spawn(new OilPuddle(x, y, AimVector.dx(dir) * SPEED, AimVector.dy(dir) * SPEED - LIFT));
    }

    @Override public String getName() { return "Oil Slick"; }
    @Override public String getIcon() { return "CharcoAceite.png"; }
}