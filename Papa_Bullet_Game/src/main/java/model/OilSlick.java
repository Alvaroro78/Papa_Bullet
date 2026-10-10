package model;


/** Segundo desbloqueo: lanza aceite caliente que cae y queda en el piso quemando a los enemigos. */
public class OilSlick implements Weapon {
    private static final double SPEED = 260, LIFT = 120;


    @Override
    public void fire(double x, double y, AimDirection dir, GameWorld world) {
        Sounds.play("oilstilsound.wav");
        world.spawn(new OilPuddle(x, y, AimVector.dx(dir) * SPEED, AimVector.dy(dir) * SPEED - LIFT));
    }


    @Override public double getCooldown() { return 1.4; } // el charco dura 5 s quemando
    @Override public double getHeat() { return 0.3; }      // el arma más fuerte es la que más calienta
    @Override public String getName() { return "Oil Slick"; }
    @Override public String getIcon() { return "CharcoAceite.png"; }
}


