package com.project.weapons;

/**
 * Immutable data record holding all stat values for a single weapon type.
 *
 * <p>These values are referenced by {@link WeaponType} and consumed by
 * {@link Weapon} when deciding how and when to fire.
 */
public final class WeaponStats {

    /** Base damage per bullet. */
    public final float damage;

    /** Rounds (shots) per second the weapon can fire. */
    public final float fireRate;

    /** Speed of each bullet in world-units per second. */
    public final float bulletSpeed;

    /** Visual half-extent of each bullet (radius-like size). */
    public final float bulletSize;

    /** Number of enemies a single bullet can pass through before disappearing. */
    public final int   pierce;

    /** Maximum number of times a bullet can bounce off level walls. */
    public final int   ricochet;

    /** Number of projectiles fired per shot (>1 for spread weapons). */
    public final int   pelletsPerShot;

    /** Spread half-angle in radians for multi-pellet weapons. */
    public final float spreadAngle;

    /** Requires this credit cost to unlock (0 = starts unlocked). */
    public final int   unlockCost;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public WeaponStats(float damage,
                       float fireRate,
                       float bulletSpeed,
                       float bulletSize,
                       int   pierce,
                       int   ricochet,
                       int   pelletsPerShot,
                       float spreadAngle,
                       int   unlockCost) {
        this.damage         = damage;
        this.fireRate       = fireRate;
        this.bulletSpeed    = bulletSpeed;
        this.bulletSize     = bulletSize;
        this.pierce         = pierce;
        this.ricochet       = ricochet;
        this.pelletsPerShot = pelletsPerShot;
        this.spreadAngle    = spreadAngle;
        this.unlockCost     = unlockCost;
    }

    /** Convenience constructor for single-pellet, non-piercing, non-ricocheting weapons. */
    public WeaponStats(float damage, float fireRate, float bulletSpeed,
                       float bulletSize, int unlockCost) {
        this(damage, fireRate, bulletSpeed, bulletSize, 0, 0, 1, 0f, unlockCost);
    }
}
