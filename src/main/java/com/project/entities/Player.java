package com.project.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.project.assets.PlaceholderGenerator;
import com.project.utils.Constants;

/**
 * The player-controlled character.
 *
 * <p>Visually represented by a blue labelled placeholder box
 * ({@code "PLAYER_SPRITE"}).  Replace the
 * {@link PlaceholderGenerator#createPlaceholderNode} call with real sprite
 * or model loading when art assets are available.
 *
 * <h3>Controls (registered in {@link com.project.utils.InputHandler})</h3>
 * <ul>
 *   <li>WASD / Arrow keys — movement</li>
 *   <li>SPACE — melee attack (area-of-effect around the player)</li>
 * </ul>
 */
public class Player extends GameObject {

    private float health;
    private final float maxHealth;
    private float attackCooldown;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Player(AssetManager assetManager, float x, float z) {
        super(x, z, Constants.PLAYER_SIZE);
        this.maxHealth    = Constants.PLAYER_MAX_HEALTH;
        this.health       = maxHealth;
        this.attackCooldown = 0f;

        // Placeholder: blue box labelled "PLAYER_SPRITE"
        // TODO: replace with assetManager.loadModel("Models/Player/player.j3o")
        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager,
                "PLAYER_SPRITE",
                Constants.PLAYER_SIZE,
                Constants.PLAYER_SIZE,
                new ColorRGBA(0.25f, 0.55f, 1.0f, 1.0f) // bright blue
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
    // Movement
    // ------------------------------------------------------------------
    /**
     * Translates the player by the given directional vector (already
     * normalised for diagonals) scaled by speed and delta-time, then clamps
     * the result inside the level bounds.
     *
     * @param dx  X-axis direction (−1 to +1)
     * @param dz  Z-axis direction (−1 to +1)
     * @param tpf seconds elapsed since last frame
     */
    public void move(float dx, float dz, float tpf) {
        float newX = position.x + dx * Constants.PLAYER_SPEED * tpf;
        float newZ = position.z + dz * Constants.PLAYER_SPEED * tpf;

        // Clamp to level bounds
        float hw = Constants.LEVEL_HALF_WIDTH  - Constants.PLAYER_SIZE;
        float hh = Constants.LEVEL_HALF_HEIGHT - Constants.PLAYER_SIZE;
        newX = Math.max(-hw, Math.min(hw, newX));
        newZ = Math.max(-hh, Math.min(hh, newZ));

        setPosition(newX, newZ);
    }

    // ------------------------------------------------------------------
    // Combat
    // ------------------------------------------------------------------
    /** @return {@code true} if the attack cooldown has expired. */
    public boolean canAttack() {
        return attackCooldown <= 0f;
    }

    /** Arms an attack and resets the cooldown timer. */
    public void attack() {
        attackCooldown = Constants.PLAYER_ATTACK_COOLDOWN;
    }

    /**
     * Reduces health by {@code damage}.  Health is clamped to [0, maxHealth].
     *
     * @param damage amount of damage received
     */
    public void takeDamage(float damage) {
        health = Math.max(0f, health - damage);
    }

    /** @return {@code true} when health has reached zero. */
    public boolean isDead() {
        return health <= 0f;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public float getHealth()        { return health; }
    public float getMaxHealth()     { return maxHealth; }
    public float getHealthPercent() { return health / maxHealth; }
}
