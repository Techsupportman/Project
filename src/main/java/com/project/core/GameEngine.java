package com.project.core;

import com.project.utils.Constants;

/**
 * Encapsulates all high-level game-logic state: wave progression, score, and
 * player-death detection.
 *
 * <p>{@link GameApp} owns an instance of this class and calls
 * {@link #tick(float)} every frame while the game is in
 * {@link GameState#PLAYING}.  The return value signals to the app whether a
 * significant event has occurred that requires a scene-graph change (e.g.
 * spawning a new wave or triggering game-over).
 */
public class GameEngine {

    // ------------------------------------------------------------------
    // Wave state
    // ------------------------------------------------------------------
    private int   currentWave       = 0;
    private int   enemiesAlive      = 0;
    private float waveCountdown     = 0f;   // time remaining before next wave
    private boolean waitingForWave  = true; // true = countdown running, false = wave active

    // ------------------------------------------------------------------
    // Score
    // ------------------------------------------------------------------
    private int score = 0;

    // ------------------------------------------------------------------
    // Events returned from tick()
    // ------------------------------------------------------------------
    public enum Event {
        /** Nothing significant happened this frame. */
        NONE,
        /** All enemies in the current wave have been killed — start countdown. */
        WAVE_CLEARED,
        /** The countdown has elapsed — spawn the next wave. */
        START_NEXT_WAVE
    }

    // ------------------------------------------------------------------
    // Per-frame update
    // ------------------------------------------------------------------
    /**
     * Advances the engine by one frame.
     *
     * @param tpf         seconds elapsed since last frame
     * @param enemiesLeft number of currently active (alive) enemies in the scene
     * @return an {@link Event} that the caller should act upon
     */
    public Event tick(float tpf, int enemiesLeft) {
        if (waitingForWave) {
            waveCountdown -= tpf;
            if (waveCountdown <= 0f) {
                waitingForWave = false;
                return Event.START_NEXT_WAVE;
            }
        } else {
            if (enemiesLeft == 0 && currentWave > 0) {
                // Wave has been cleared — begin countdown to next wave
                waitingForWave = true;
                waveCountdown  = Constants.WAVE_DELAY;
                return Event.WAVE_CLEARED;
            }
        }
        return Event.NONE;
    }

    // ------------------------------------------------------------------
    // Wave management
    // ------------------------------------------------------------------
    /**
     * Increments the wave counter.  Called by {@link GameApp} when
     * {@link Event#START_NEXT_WAVE} is received.
     */
    public void advanceWave() {
        currentWave++;
    }

    /**
     * Number of enemies to spawn for the upcoming wave.
     * Scales linearly with wave number.
     */
    public int enemiesForNextWave() {
        return Constants.BASE_ENEMIES_PER_WAVE
                + (currentWave) * Constants.ENEMIES_INCREASE_PER_WAVE;
    }

    /**
     * Enemy movement speed for the upcoming wave.
     * Increases slightly each wave to raise difficulty.
     */
    public float speedForNextWave() {
        return Constants.ENEMY_SPEED_BASE
                + (currentWave) * Constants.SPEED_INCREASE_PER_WAVE;
    }

    // ------------------------------------------------------------------
    // Score
    // ------------------------------------------------------------------
    /** Awards {@link Constants#ENEMY_SCORE_VALUE} points for a single kill. */
    public void recordKill() {
        score += Constants.ENEMY_SCORE_VALUE;
    }

    // ------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------
    /** Resets all state back to the start-of-game values. */
    public void reset() {
        currentWave    = 0;
        enemiesAlive   = 0;
        waveCountdown  = 0f;
        waitingForWave = true;
        score          = 0;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    public int     getCurrentWave()    { return currentWave;    }
    public int     getScore()          { return score;          }
    public float   getWaveCountdown()  { return waveCountdown;  }
    public boolean isWaitingForWave()  { return waitingForWave; }
}
