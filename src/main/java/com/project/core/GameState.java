package com.project.core;

/**
 * All possible states the game can be in at any point in time.
 *
 * <p>State transitions:
 * <pre>
 *   PLAYING ──P key──► PAUSED ──P key──► PLAYING
 *   PLAYING ──player dies──► GAME_OVER ──R key──► PLAYING
 * </pre>
 */
public enum GameState {
    /** The game is actively running: enemies move, input is read, HUD updates. */
    PLAYING,

    /** The game is frozen; no entity updates occur.  Press P to resume. */
    PAUSED,

    /** The player has died.  The game over overlay is shown.  Press R to restart. */
    GAME_OVER
}
