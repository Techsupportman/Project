package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Projectile;

import java.util.Iterator;
import java.util.List;

/**
 * Manages all active {@link Projectile} instances each frame.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Advance each projectile's position ({@link Projectile#update(float)}).</li>
 *   <li>Remove projectiles that have gone inactive (out of range).</li>
 *   <li>Test each projectile against each enemy for collision.</li>
 *   <li>Apply damage; if the projectile has ricochet remaining it bounces off
 *       the hit enemy toward the nearest other active enemy instead of
 *       deactivating.</li>
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

                    if (proj.getRicochetRemaining() > 0) {
                        // Bounce toward the nearest OTHER active enemy; if none,
                        // reflect the incoming direction.
                        Enemy target = nearestOtherEnemy(proj, enemy, enemies);
                        float bx, bz;
                        if (target != null) {
                            float tdx = target.getPosition().x - proj.getPosition().x;
                            float tdz = target.getPosition().z - proj.getPosition().z;
                            float tlen = (float) Math.sqrt(tdx * tdx + tdz * tdz);
                            if (tlen < 0.001f) tlen = 0.001f;
                            bx = tdx / tlen;
                            bz = tdz / tlen;
                        } else {
                            // Reflect by inverting the direction relative to the
                            // surface normal (approximated as the hit vector).
                            float nx = dist > 0.001f ? -dx / dist : -proj.getDirX();
                            float nz = dist > 0.001f ? -dz / dist : -proj.getDirZ();
                            float dot = proj.getDirX() * nx + proj.getDirZ() * nz;
                            bx = proj.getDirX() - 2f * dot * nx;
                            bz = proj.getDirZ() - 2f * dot * nz;
                            float blen = (float) Math.sqrt(bx * bx + bz * bz);
                            if (blen < 0.001f) { bx = -proj.getDirX(); bz = -proj.getDirZ(); }
                            else { bx /= blen; bz /= blen; }
                        }
                        proj.applyRicochet(bx, bz);
                        break; // processed this hit; continue in new direction
                    }

                    if (!proj.isActive()) break; // pierce exhausted
                }
            }

            if (!proj.isActive()) {
                pit.remove();
            }
        }
        return totalDamage;
    }

    /** Returns the nearest active enemy to {@code proj} that is not {@code skip}, or null. */
    private Enemy nearestOtherEnemy(Projectile proj, Enemy skip, List<Enemy> enemies) {
        Enemy best = null;
        float bestDist = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e == skip || !e.isActive()) continue;
            float ex = proj.getPosition().x - e.getPosition().x;
            float ez = proj.getPosition().z - e.getPosition().z;
            float d  = ex * ex + ez * ez;
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}
