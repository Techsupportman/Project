package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Player;
import com.project.utils.Constants;

/**
 * Calculates which enemies are visible to the player each frame, based on
 * the currently selected vision mode.
 *
 * <h3>Vision Modes</h3>
 * <ul>
 *   <li><b>Circle Vision</b> — reveals all enemies within
 *       {@link Constants#VISION_CIRCLE_RADIUS} world-units of the player,
 *       in all directions.</li>
 *   <li><b>Cone Vision</b> — reveals enemies in a directional cone extending
 *       {@link Constants#VISION_CONE_RANGE} world-units ahead of the player's
 *       aim direction, within a half-angle of
 *       {@link Constants#VISION_CONE_HALF_ANGLE} radians.  Additionally, a
 *       small circle ({@link Constants#VISION_CONE_NEAR_RADIUS}) always
 *       reveals nearby enemies regardless of cone direction.</li>
 * </ul>
 *
 * <p>Enemy attacks are always visible (handled by the enemy entity itself).
 * EXP pickups share the same visibility rules as enemies — invisible in
 * darkness, visible inside the player's vision zone.
 */
public class VisionSystem {

    /**
     * Determines whether a single enemy is visible in the current vision
     * mode and updates its {@link Enemy#setVisibleToPlayer(boolean)} flag.
     *
     * @param enemy   the enemy to evaluate
     * @param player  the player whose vision is used
     */
    public void updateVisibility(Enemy enemy, Player player) {
        boolean visible = isVisible(
                enemy.getPosition().x, enemy.getPosition().z,
                player.getPosition().x, player.getPosition().z,
                player.getAimDirX(), player.getAimDirZ(),
                player.isCircleVision()
        );
        // Bosses are always shown so the player is never caught off-guard
        if (enemy.getEnemyType().isAnyBoss()) visible = true;
        enemy.setVisibleToPlayer(visible);
    }

    /**
     * Checks whether a world-space point is inside the player's current
     * vision area.  This method is also used by the pickup system to
     * determine whether EXP orbs should be rendered.
     *
     * @param targetX      point to test (X)
     * @param targetZ      point to test (Z)
     * @param playerX      player position (X)
     * @param playerZ      player position (Z)
     * @param aimDirX      normalised aim direction (X)
     * @param aimDirZ      normalised aim direction (Z)
     * @param circleMode   {@code true} for circle vision, {@code false} for cone
     * @return {@code true} if the point is visible
     */
    public boolean isVisible(float targetX, float targetZ,
                              float playerX, float playerZ,
                              float aimDirX, float aimDirZ,
                              boolean circleMode) {
        float dx   = targetX - playerX;
        float dz   = targetZ - playerZ;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (circleMode) {
            return dist <= Constants.VISION_CIRCLE_RADIUS;
        } else {
            // Near circle always visible
            if (dist <= Constants.VISION_CONE_NEAR_RADIUS) return true;
            if (dist > Constants.VISION_CONE_RANGE) return false;

            // Dot product to check angular overlap with aim direction
            float dot = (dx / dist) * aimDirX + (dz / dist) * aimDirZ;
            return dot >= Math.cos(Constants.VISION_CONE_HALF_ANGLE);
        }
    }
}
