package es.karmadev.api.spigot.tracker.stand;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

/**
 * Stand animator
 */
public final class StandAnimator {

    private final ArmorStand stand;
    private EulerAngle current;
    @Getter @Setter
    private EulerAngle target;
    private final double speed;
    private final double frames;

    /**
     * Initialize the stand animator
     *
     * @param stand the stand that will be
     *              animated
     * @param target the target angle
     * @param speed the animation speed
     * @param frames the animation frames
     */
    public StandAnimator(final ArmorStand stand, final EulerAngle target, final double speed, final double frames) {
        this.stand = stand;
        this.current = stand.getHeadPose();
        this.target = target;
        this.speed = speed;
        this.frames = frames;
    }

    /**
     * Animate the current head
     * rotation
     */
    public void animate() {
        double maxRotation = speed / frames;
        EulerAngle delta = calculateDelta(current, target, maxRotation);

        current = current.add(delta.getX(), delta.getY(), delta.getZ());
        stand.setHeadPose(current);
    }

    /**
     * Get if the animation has finished
     *
     * @return if the animation is finished
     */
    public boolean finished() {
        return current.equals(target);
    }

    /**
     * Calculate the next angle in where
     * to face
     *
     * @param current the current angle
     * @param target the next angle
     * @param maxRotation the maximum rotation
     * @return the next angle
     */
    private EulerAngle calculateDelta(final EulerAngle current, final EulerAngle target, final double maxRotation) {
        EulerAngle difference = target.subtract(current.getX(), current.getY(), current.getZ());

        double x = Math.max(-maxRotation, Math.min(maxRotation, difference.getX()));
        double y = Math.max(-maxRotation, Math.min(maxRotation, difference.getY()));
        double z = Math.max(-maxRotation, Math.min(maxRotation, difference.getZ()));

        return new EulerAngle(x, y, z);
    }
}
