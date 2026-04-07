package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Player;
import com.project.utils.Constants;

import java.util.List;

/**
 * Handles movement constraints and simple AABB collision resolution.
 *
 * <p>For this MVP the player's movement bounds are already enforced in
 * {@link Player#move}, so this system's main job is:
 * <ol>
 *   <li>Enemy–enemy separation (delegated to {@link AISystem#separate})</li>
 *   <li>Enemy–player push-back so enemies cannot occupy the same cell as the
 *       player</li>
 * </ol>
 *
 * <p>A full physics engine (e.g. Bullet) can replace this class without
 * changing any higher-level game logic.
 */
public class PhysicsSystem {

    /**
     * Resolves overlap between a single enemy and the player by pushing the
     * enemy away if their AABBs intersect.
     *
     * @param enemy  enemy to test
     * @param player the player
     */
    public void resolveEnemyPlayerOverlap(Enemy enemy, Player player) {
        if (!enemy.isActive()) return;
        if (!enemy.collidesWith(player)) return;

        float dx   = enemy.getPosition().x - player.getPosition().x;
        float dz   = enemy.getPosition().z - player.getPosition().z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        float minSep = enemy.getSize() + player.getSize() + 0.05f;

        if (dist < 0.001f) {
            // Exactly on top of each other — push enemy in an arbitrary direction
            enemy.setPosition(enemy.getPosition().x + minSep, enemy.getPosition().z);
            return;
        }

        float overlap = minSep - dist;
        if (overlap > 0f) {
            float nx = dx / dist;
            float nz = dz / dist;
            enemy.setPosition(
                    enemy.getPosition().x + nx * overlap,
                    enemy.getPosition().z + nz * overlap
            );
        }
    }

    /**
     * Resolves overlap between two enemies by pushing them apart equally.
     *
     * @param a first enemy
     * @param b second enemy
     */
    public void resolveEnemyEnemyOverlap(Enemy a, Enemy b) {
        if (!a.isActive() || !b.isActive()) return;
        if (!a.collidesWith(b)) return;

        float dx   = a.getPosition().x - b.getPosition().x;
        float dz   = a.getPosition().z - b.getPosition().z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        float minSep = a.getSize() + b.getSize() + 0.02f;

        if (dist < 0.001f) {
            a.setPosition(a.getPosition().x + minSep * 0.5f, a.getPosition().z);
            b.setPosition(b.getPosition().x - minSep * 0.5f, b.getPosition().z);
            return;
        }

        float overlap = minSep - dist;
        if (overlap > 0f) {
            float nx = dx / dist;
            float nz = dz / dist;
            float half = overlap * 0.5f;
            a.setPosition(a.getPosition().x + nx * half, a.getPosition().z + nz * half);
            b.setPosition(b.getPosition().x - nx * half, b.getPosition().z - nz * half);
        }
    }

    /**
     * Convenience method: resolves enemy–player overlap and all pairwise
     * enemy–enemy overlaps for a full list of enemies.
     *
     * @param enemies all active enemies
     * @param player  the player
     */
    public void resolveAll(List<Enemy> enemies, Player player) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            resolveEnemyPlayerOverlap(e, player);

            for (int j = i + 1; j < enemies.size(); j++) {
                resolveEnemyEnemyOverlap(e, enemies.get(j));
            }
        }
    }
}
