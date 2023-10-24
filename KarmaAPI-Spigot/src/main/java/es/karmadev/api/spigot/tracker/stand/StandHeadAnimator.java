package es.karmadev.api.spigot.tracker.stand;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

/**
 * Stand head animation
 * @deprecated time-based rotation should be avoided, see
 * {@link StandAnimator}
 */
@Deprecated
public final class StandHeadAnimator {

    private final ArmorStand stand;
    private final long startTime;
    private final double animationTime;

    private final EulerAngle startAngle;
    private final EulerAngle targetAngle;
    private boolean lastFrame = false;

    public StandHeadAnimator(final ArmorStand stand, final EulerAngle targetAngle, final double animationTime) {
        this.stand = stand;
        this.startAngle = stand.getHeadPose();
        this.targetAngle = targetAngle;
        this.startTime = System.currentTimeMillis();
        this.animationTime = animationTime;
    }

    public void animateFrame() {
        long currentTime = System.currentTimeMillis();
        double elapsed = (currentTime - startTime) / 1000d;

        if (elapsed > animationTime) {
            stand.setHeadPose(targetAngle);
            lastFrame = true;
        } else {
            elapsed = elapsed / animationTime;

            EulerAngle intermediateAngle = slerp(startAngle, targetAngle, elapsed);
            stand.setHeadPose(intermediateAngle);
        }
    }

    public boolean hasFinished() {
        return lastFrame;
    }

    private EulerAngle slerp(final EulerAngle start, final EulerAngle end, double t) {
        double dot = start.getX() * end.getX() + start.getY() * end.getY() + start.getZ() * end.getZ();
        dot = Math.max(-1.0, Math.min(1.0, dot));

        double theta = Math.acos(dot);
        double sinTheta = Math.sin(theta);

        if (sinTheta < 1e-6) {
            // If sin(theta) is close to 0, use linear interpolation
            EulerAngle multiplied = multiply(start, 1 - t);
            return add(multiplied, multiply(end, t));
            //return start.multiply(1 - t).add(end.multiply(t));
        }

        double w1 = Math.sin((1 - t) * theta) / sinTheta;
        double w2 = Math.sin(t * theta) / sinTheta;

        EulerAngle multiplied = multiply(start, w1);
        return add(multiplied, multiply(end, w2));
    }

    private EulerAngle multiply(final EulerAngle angle, final double value) {
        double x = angle.getX();
        double y = angle.getY();
        double z = angle.getZ();

        return new EulerAngle(x * value, y * value, z * value);
    }

    private EulerAngle add(final EulerAngle source, final EulerAngle angle) {
        return source.add(angle.getX(), angle.getY(), angle.getZ());
    }
}
