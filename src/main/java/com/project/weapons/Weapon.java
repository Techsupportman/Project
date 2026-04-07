package com.project.weapons;

/**
 * Represents a player's currently equipped weapon and tracks firing state
 * (fire-rate cooldown, unlock status).
 *
 * <p>Firing logic (creating projectile entities) is handled by the caller
 * ({@link com.project.core.GameApp}); this class only decides <em>when</em>
 * and <em>how many</em> shots to fire based on the weapon's stats.
 */
public class Weapon {

    private final WeaponType type;
    private boolean unlocked;

    /** Seconds remaining until the weapon can fire again. */
    private float fireCooldown = 0f;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Weapon(WeaponType type) {
        this.type     = type;
        this.unlocked = type.isStarterWeapon();
    }

    // ------------------------------------------------------------------
    // Per-frame update
    // ------------------------------------------------------------------
    /** Advances the fire-rate cooldown timer. Call once per game frame. */
    public void update(float tpf) {
        if (fireCooldown > 0f) {
            fireCooldown -= tpf;
        }
    }

    // ------------------------------------------------------------------
    // Firing
    // ------------------------------------------------------------------
    /**
     * Attempts to fire the weapon.
     *
     * @return {@code true} if the weapon was ready and the shot is authorised;
     *         {@code false} if still cooling down or locked.
     */
    public boolean tryFire() {
        if (!unlocked || fireCooldown > 0f) return false;
        fireCooldown = 1f / type.stats.fireRate;
        return true;
    }

    /** @return {@code true} when the weapon is off cooldown and ready to fire. */
    public boolean isReady() {
        return unlocked && fireCooldown <= 0f;
    }

    // ------------------------------------------------------------------
    // Unlock
    // ------------------------------------------------------------------
    /** Unlocks the weapon so it can be equipped and fired. */
    public void unlock() {
        this.unlocked = true;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public WeaponType  getType()       { return type; }
    public WeaponStats getStats()      { return type.stats; }
    public boolean     isUnlocked()    { return unlocked; }
    public float       getFireCooldown() { return fireCooldown; }
}
