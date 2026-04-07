package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Projectile;
import com.project.utils.Constants;

import java.util.Iterator;
import java.util.List;

/**
 * Manages all active {@link Projectile} instances each frame.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Advance each projectile's position ({@link Projectile#update(float)}).</li>
 *   <li>Remove projectiles that have gone inactive (out of range or hit wall).</li>
 *   <li>Test each projectile against each enemy for collision.</li>
 *   <li>Apply damage and remove projectiles that have exhausted their pierce count.</li>
 * </ol>
 */
public class ProjectileSystem {

    /**
     * Updates all projectiles and resolves their collisions with enemies.
     *
     * @param projectiles live projectile list (modified in-place)
     * @param enemies     active enemy list
     * @param tpf         seconds since last frame
     * @return            total damage dealt this frame (for stat tracking)
     */
    public float update(List<Projectile> projectiles, List<Enemy> enemies, float tpf) {
        float totalDamage = 0f;

        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile proj = pit.next();
            if (!proj.isActive()) {
                pit.remove();
                continue;
            }

            proj.update(tpf);

            if (!proj.isActive()) {
                pit.remove();
                continue;
            }

            // Collision with enemies
            for (Enemy enemy : enemies) {
                if (!enemy.isActive()) continue;

                float dx   = proj.getPosition().x - enemy.getPosition().x;
                float dz   = proj.getPosition().z - enemy.getPosition().z;
                float dist = (float) Math.sqrt(dx * dx + dz * dz);
                float hitThreshold = proj.getSize() + enemy.getSize();

                if (dist <= hitThreshold) {
                    float dmg = proj.onHit();
                    enemy.takeDamage(dmg);
                    totalDamage += dmg;
                    if (!proj.isActive()) break; // pierce exhausted
                }
            }

            if (!proj.isActive()) {
                pit.remove();
            }
        }
        return totalDamage;
    }
}
