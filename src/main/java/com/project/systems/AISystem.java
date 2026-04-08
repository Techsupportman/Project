package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.EnemyType;
import com.project.entities.Player;
import com.project.utils.Constants;
import com.project.utils.Vector2D;

/**
 * Drives enemy movement every frame.
 *
 * <p>Melee enemies use simple <em>direct pursuit</em>; they move straight
 * toward the player.  No obstacle avoidance is performed beyond the light
 * separation force applied when two enemies overlap.
 *
 * <p>Ranged enemies ({@link EnemyType#SHOOTER} and {@link EnemyType#ARTILLERY})
 * keep their preferred distance from the player and fire projectiles when
 * their cooldown expires.  Call {@link #tryRangedShot} each frame to obtain
 * any pending shot parameters.
 *
 * <p>A more sophisticated implementation (e.g. A* path-finding or steering
 * behaviours) can replace the internals of {@link #update} without changing
 * any calling code.
 */
public class AISystem {

    // ------------------------------------------------------------------
    // Separation
    // ------------------------------------------------------------------
    /** Minimum separation between two enemies before separation force kicks in. */
    private static final float SEPARATION_RADIUS   = 0.9f;
    /** Strength of the lateral push applied when enemies are too close. */
    private static final float SEPARATION_STRENGTH = 0.4f;

    // ------------------------------------------------------------------
    // Ranged enemy shot data
    // ------------------------------------------------------------------
    /**
     * Immutable description of a ranged-enemy shot request.  The GameApp
     * converts these into {@link com.project.entities.Projectile} instances.
     */
    public static final class EnemyShot {
        public final float x, z, dirX, dirZ, speed, damage, size;
        public EnemyShot(float x, float z, float dirX, float dirZ,
                         float speed, float damage, float size) {
            this.x = x; this.z = z;
            this.dirX = dirX; this.dirZ = dirZ;
            this.speed = speed; this.damage = damage; this.size = size;
        }
    }

    /**
     * Advances a single enemy toward the player for one frame.
     *
     * <p>Melee enemies always close the distance.  Ranged enemies slow their
     * approach once they are within their preferred fire range.
     *
     * @param enemy  the enemy to update
     * @param player the player to pursue
     * @param tpf    seconds elapsed since last frame
     */
    public void update(Enemy enemy, Player player, float tpf) {
        if (!enemy.isActive()) return;

        Vector2D ePos = enemy.getPosition();
        Vector2D pPos = player.getPosition();

        float dx   = pPos.x - ePos.x;
        float dz   = pPos.z - ePos.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.01f) return; // already on top of the player

        float nx = dx / dist;
        float nz = dz / dist;

        EnemyType type = enemy.getEnemyType();
        if (type == EnemyType.SHOOTER) {
            // Only approach if outside preferred range; hover in place once close enough
            if (dist > Constants.SHOOTER_FIRE_RANGE) {
                enemy.moveToward(nx, nz, tpf);
            }
        } else if (type == EnemyType.ARTILLERY) {
            if (dist > Constants.ARTILLERY_FIRE_RANGE) {
                enemy.moveToward(nx, nz, tpf);
            }
        } else {
            enemy.moveToward(nx, nz, tpf);
        }
    }

    /**
     * Checks whether a ranged enemy should fire this frame.
     *
     * @param enemy  the enemy to check
     * @param player the player to shoot at
     * @param tpf    seconds elapsed since last frame
     * @return an {@link EnemyShot} if the enemy fires, or {@code null} otherwise
     */
    public EnemyShot tryRangedShot(Enemy enemy, Player player, float tpf) {
        if (!enemy.isActive()) return null;

        EnemyType type = enemy.getEnemyType();
        float fireRange;
        float fireInterval;
        float bulletSpeed;
        float bulletDamage;
        float bulletSize;

        if (type == EnemyType.SHOOTER) {
            fireRange    = Constants.SHOOTER_FIRE_RANGE;
            fireInterval = Constants.SHOOTER_FIRE_INTERVAL;
            bulletSpeed  = Constants.SHOOTER_BULLET_SPEED;
            bulletDamage = Constants.SHOOTER_BULLET_DAMAGE;
            bulletSize   = Constants.SHOOTER_BULLET_SIZE;
        } else if (type == EnemyType.ARTILLERY) {
            fireRange    = Constants.ARTILLERY_FIRE_RANGE;
            fireInterval = Constants.ARTILLERY_FIRE_INTERVAL;
            bulletSpeed  = Constants.ARTILLERY_BULLET_SPEED;
            bulletDamage = Constants.ARTILLERY_BULLET_DAMAGE;
            bulletSize   = Constants.ARTILLERY_BULLET_SIZE;
        } else {
            return null; // non-ranged type
        }

        Vector2D ePos = enemy.getPosition();
        Vector2D pPos = player.getPosition();

        float dx   = pPos.x - ePos.x;
        float dz   = pPos.z - ePos.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        // Only shoot when within fire range and cooldown is ready
        if (dist > fireRange || !enemy.canRangedAttack()) return null;

        enemy.resetRangedCooldown(fireInterval);

        float nx = dist > 0.001f ? dx / dist : 1f;
        float nz = dist > 0.001f ? dz / dist : 0f;

        return new EnemyShot(ePos.x, ePos.z, nx, nz, bulletSpeed, bulletDamage, bulletSize);
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
