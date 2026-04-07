package com.project.weapons;

/**
 * Enumerates all 18 weapon types available in the game, each with their
 * associated {@link WeaponStats}.
 *
 * <p>Weapons are unlocked by spending Credits (earned as score × 0.1).
 * The {@code PISTOL} starts unlocked (unlockCost = 0).
 *
 * <p>Stats follow a progression:
 * {@code (damage, fireRate, bulletSpeed, bulletSize, pierce, ricochet, pellets, spread, unlockCost)}
 */
public enum WeaponType {

    // ------------------------------------------------------------------
    // Tier 1 — Starter / basic
    // ------------------------------------------------------------------
    PISTOL      ("Pistol",      new WeaponStats(12f,  4f,  18f, 0.12f, 0)),
    REVOLVER    ("Revolver",    new WeaponStats(28f,  2f,  20f, 0.15f, 50)),
    SMG         ("SMG",         new WeaponStats(7f,  10f,  22f, 0.10f, 80)),

    // ------------------------------------------------------------------
    // Tier 2 — Spread / area
    // ------------------------------------------------------------------
    SHOTGUN     ("Shotgun",     new WeaponStats(10f,  1.8f, 16f, 0.12f, 0, 0, 5, 0.35f, 100)),
    DOUBLE_SHOT ("Double Shot", new WeaponStats(14f,  3f,   18f, 0.12f, 0, 0, 2, 0.08f, 120)),
    SCATTER     ("Scatter",     new WeaponStats(7f,   1.5f, 15f, 0.10f, 0, 0, 8, 0.50f, 150)),

    // ------------------------------------------------------------------
    // Tier 3 — Precision / long range
    // ------------------------------------------------------------------
    SNIPER      ("Sniper",      new WeaponStats(80f,  0.7f, 40f, 0.10f, 1, 0, 1, 0f,    200)),
    BURST       ("Burst",       new WeaponStats(15f,  3f,   22f, 0.11f, 0, 0, 3, 0.05f, 180)),
    MARKSMAN    ("Marksman",    new WeaponStats(35f,  1.5f, 30f, 0.11f, 0, 0, 1, 0f,    160)),

    // ------------------------------------------------------------------
    // Tier 4 — Piercing / wall-bounce
    // ------------------------------------------------------------------
    PIERCER     ("Piercer",     new WeaponStats(20f,  3f,   20f, 0.11f, 3, 0, 1, 0f,    220)),
    BOUNCER     ("Bouncer",     new WeaponStats(18f,  2.5f, 18f, 0.12f, 0, 3, 1, 0f,    240)),
    RAIL        ("Rail",        new WeaponStats(50f,  1f,   35f, 0.09f, 5, 0, 1, 0f,    300)),

    // ------------------------------------------------------------------
    // Tier 5 — Exotic
    // ------------------------------------------------------------------
    FLAK        ("Flak",        new WeaponStats(8f,   2f,   14f, 0.14f, 0, 2, 6, 0.45f, 280)),
    LASER       ("Laser",       new WeaponStats(5f,  15f,   30f, 0.08f, 2, 0, 1, 0f,    260)),
    SHOTGUN_SNG ("Super Shotgun",new WeaponStats(14f, 1.2f, 14f, 0.13f, 0, 0, 10,0.60f, 320)),

    // ------------------------------------------------------------------
    // Tier 6 — Heavy
    // ------------------------------------------------------------------
    MINIGUN     ("Minigun",     new WeaponStats(5f,  20f,   18f, 0.09f, 0, 0, 1, 0.04f, 350)),
    GRENADE     ("Grenade",     new WeaponStats(60f,  0.5f, 12f, 0.20f, 0, 0, 1, 0f,    400)),
    ROCKET      ("Rocket",      new WeaponStats(90f,  0.4f, 10f, 0.22f, 0, 0, 1, 0f,    500));

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    public final String      displayName;
    public final WeaponStats stats;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    WeaponType(String displayName, WeaponStats stats) {
        this.displayName = displayName;
        this.stats       = stats;
    }

    /** @return {@code true} if this weapon is available from the start. */
    public boolean isStarterWeapon() {
        return stats.unlockCost == 0;
    }
}
