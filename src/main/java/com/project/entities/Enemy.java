package com.project.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.project.assets.PlaceholderGenerator;
import com.project.difficulty.Difficulty;
import com.project.utils.Constants;

/**
 * An AI-controlled enemy.  Behaviour varies by {@link EnemyType}.
 *
 * <p>Movement is driven by {@link com.project.systems.AISystem};
 * damage to the player is applied by {@link com.project.systems.CombatSystem}.
 *
 * <p>Health and damage are scaled by the current {@link Difficulty} at
 * construction time.
 */
public class Enemy extends GameObject {

    // ------------------------------------------------------------------
    // Type & stats
    // ------------------------------------------------------------------
    private final EnemyType type;
    private float health;
    private final float maxHealth;
    private final float speed;
    private final float contactDamage;
    private float attackCooldown;

    // ------------------------------------------------------------------
    // Visibility (set by VisionSystem each frame)
    // ------------------------------------------------------------------
    private boolean visibleToPlayer = false;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Enemy(AssetManager assetManager, float x, float z, EnemyType type, Difficulty difficulty) {
        super(x, z, sizeFor(type));
        this.type          = type;
        this.maxHealth     = type.baseHealth * difficulty.enemyHealthMult;
        this.health        = maxHealth;
        this.speed         = type.baseSpeed;
        this.contactDamage = Constants.ENEMY_ATTACK_DAMAGE * difficulty.enemyDamageMult;
        this.attackCooldown = 0f;

        ColorRGBA color = colorFor(type);
        float     sz    = sizeFor(type);

        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager,
                type.displayName.toUpperCase(),
                sz, sz, color
        ));

        // Bosses/mini-bosses start visible so the player always sees them
        if (type.isAnyBoss()) visibleToPlayer = true;
    }

    /** Legacy constructor — creates a BASIC enemy on NORMAL difficulty. */
    public Enemy(AssetManager assetManager, float x, float z, float speed) {
        this(assetManager, x, z, EnemyType.BASIC, Difficulty.NORMAL);
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    @Override
    public void update(float tpf) {
        if (attackCooldown > 0f) {
            attackCooldown -= tpf;
        }
        // Sync node visibility with vision system result
        node.setCullHint(visibleToPlayer
                ? com.jme3.scene.Spatial.CullHint.Never
                : com.jme3.scene.Spatial.CullHint.Always);
    }

    // ------------------------------------------------------------------
    // Movement (called by AISystem)
    // ------------------------------------------------------------------
    /**
     * Moves the enemy by the supplied normalised direction vector scaled by
     * this enemy's speed and delta-time.
     *
     * @param dx  X-axis direction component
     * @param dz  Z-axis direction component
     * @param tpf seconds elapsed since last frame
     */
    public void moveToward(float dx, float dz, float tpf) {
        float newX = position.x + dx * speed * tpf;
        float newZ = position.z + dz * speed * tpf;

        float hw = Constants.LEVEL_HALF_WIDTH  - size;
        float hh = Constants.LEVEL_HALF_HEIGHT - size;
        newX = Math.max(-hw, Math.min(hw, newX));
        newZ = Math.max(-hh, Math.min(hh, newZ));

        setPosition(newX, newZ);
    }

    // ------------------------------------------------------------------
    // Combat
    // ------------------------------------------------------------------
    /**
     * Reduces health by {@code damage}.  Marks the enemy inactive when
     * health reaches zero.
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
    // Vision
    // ------------------------------------------------------------------
    /** Called by {@link com.project.systems.VisionSystem} each frame. */
    public void setVisibleToPlayer(boolean visible) {
        this.visibleToPlayer = visible;
    }

    /** @return {@code true} when this enemy is inside the player's vision. */
    public boolean isVisibleToPlayer() {
        return visibleToPlayer;
    }

    // ------------------------------------------------------------------
    // Static helpers
    // ------------------------------------------------------------------
    private static float sizeFor(EnemyType t) {
        if (t.isBoss)     return Constants.BOSS_SIZE;
        if (t.isMiniBoss) return Constants.MINI_BOSS_SIZE;
        return Constants.ENEMY_SIZE;
    }

    private static ColorRGBA colorFor(EnemyType t) {
        if (t.isBoss)     return new ColorRGBA(0.8f, 0.0f, 0.8f, 1.0f); // purple
        if (t.isMiniBoss) return new ColorRGBA(1.0f, 0.5f, 0.0f, 1.0f); // orange
        return switch (t) {
            case RUNNER    -> new ColorRGBA(1.0f, 0.4f, 0.4f, 1.0f);
            case TANK      -> new ColorRGBA(0.5f, 0.1f, 0.1f, 1.0f);
            case SHOOTER   -> new ColorRGBA(1.0f, 0.6f, 0.1f, 1.0f);
            case SWARM     -> new ColorRGBA(0.9f, 0.9f, 0.2f, 1.0f);
            case BRUISER   -> new ColorRGBA(0.6f, 0.1f, 0.1f, 1.0f);
            case SPECTER   -> new ColorRGBA(0.7f, 0.4f, 1.0f, 1.0f);
            case ARTILLERY -> new ColorRGBA(0.4f, 0.4f, 0.8f, 1.0f);
            default        -> new ColorRGBA(1.0f, 0.2f, 0.2f, 1.0f); // BASIC = red
        };
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public EnemyType getEnemyType()     { return type; }
    public float     getHealth()        { return health; }
    public float     getMaxHealth()     { return maxHealth; }
    public float     getSpeed()         { return speed; }
    public float     getContactDamage() { return contactDamage; }
}
