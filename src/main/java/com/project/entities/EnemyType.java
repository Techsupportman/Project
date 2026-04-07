package com.project.entities;

/**
 * Classifies every enemy in the game, controlling their base stats, visual
 * representation, and AI behaviour.
 *
 * <p>Each type provides:
 * <ul>
 *   <li>Base health and movement speed</li>
 *   <li>Score and EXP values awarded on kill</li>
 *   <li>Earliest game-time (seconds) at which this enemy can spawn</li>
 *   <li>Whether the enemy is a mini-boss or final boss</li>
 * </ul>
 */
public enum EnemyType {

    // ------------------------------------------------------------------
    // Standard enemies (spawnAfterSeconds = earliest appearance)
    // ------------------------------------------------------------------
    BASIC       ("Basic",       30f,  3.0f, 10, 8,   0,    false, false),
    RUNNER      ("Runner",      20f,  5.5f,  8, 6,   30,   false, false),
    TANK        ("Tank",        80f,  1.8f, 25, 20,  60,   false, false),
    SHOOTER     ("Shooter",     25f,  2.0f, 15, 12,  90,   false, false),
    SWARM       ("Swarm",       12f,  4.5f,  5, 4,   45,   false, false),
    BRUISER     ("Bruiser",     60f,  2.8f, 20, 18,  120,  false, false),
    SPECTER     ("Specter",     35f,  4.0f, 18, 14,  150,  false, false),
    ARTILLERY   ("Artillery",   40f,  1.5f, 22, 16,  180,  false, false),

    // ------------------------------------------------------------------
    // Mini-bosses (occasionally replace a normal spawn cluster)
    // ------------------------------------------------------------------
    MINI_BOSS_ALPHA ("Mini-Boss Alpha", 200f, 2.5f, 100, 80, 120, true, false),
    MINI_BOSS_BETA  ("Mini-Boss Beta",  250f, 3.5f, 120, 100, 240, true, false),

    // ------------------------------------------------------------------
    // Bosses (spawn every 5 real-time minutes, except the final boss)
    // ------------------------------------------------------------------
    BOSS_1      ("Colossus",    500f,  2.0f, 500, 400, 300,  false, true),
    BOSS_2      ("Hive Mind",   750f,  2.5f, 750, 600, 600,  false, true),
    BOSS_3      ("Necromancer", 1000f, 1.8f, 1000,800, 900,  false, true),
    BOSS_4      ("Titan",       1400f, 2.2f, 1400,1100,1200, false, true),
    FINAL_BOSS  ("Oblivion",    2500f, 2.8f, 3000,2000,1800, false, true);

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    public final String  displayName;
    /** Base HP before difficulty multipliers are applied. */
    public final float   baseHealth;
    /** Base movement speed. */
    public final float   baseSpeed;
    /** Score awarded to the player on kill. */
    public final int     scoreValue;
    /** EXP awarded to the player on kill. */
    public final int     expValue;
    /** Minimum elapsed game-time in seconds before this type can appear. */
    public final int     spawnAfterSeconds;
    public final boolean isMiniBoss;
    public final boolean isBoss;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    EnemyType(String displayName,
              float  baseHealth,
              float  baseSpeed,
              int    scoreValue,
              int    expValue,
              int    spawnAfterSeconds,
              boolean isMiniBoss,
              boolean isBoss) {
        this.displayName       = displayName;
        this.baseHealth        = baseHealth;
        this.baseSpeed         = baseSpeed;
        this.scoreValue        = scoreValue;
        this.expValue          = expValue;
        this.spawnAfterSeconds = spawnAfterSeconds;
        this.isMiniBoss        = isMiniBoss;
        this.isBoss            = isBoss;
    }

    /** @return {@code true} if this is any kind of boss (mini or full). */
    public boolean isAnyBoss() {
        return isMiniBoss || isBoss;
    }
}
