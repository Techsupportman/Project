package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Player;
import com.project.utils.Vector2D;

/**
 * Drives enemy movement every frame.
 *
 * <p>For the MVP, each enemy uses simple <em>direct pursuit</em>: it moves
 * straight toward the player's current position.  No obstacle avoidance is
 * performed, but enemies are separated slightly if they overlap each other
 * to prevent pile-ups.
 *
 * <p>A more sophisticated implementation (e.g. A* path-finding or steering
 * behaviours) can replace the internals of {@link #update} without changing
 * any calling code.
 */
public class AISystem {

    /** Minimum separation between two enemies before separation force kicks in. */
    private static final float SEPARATION_RADIUS = 0.9f;
    /** Strength of the lateral push applied when enemies are too close. */
    private static final float SEPARATION_STRENGTH = 0.4f;

    /**
     * Advances a single enemy toward the player for one frame.
     *
     * @param enemy  the enemy to update
     * @param player the player to pursue
     * @param tpf    seconds elapsed since last frame
     */
    public void update(Enemy enemy, Player player, float tpf) {
        if (!enemy.isActive()) return;

        Vector2D ePos = enemy.getPosition();
        Vector2D pPos = player.getPosition();

        float dx = pPos.x - ePos.x;
        float dz = pPos.z - ePos.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.01f) return; // already on top of the player

        // Normalise and move
        enemy.moveToward(dx / dist, dz / dist, tpf);
    }

    /**
     * Applies a simple separation force between two enemies that are
     * overlapping, pushing them apart.  Call this for every pair of active
     * enemies each frame.
     *
     * @param a   first enemy
     * @param b   second enemy
     * @param tpf seconds elapsed since last frame
     */
    public void separate(Enemy a, Enemy b, float tpf) {
        if (!a.isActive() || !b.isActive()) return;

        float dx   = a.getPosition().x - b.getPosition().x;
        float dz   = a.getPosition().z - b.getPosition().z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist > SEPARATION_RADIUS || dist < 0.001f) return;

        // Push each enemy away from the other
        float nx   = dx / dist;
        float nz   = dz / dist;
        float push = SEPARATION_STRENGTH * tpf;

        a.setPosition(a.getPosition().x + nx * push, a.getPosition().z + nz * push);
        b.setPosition(b.getPosition().x - nx * push, b.getPosition().z - nz * push);
    }
}
