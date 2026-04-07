package com.project.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.project.assets.PlaceholderGenerator;
import com.project.utils.Constants;

/**
 * An AI-controlled enemy that chases the player and deals contact damage.
 *
 * <p>Visually represented by a red labelled placeholder box
 * ({@code "ENEMY_SPRITE"}).  Replace the
 * {@link PlaceholderGenerator#createPlaceholderNode} call with real art when
 * assets become available.
 *
 * <p>Movement is driven by {@link com.project.systems.AISystem};
 * damage is applied by {@link com.project.systems.CombatSystem}.
 */
public class Enemy extends GameObject {

    private float health;
    private final float maxHealth;
    private final float speed;
    private float attackCooldown;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Enemy(AssetManager assetManager, float x, float z, float speed) {
        super(x, z, Constants.ENEMY_SIZE);
        this.maxHealth     = Constants.ENEMY_MAX_HEALTH;
        this.health        = maxHealth;
        this.speed         = speed;
        this.attackCooldown = 0f;

        // Placeholder: red box labelled "ENEMY_SPRITE"
        // TODO: replace with assetManager.loadModel("Models/Enemy/enemy.j3o")
        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager,
                "ENEMY_SPRITE",
                Constants.ENEMY_SIZE,
                Constants.ENEMY_SIZE,
                new ColorRGBA(1.0f, 0.2f, 0.2f, 1.0f) // bright red
        ));
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    @Override
    public void update(float tpf) {
        if (attackCooldown > 0f) {
            attackCooldown -= tpf;
        }
    }

    // ------------------------------------------------------------------
    // Movement (called by AISystem)
    // ------------------------------------------------------------------
    /**
     * Moves the enemy by the supplied direction vector (expected to be
     * normalised) scaled by this enemy's speed and delta-time.
     *
     * @param dx  X-axis direction component
     * @param dz  Z-axis direction component
     * @param tpf seconds elapsed since last frame
     */
    public void moveToward(float dx, float dz, float tpf) {
        float newX = position.x + dx * speed * tpf;
        float newZ = position.z + dz * speed * tpf;

        // Clamp to level bounds so enemies never escape the arena
        float hw = Constants.LEVEL_HALF_WIDTH  - Constants.ENEMY_SIZE;
        float hh = Constants.LEVEL_HALF_HEIGHT - Constants.ENEMY_SIZE;
        newX = Math.max(-hw, Math.min(hw, newX));
        newZ = Math.max(-hh, Math.min(hh, newZ));

        setPosition(newX, newZ);
    }

    // ------------------------------------------------------------------
    // Combat
    // ------------------------------------------------------------------
    /**
     * Reduces health by {@code damage}.  Marks the enemy as inactive when
     * health reaches zero (signals the game loop to remove it).
     *
     * @param damage amount of damage received
     */
    public void takeDamage(float damage) {
        health = Math.max(0f, health - damage);
        if (health <= 0f) {
            active = false;
        }
    }

    /** @return {@code true} if this enemy's attack cooldown has expired. */
    public boolean canAttack() {
        return attackCooldown <= 0f;
    }

    /** Resets the attack cooldown after dealing damage to the player. */
    public void resetAttackCooldown() {
        attackCooldown = Constants.ENEMY_ATTACK_COOLDOWN;
    }

    /** @return {@code true} when health has reached zero. */
    public boolean isDead() {
        return health <= 0f;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float getHealth()    { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getSpeed()     { return speed; }
}
