package com.project.systems;

import com.project.entities.Enemy;
import com.project.entities.Player;
import com.project.utils.Constants;

import java.util.List;

/**
 * Handles all damage interactions between game entities.
 *
 * <h3>Player attack</h3>
 * When the player presses SPACE, {@link #playerAttack} is called.  It damages
 * every enemy within {@link Constants#PLAYER_ATTACK_RANGE} of the player.
 *
 * <h3>Enemy contact damage</h3>
 * {@link #enemyContactDamage} is called each frame for every living enemy.
 * When an enemy is close enough to the player and its own cooldown has expired
 * it deals {@link Constants#ENEMY_ATTACK_DAMAGE} to the player.
 */
public class CombatSystem {

    /**
     * Executes the player's area-of-effect melee attack.
     *
     * @param player  the attacking player
     * @param enemies all active enemies in the scene
     */
    public void playerAttack(Player player, List<Enemy> enemies) {
        float px = player.getPosition().x;
        float pz = player.getPosition().z;

        for (Enemy enemy : enemies) {
            if (!enemy.isActive()) continue;

            float dx   = px - enemy.getPosition().x;
            float dz   = pz - enemy.getPosition().z;
            float dist = (float) Math.sqrt(dx * dx + dz * dz);

            if (dist <= Constants.PLAYER_ATTACK_RANGE) {
                enemy.takeDamage(Constants.PLAYER_ATTACK_DAMAGE);
            }
        }
    }

    /**
     * Checks whether an enemy is in contact range and applies damage to the
     * player if the enemy's cooldown allows it.
     *
     * @param enemy  the attacking enemy
     * @param player the player who may receive damage
     * @param tpf    seconds elapsed since last frame (unused here but kept for
     *               future use with interpolated damage)
     */
    public void enemyContactDamage(Enemy enemy, Player player, float tpf) {
        if (!enemy.isActive() || !enemy.canAttack()) return;

        float dx   = enemy.getPosition().x - player.getPosition().x;
        float dz   = enemy.getPosition().z - player.getPosition().z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist <= Constants.ENEMY_CONTACT_RANGE) {
            player.takeDamage(Constants.ENEMY_ATTACK_DAMAGE);
            enemy.resetAttackCooldown();
        }
    }
}
