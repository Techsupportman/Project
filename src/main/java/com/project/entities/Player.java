package com.project.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.project.assets.PlaceholderGenerator;
import com.project.difficulty.Difficulty;
import com.project.utils.Constants;
import com.project.weapons.Weapon;
import com.project.weapons.WeaponType;

import java.util.ArrayList;
import java.util.List;

/**
 * The player-controlled character.
 *
 * <h3>Changes from the original MVP</h3>
 * <ul>
 *   <li>HP is now <em>integer hearts</em> (max set by difficulty).</li>
 *   <li>The player equips a {@link Weapon} and fires ranged projectiles.</li>
 *   <li>Aim direction (toward the mouse cursor) is tracked as a 2-D vector.</li>
 *   <li>EXP and level progression are managed here.</li>
 *   <li>Credits (score × 0.1) are accumulated for weapon/mutation unlocks.</li>
 * </ul>
 *
 * <h3>Controls</h3>
 * <ul>
 *   <li>WASD — movement</li>
 *   <li>Left Mouse Button (held) — fire active weapon toward cursor</li>
 *   <li>Right Mouse Button — toggle circle / cone vision</li>
 * </ul>
 */
public class Player extends GameObject {

    // ------------------------------------------------------------------
    // Hearts (integer-based HP)
    // ------------------------------------------------------------------
    private int hearts;
    private final int maxHearts;
    /** Invincibility frames after taking damage (prevents instant death). */
    private float invincibilityTimer = 0f;
    private static final float INVINCIBILITY_DURATION = 0.8f;

    // ------------------------------------------------------------------
    // Weapon
    // ------------------------------------------------------------------
    private final List<Weapon> weapons = new ArrayList<>();
    private int activeWeaponIndex = 0;

    // ------------------------------------------------------------------
    // Aim direction (normalised, toward mouse cursor in world space)
    // ------------------------------------------------------------------
    private float aimDirX = 1f;  // default: aim right
    private float aimDirZ = 0f;

    // ------------------------------------------------------------------
    // EXP & levelling
    // ------------------------------------------------------------------
    private int   level        = 1;
    private int   currentExp   = 0;
    private int   expToNextLevel;
    private boolean pendingLevelUp = false;

    // ------------------------------------------------------------------
    // Credits
    // ------------------------------------------------------------------
    private int credits = 0;

    // ------------------------------------------------------------------
    // Vision mode
    // ------------------------------------------------------------------
    /** {@code true} = circle vision; {@code false} = cone vision. */
    private boolean circleVisionMode = true;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Player(AssetManager assetManager, float x, float z, Difficulty difficulty) {
        super(x, z, Constants.PLAYER_SIZE);
        this.maxHearts    = difficulty.startingHearts;
        this.hearts       = maxHearts;
        this.expToNextLevel = Constants.EXP_PER_LEVEL;

        // Register all weapon types; only starter weapons are unlocked
        for (WeaponType wt : WeaponType.values()) {
            weapons.add(new Weapon(wt));
        }

        // Placeholder: blue box labelled "PLAYER"
        node.attachChild(PlaceholderGenerator.createPlaceholderNode(
                assetManager,
                "PLAYER",
                Constants.PLAYER_SIZE,
                Constants.PLAYER_SIZE,
                new ColorRGBA(0.25f, 0.55f, 1.0f, 1.0f)
        ));
    }

