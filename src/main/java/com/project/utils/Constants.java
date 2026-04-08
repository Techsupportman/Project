package com.project.utils;

/**
 * Central repository of game-wide constants.
 * Change values here to tweak game balance without hunting through multiple classes.
 */
public final class Constants {

    private Constants() {}

    // -------------------------------------------------------------------------
    // Window / Display
    // -------------------------------------------------------------------------
    public static final int    WINDOW_WIDTH    = 1280;
    public static final int    WINDOW_HEIGHT   = 720;
    public static final String GAME_TITLE      = "Project — Disfigure-inspired";
    public static final int    TARGET_FPS      = 60;

    // -------------------------------------------------------------------------
    // Camera (top-down orthographic)
    // -------------------------------------------------------------------------
    /** World-unit height the camera sits above the play field. */
    public static final float CAMERA_HEIGHT = 30f;

    // -------------------------------------------------------------------------
    // Level bounds (half-extents in world units)
    // -------------------------------------------------------------------------
    public static final float LEVEL_HALF_WIDTH  = 20f;
    public static final float LEVEL_HALF_HEIGHT = 12f;

    // -------------------------------------------------------------------------
    // Player
    // -------------------------------------------------------------------------
    public static final float PLAYER_SPEED          = 8f;
    /** Legacy float health kept for HUD backward compatibility. */
    public static final float PLAYER_MAX_HEALTH     = 100f;
    public static final float PLAYER_ATTACK_RANGE   = 2.8f;
    public static final float PLAYER_ATTACK_DAMAGE  = 25f;
    public static final float PLAYER_ATTACK_COOLDOWN = 0.5f;
    /** Collision half-extent (square AABB). */
    public static final float PLAYER_SIZE           = 0.5f;

    // -------------------------------------------------------------------------
    // Enemy
    // -------------------------------------------------------------------------
    public static final float ENEMY_SPEED_BASE            = 2.5f;
    public static final float ENEMY_MAX_HEALTH            = 30f;
    public static final float ENEMY_CONTACT_RANGE         = 0.9f;
    public static final float ENEMY_ATTACK_DAMAGE         = 1f;  // 1 heart
    public static final float ENEMY_ATTACK_COOLDOWN       = 1.0f;
    /** Collision half-extent for standard enemies. */
    public static final float ENEMY_SIZE                  = 0.45f;
    /** Collision half-extent for mini-bosses. */
    public static final float MINI_BOSS_SIZE              = 0.75f;
    /** Collision half-extent for full bosses. */
    public static final float BOSS_SIZE                   = 1.20f;
    public static final int   ENEMY_SCORE_VALUE           = 10;

    // -------------------------------------------------------------------------
    // Enemy ranged attacks (SHOOTER type)
    // -------------------------------------------------------------------------
    /** Distance within which a SHOOTER will stop and fire. */
    public static final float SHOOTER_FIRE_RANGE    = 8f;
    /** Seconds between SHOOTER shots. */
    public static final float SHOOTER_FIRE_INTERVAL = 1.8f;
    /** Bullet travel speed for SHOOTER projectiles. */
    public static final float SHOOTER_BULLET_SPEED  = 14f;
    /** Damage (hearts) dealt by a SHOOTER bullet. */
    public static final float SHOOTER_BULLET_DAMAGE = 0.5f;
    /** Visual/collision radius for SHOOTER bullets. */
    public static final float SHOOTER_BULLET_SIZE   = 0.12f;

    // -------------------------------------------------------------------------
    // Enemy ranged attacks (ARTILLERY type)
    // -------------------------------------------------------------------------
    /** Distance within which an ARTILLERY enemy will stop and fire. */
    public static final float ARTILLERY_FIRE_RANGE    = 14f;
    /** Seconds between ARTILLERY shots. */
    public static final float ARTILLERY_FIRE_INTERVAL = 3.0f;
    /** Bullet travel speed for ARTILLERY projectiles. */
    public static final float ARTILLERY_BULLET_SPEED  = 10f;
    /** Damage (hearts) dealt by an ARTILLERY shell. */
    public static final float ARTILLERY_BULLET_DAMAGE = 1.0f;
    /** Visual/collision radius for ARTILLERY shells. */
    public static final float ARTILLERY_BULLET_SIZE   = 0.20f;

    // -------------------------------------------------------------------------
    // Wave / Spawning (time-based continuous system)
    // -------------------------------------------------------------------------
    /** Legacy wave delay kept for test compatibility. */
    public static final float WAVE_DELAY                = 3f;
    /** Legacy enemies-per-wave kept for test compatibility. */
    public static final int   BASE_ENEMIES_PER_WAVE     = 3;
    public static final int   ENEMIES_INCREASE_PER_WAVE = 2;
    public static final float SPEED_INCREASE_PER_WAVE   = 0.25f;

    /** Starting interval between enemy spawns (seconds). */
    public static final float SPAWN_INTERVAL_INITIAL    = 3.0f;
    /** Minimum spawn interval after full ramp-up. */
    public static final float SPAWN_INTERVAL_MIN        = 0.4f;
    /** Seconds over which the spawn rate ramps from initial to min. */
    public static final float SPAWN_RAMP_DURATION       = 600f;  // 10 minutes
    /** Maximum enemies on screen at once. */
    public static final int   MAX_ENEMIES_ALIVE         = 80;

    // -------------------------------------------------------------------------
    // EXP & Levelling
    // -------------------------------------------------------------------------
    /** Base EXP required to reach level 2 (each level costs 10% more). */
    public static final int EXP_PER_LEVEL = 100;

    // -------------------------------------------------------------------------
    // Scoring & Credits
    // -------------------------------------------------------------------------
    /** Credits = floor(score × CREDITS_PER_SCORE). */
    public static final float CREDITS_PER_SCORE = 0.1f;
}
