package model;

/** Primer desbloqueo: una caja de papas a la francesa que a mitad del camino se abre en espinas. */
public class FrenchSpikes implements Weapon {
    private static final double SPEED = 520;

    @Override
    public void fire(double x, double y, AimDirection dir, GameWorld world) {
        world.spawn(new FrenchFry(x, y, AimVector.dx(dir) * SPEED, AimVector.dy(dir) * SPEED, false));
    }

    @Override public String getName() { return "French Spikes"; }
    @Override public String getIcon() { return "FrenchSpikes.png"; }
}