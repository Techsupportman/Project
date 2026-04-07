package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Player;
import com.project.utils.Constants;

import java.util.List;

/**
 * Handles contact damage from enemies to the player.
 *
 * <p>Player now uses a <em>ranged</em> weapon (projectiles handled by
 * {@link ProjectileSystem}), so there is no longer a melee player-attack
 * method here.  This class only manages enemy-to-player contact damage.
 *
 * <p>Each enemy checks whether it is within contact range of the player and,
 * if its cooldown allows, deals 1 heart of damage.
 */
public class CombatSystem {

    /**
     * Checks whether {@code enemy} is in contact range and applies 1-heart
     * damage to {@code player} if the enemy's cooldown has expired.
     *
     * @param enemy  the attacking enemy
     * @param player the player who may receive damage
     */
    public void enemyContactDamage(Enemy enemy, Player player) {
        if (!enemy.isActive() || !enemy.canAttack()) return;

        float dx   = enemy.getPosition().x - player.getPosition().x;
        float dz   = enemy.getPosition().z - player.getPosition().z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist <= Constants.ENEMY_CONTACT_RANGE) {
            player.takeDamage();            // 1 heart
            enemy.resetAttackCooldown();
        }
    }

    /**
     * Legacy overload kept for backward compatibility.
     *
     * @param enemy  attacking enemy
     * @param player player
     * @param tpf    ignored
     */
    public void enemyContactDamage(Enemy enemy, Player player, float tpf) {
        enemyContactDamage(enemy, player);
    }

    /**
     * Legacy melee player-attack method — now a no-op since the player uses
     * projectiles.  Kept so any callers that haven't been updated yet don't
     * cause a compile error.
     *
     * @param player  attacker (unused)
     * @param enemies target list (unused)
     */
    public void playerAttack(Player player, List<Enemy> enemies) {
        // No-op: player attacks via projectiles (ProjectileSystem)
    }
}
