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
    public static final String GAME_TITLE      = "Project - Top Down 2D Game";
    public static final int    TARGET_FPS      = 60;

    // -------------------------------------------------------------------------
    // Camera (top-down orthographic)
    // -------------------------------------------------------------------------
    /** World-unit height the camera sits above the play field. */
    public static final float CAMERA_HEIGHT = 30f;

    // -------------------------------------------------------------------------
    // Level bounds (half-extents in world units)
    // -------------------------------------------------------------------------
    public static final float LEVEL_HALF_WIDTH  = 16f;
    public static final float LEVEL_HALF_HEIGHT = 10f;

    // -------------------------------------------------------------------------
    // Player
    // -------------------------------------------------------------------------
    public static final float PLAYER_SPEED          = 8f;
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
    public static final float ENEMY_MAX_HEALTH            = 50f;
    public static final float ENEMY_CONTACT_RANGE         = 0.9f;
    public static final float ENEMY_ATTACK_DAMAGE         = 10f;
    public static final float ENEMY_ATTACK_COOLDOWN       = 1.0f;
    /** Collision half-extent (square AABB). */
    public static final float ENEMY_SIZE                  = 0.45f;
    public static final int   ENEMY_SCORE_VALUE           = 10;

    // -------------------------------------------------------------------------
    // Wave / Spawning
    // -------------------------------------------------------------------------
    public static final float WAVE_DELAY                = 3f;
    public static final int   BASE_ENEMIES_PER_WAVE     = 3;
    public static final int   ENEMIES_INCREASE_PER_WAVE = 2;
    public static final float SPEED_INCREASE_PER_WAVE   = 0.25f;
}