    /** Legacy constructor — uses NORMAL difficulty. */
    public Player(AssetManager assetManager, float x, float z) {
        this(assetManager, x, z, Difficulty.NORMAL);
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    @Override
    public void update(float tpf) {
        if (invincibilityTimer > 0f) {
            invincibilityTimer -= tpf;
        }
        getActiveWeapon().update(tpf);
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

        float hw = Constants.LEVEL_HALF_WIDTH  - Constants.PLAYER_SIZE;
        float hh = Constants.LEVEL_HALF_HEIGHT - Constants.PLAYER_SIZE;
        newX = Math.max(-hw, Math.min(hw, newX));
        newZ = Math.max(-hh, Math.min(hh, newZ));

        setPosition(newX, newZ);
    }

    // ------------------------------------------------------------------
    // Weapon / Shooting
    // ------------------------------------------------------------------
    /** @return the currently equipped weapon. */
    public Weapon getActiveWeapon() {
        return weapons.get(activeWeaponIndex);
    }

    /**
     * Attempts to fire the active weapon.
     *
     * @return {@code true} if the weapon fired (caller should spawn a projectile)
     */
    public boolean tryFire() {
        return getActiveWeapon().tryFire();
    }

    /**
     * Cycles to the next unlocked weapon in the list.
     */
    public void cycleWeapon() {
        int start = activeWeaponIndex;
        do {
            activeWeaponIndex = (activeWeaponIndex + 1) % weapons.size();
        } while (!weapons.get(activeWeaponIndex).isUnlocked()
                && activeWeaponIndex != start);
    }

    /**
     * Unlocks the weapon with the specified type if the player has enough credits.
     *
     * @param type   weapon to unlock
     * @return {@code true} if the unlock succeeded
     */
    public boolean unlockWeapon(WeaponType type) {
        if (credits < type.stats.unlockCost) return false;
        for (Weapon w : weapons) {
            if (w.getType() == type && !w.isUnlocked()) {
                credits -= type.stats.unlockCost;
                w.unlock();
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Aim
    // ------------------------------------------------------------------
    /**
     * Updates the aim direction from the player toward the world-space cursor
     * position.  The vector is normalised automatically.
     *
     * @param worldCursorX  cursor X in world space
     * @param worldCursorZ  cursor Z in world space
     */
    public void updateAim(float worldCursorX, float worldCursorZ) {
        float dx  = worldCursorX - position.x;
        float dz  = worldCursorZ - position.z;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len > 0.001f) {
            aimDirX = dx / len;
            aimDirZ = dz / len;
        }
    }

    // ------------------------------------------------------------------
    // Vision toggle
    // ------------------------------------------------------------------
    /** Toggles between circle and cone vision modes. */
    public void toggleVisionMode() {
        circleVisionMode = !circleVisionMode;
    }

    // ------------------------------------------------------------------
    // Combat (receiving damage)
    // ------------------------------------------------------------------
    /**
     * Removes one heart of HP, unless the player is currently invincible.
     * Activates brief invincibility to prevent rapid successive hits.
     */
    public void takeDamage() {
        if (invincibilityTimer > 0f) return;
        hearts = Math.max(0, hearts - 1);
        invincibilityTimer = INVINCIBILITY_DURATION;
    }

    /**
     * Heals the player by one heart.  If already at max HP the heart is
     * still consumed (full-heart pickup rule from the design spec).
     */
    public void heal() {
        hearts = Math.min(maxHearts, hearts + 1);
    }

    /** @return {@code true} when all hearts are lost. */
    public boolean isDead() {
        return hearts <= 0;
    }

    // ------------------------------------------------------------------
    // EXP & Levelling
    // ------------------------------------------------------------------
    /**
     * Awards EXP to the player.  If the threshold is reached, flags a
     * pending level-up (caller should pause and show the upgrade menu).
     *
     * @param amount raw EXP amount (before difficulty modifiers)
     * @param expMult difficulty EXP multiplier
     */
    public void addExp(int amount, float expMult) {
        currentExp += (int) (amount * expMult);
        if (currentExp >= expToNextLevel) {
            currentExp -= expToNextLevel;
            level++;
            // Scale EXP threshold: each level requires 10% more than the last
            expToNextLevel = (int) (expToNextLevel * 1.1f);
            pendingLevelUp = true;
        }
    }

    /**
     * Consumes the pending level-up flag.
     *
     * @return {@code true} once per level gained, then {@code false} until
     *         the next level-up
     */
    public boolean consumeLevelUp() {
        if (pendingLevelUp) {
            pendingLevelUp = false;
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Credits
    // ------------------------------------------------------------------
    /** Awards credits (score * 0.1 rule applied by the caller). */
    public void addCredits(int amount) {
        credits += amount;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public int   getHearts()          { return hearts; }
    public int   getMaxHearts()       { return maxHearts; }
    public int   getLevel()           { return level; }
    public int   getCurrentExp()      { return currentExp; }
    public int   getExpToNextLevel()  { return expToNextLevel; }
    public int   getCredits()         { return credits; }
    public float getAimDirX()         { return aimDirX; }
    public float getAimDirZ()         { return aimDirZ; }
    public boolean isCircleVision()   { return circleVisionMode; }
    public List<Weapon> getWeapons()  { return weapons; }

    // Legacy health accessors kept for backward compatibility with tests/HUD
    public float getHealth()          { return hearts * (100f / maxHearts); }
    public float getMaxHealth()       { return 100f; }
    public float getHealthPercent()   { return maxHearts > 0 ? (float) hearts / maxHearts : 0f; }
}
