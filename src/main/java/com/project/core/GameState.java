package com.project.core;

/**
 * All possible states the game can be in at any point in time.
 *
 * <p>State transitions:
 * <pre>
 *   DIFFICULTY_SELECT ──choice──► PLAYING
 *   PLAYING ──P key──► PAUSED ──P key──► PLAYING
 *   PLAYING ──player dies──► GAME_OVER ──R key──► DIFFICULTY_SELECT
 *   PLAYING ──level up──► LEVEL_UP ──choice──► PLAYING
 *   PLAYING ──boss spawns──► BOSS_ARENA (sub-state, still PLAYING)
 * </pre>
 */
public enum GameState {
    /** Shown at start/after game-over — player picks a difficulty tier. */
    DIFFICULTY_SELECT,

    /** The game is actively running: enemies move, input is read, HUD updates. */
    PLAYING,

    /** The game is frozen; no entity updates occur.  Press P to resume. */
    PAUSED,

    /** Player levelled up — upgrade choice menu is displayed. */
    LEVEL_UP,

    /** The player has died.  The game over overlay is shown.  Press R to restart. */
    GAME_OVER
}
